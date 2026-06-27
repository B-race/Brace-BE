package com.brace.server.notification.repository;

import com.brace.server.TestcontainersConfiguration;
import com.brace.server.application.entity.Application;
import com.brace.server.application.repository.ApplicationRepository;
import com.brace.server.global.config.JpaAuditingConfig;
import com.brace.server.notification.entity.Notification;
import com.brace.server.notification.entity.NotificationType;
import com.brace.server.project.entity.*;
import com.brace.server.project.repository.ProjectRepository;
import com.brace.server.project.repository.ProjectRoleRepository;
import com.brace.server.user.entity.*;
import com.brace.server.user.repository.RoleRepository;
import com.brace.server.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
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
    private RoleRepository roleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectRoleRepository projectRoleRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

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

    @Test
    @DisplayName("delete by user id deletes only target user's notifications")
    void deleteByUserIdDeletesOnlyTargetUsersNotifications() {
        User user = saveUser(1L);
        User otherUser = saveUser(2L);
        Notification target = saveNotification(user, "target");
        Notification other = saveNotification(otherUser, "other");
        flushAndClear();

        notificationRepository.deleteByUser_Id(user.getId());
        flushAndClear();

        assertThat(notificationRepository.findById(target.getId())).isEmpty();
        assertThat(notificationRepository.findById(other.getId())).isPresent();
    }

    @Test
    @DisplayName("delete by application user id deletes notifications for target applicant applications")
    void deleteByApplicationUserIdDeletesNotificationsForTargetApplicantApplications() {
        User owner = saveUser(1L);
        User applicant = saveUser(2L);
        User otherApplicant = saveUser(3L);
        Role role = saveRole();
        Project project = saveProject(owner, role);
        Application targetApplication = saveApplication(applicant, project, role, "target");
        Application otherApplication = saveApplication(otherApplicant, project, role, "other");
        Notification target = saveNotification(owner, targetApplication, "target");
        Notification other = saveNotification(owner, otherApplication, "other");
        flushAndClear();

        notificationRepository.deleteByApplication_User_Id(applicant.getId());
        flushAndClear();

        assertThat(notificationRepository.findById(target.getId())).isEmpty();
        assertThat(notificationRepository.findById(other.getId())).isPresent();
    }

    @Test
    @DisplayName("delete by application project owner id deletes notifications for owner's project applications")
    void deleteByApplicationProjectOwnerIdDeletesNotificationsForOwnersProjectApplications() {
        User owner = saveUser(1L);
        User otherOwner = saveUser(2L);
        User applicant = saveUser(3L);
        User otherApplicant = saveUser(4L);
        Role role = saveRole();
        Project project = saveProject(owner, role);
        Project otherProject = saveProject(otherOwner, role);
        Application targetApplication = saveApplication(applicant, project, role, "target");
        Application otherApplication = saveApplication(otherApplicant, otherProject, role, "other");
        Notification target = saveNotification(owner, targetApplication, "target");
        Notification other = saveNotification(otherOwner, otherApplication, "other");
        flushAndClear();

        notificationRepository.deleteByApplication_Project_User_Id(owner.getId());
        flushAndClear();

        assertThat(notificationRepository.findById(target.getId())).isEmpty();
        assertThat(notificationRepository.findById(other.getId())).isPresent();
    }

    private Notification saveNotification(User user, String content) {
        return notificationRepository.save(Notification.builder()
                .type(NotificationType.SYSTEM)
                .content(content)
                .user(user)
                .build());
    }

    private Notification saveNotification(User user, Application application, String content) {
        return notificationRepository.save(Notification.builder()
                .type(NotificationType.APPLICATION_RESULT)
                .content(content)
                .user(user)
                .application(application)
                .build());
    }

    private Application saveApplication(User applicant, Project project, Role role, String message) {
        return applicationRepository.save(Application.builder()
                .message(message)
                .user(applicant)
                .project(project)
                .role(role)
                .build());
    }

    private Project saveProject(User owner, Role role) {
        Project project = projectRepository.save(Project.builder()
                .activityType(ActivityType.PERSONAL_PROJECT)
                .title("프로젝트")
                .description("설명")
                .projectName("brace")
                .projectUrl("https://example.com")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .deadline(LocalDate.now().plusDays(1))
                .meetingType(MeetingType.ONLINE)
                .tags("java,spring")
                .status(ProjectStatus.RECRUITING)
                .user(owner)
                .build());
        projectRoleRepository.save(ProjectRole.builder()
                .project(project)
                .role(role)
                .recruitCount(3)
                .build());
        return project;
    }

    private Role saveRole() {
        return roleRepository.save(Role.builder()
                .id(10L)
                .name("backend")
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
