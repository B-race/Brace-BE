package com.brace.server.notification.repository;

import com.brace.server.TestcontainersConfiguration;
import com.brace.server.global.config.JpaAuditingConfig;
import com.brace.server.notification.entity.Notification;
import com.brace.server.notification.entity.NotificationType;
import com.brace.server.user.entity.ParticipationType;
import com.brace.server.user.entity.SkillTag;
import com.brace.server.user.entity.SocialProvider;
import com.brace.server.user.entity.User;
import com.brace.server.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import({TestcontainersConfiguration.class, JpaAuditingConfig.class})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("find by user id with cursor returns unread notifications in descending order")
    void findByUserIdWithCursorReturnsUnreadNotificationsInDescendingOrder() {
        User user = saveUser(1L);
        User otherUser = saveUser(2L);
        Notification first = saveNotification(user, "first");
        Notification second = saveNotification(user, "second");
        Notification third = saveNotification(user, "third");
        second.markAsRead();
        saveNotification(otherUser, "other");
        flushAndClear();

        List<Notification> result = notificationRepository.findByUserIdWithCursor(
                user.getId(),
                null,
                false,
                PageRequest.of(0, 10)
        );

        assertThat(result).extracting(Notification::getId).containsExactly(third.getId(), first.getId());
    }

    @Test
    @DisplayName("find by user id with cursor applies cursor condition")
    void findByUserIdWithCursorAppliesCursorCondition() {
        User user = saveUser(1L);
        Notification first = saveNotification(user, "first");
        Notification second = saveNotification(user, "second");
        Notification third = saveNotification(user, "third");
        flushAndClear();

        List<Notification> result = notificationRepository.findByUserIdWithCursor(
                user.getId(),
                second.getId(),
                null,
                PageRequest.of(0, 10)
        );

        assertThat(result).extracting(Notification::getId).containsExactly(first.getId());
        assertThat(third.getId()).isGreaterThan(second.getId());
    }

    @Test
    @DisplayName("find by id with user returns notification owner")
    void findByIdWithUserReturnsNotificationOwner() {
        User user = saveUser(1L);
        Notification notification = saveNotification(user, "알림");
        flushAndClear();

        Notification result = notificationRepository.findByIdWithUser(notification.getId()).orElseThrow();

        assertThat(result.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("mark all as read by user id updates only target user unread notifications")
    void markAllAsReadByUserIdUpdatesOnlyTargetUserUnreadNotifications() {
        User user = saveUser(1L);
        User otherUser = saveUser(2L);
        Notification unread = saveNotification(user, "unread");
        Notification alreadyRead = saveNotification(user, "read");
        alreadyRead.markAsRead();
        Notification other = saveNotification(otherUser, "other");
        flushAndClear();

        int updated = notificationRepository.markAllAsReadByUserId(user.getId());
        flushAndClear();

        assertThat(updated).isEqualTo(1);
        assertThat(notificationRepository.findById(unread.getId()).orElseThrow().getIsRead()).isTrue();
        assertThat(notificationRepository.findById(alreadyRead.getId()).orElseThrow().getIsRead()).isTrue();
        assertThat(notificationRepository.findById(other.getId()).orElseThrow().getIsRead()).isFalse();
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
