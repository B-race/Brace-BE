package com.brace.server.notification.service;

import com.brace.server.TestcontainersConfiguration;
import com.brace.server.global.code.NotificationErrorCode;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.notification.dto.NotificationReqDto.NotificationSearch;
import com.brace.server.notification.dto.NotificationResDto.NotificationSlice;
import com.brace.server.notification.entity.Notification;
import com.brace.server.notification.entity.NotificationType;
import com.brace.server.notification.repository.NotificationRepository;
import com.brace.server.user.entity.ParticipationType;
import com.brace.server.user.entity.SkillTag;
import com.brace.server.user.entity.SocialProvider;
import com.brace.server.user.entity.User;
import com.brace.server.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("get notifications returns cursor page")
    void getNotificationsReturnsCursorPage() {
        User user = saveUser(1L);
        Notification first = saveNotification(user, "first");
        Notification second = saveNotification(user, "second");
        Notification third = saveNotification(user, "third");
        flushAndClear();

        NotificationSlice response = notificationService.getNotifications(
                user.getId(),
                new NotificationSearch(null, 2, null)
        );

        assertThat(response.content()).extracting("id").containsExactly(third.getId(), second.getId());
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursorId()).isEqualTo(second.getId());
        assertThat(first.getId()).isLessThan(response.nextCursorId());
    }

    @Test
    @DisplayName("get notifications validates request dto")
    void getNotificationsValidatesRequestDto() {
        User user = saveUser(1L);

        assertThatThrownBy(() -> notificationService.getNotifications(
                user.getId(),
                new NotificationSearch(0L, 101, null)
        )).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("mark as read marks owned notification")
    void markAsReadMarksOwnedNotification() {
        User user = saveUser(1L);
        Notification notification = saveNotification(user, "알림");
        flushAndClear();

        notificationService.markAsRead(user.getId(), notification.getId());
        flushAndClear();

        assertThat(notificationRepository.findById(notification.getId()).orElseThrow().getIsRead()).isTrue();
    }

    @Test
    @DisplayName("mark as read rejects other user's notification")
    void markAsReadRejectsOtherUsersNotification() {
        User user = saveUser(1L);
        User otherUser = saveUser(2L);
        Notification notification = saveNotification(otherUser, "다른 사용자 알림");
        flushAndClear();

        assertProjectException(
                () -> notificationService.markAsRead(user.getId(), notification.getId()),
                NotificationErrorCode.NOTIFICATION_ACCESS_DENIED
        );
    }

    @Test
    @DisplayName("mark as read rejects missing notification")
    void markAsReadRejectsMissingNotification() {
        User user = saveUser(1L);

        assertProjectException(
                () -> notificationService.markAsRead(user.getId(), 999L),
                NotificationErrorCode.NOTIFICATION_NOT_FOUND
        );
    }

    @Test
    @DisplayName("mark all as read updates only current user's unread notifications")
    void markAllAsReadUpdatesOnlyCurrentUsersUnreadNotifications() {
        User user = saveUser(1L);
        User otherUser = saveUser(2L);
        Notification unread = saveNotification(user, "unread");
        Notification alreadyRead = saveNotification(user, "read");
        alreadyRead.markAsRead();
        Notification other = saveNotification(otherUser, "other");
        flushAndClear();

        notificationService.markAllAsRead(user.getId());
        flushAndClear();

        assertThat(notificationRepository.findById(unread.getId()).orElseThrow().getIsRead()).isTrue();
        assertThat(notificationRepository.findById(alreadyRead.getId()).orElseThrow().getIsRead()).isTrue();
        assertThat(notificationRepository.findById(other.getId()).orElseThrow().getIsRead()).isFalse();
    }

    private void assertProjectException(Runnable runnable, NotificationErrorCode expectedErrorCode) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(ProjectException.class)
                .extracting(exception -> ((ProjectException) exception).getErrorCode())
                .isEqualTo(expectedErrorCode);
    }

    private Notification saveNotification(User user, String content) {
        return notificationRepository.save(Notification.builder()
                .type(NotificationType.SYSTEM)
                .content(content)
                .user(user)
                .build());
    }

    private User saveUser(Long id) {
        User user = User.builder()
                .email("user" + id + "@example.com")
                .password("password")
                .socialProvider(SocialProvider.NONE)
                .socialId("social-" + id)
                .name("user" + id)
                .role("개발자")
                .profileImageUrl("https://example.com/" + id + ".png")
                .participationType(ParticipationType.BOTH)
                .introduction("소개")
                .portfolioUrl("https://example.com/portfolio/" + id)
                .profileCompleted(true)
                .build();
        user.profileOnboarding(
                "https://example.com/" + id + ".png",
                "개발자",
                List.of(SkillTag.JAVA, SkillTag.SPRING),
                ParticipationType.BOTH,
                "소개",
                "https://example.com/portfolio/" + id
        );
        return userRepository.save(user);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
