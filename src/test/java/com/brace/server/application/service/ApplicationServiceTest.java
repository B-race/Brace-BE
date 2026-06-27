package com.brace.server.application.service;

import com.brace.server.TestcontainersConfiguration;
import com.brace.server.application.dto.ApplicationReqDto.Message;
import com.brace.server.application.dto.ApplicationReqDto.Status;
import com.brace.server.application.dto.ApplicationResDto.ApplicationId;
import com.brace.server.application.dto.ApplicationResDto.ApplicationResult;
import com.brace.server.application.dto.ApplicationResDto.ApplicationSlice;
import com.brace.server.application.entity.Application;
import com.brace.server.application.entity.ApplicationStatus;
import com.brace.server.application.repository.ApplicationRepository;
import com.brace.server.global.code.ApplicationErrorCode;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.notification.entity.Notification;
import com.brace.server.notification.entity.NotificationType;
import com.brace.server.notification.repository.NotificationRepository;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectRoleRepository projectRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("create application saves progress application and notification")
    void createApplicationSavesProgressApplicationAndNotification() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        authenticate(fixture.applicant);

        ApplicationId response = applicationService.createApplication(fixture.project.getId(), new Message("backend", "지원합니다."));

        Application application = applicationRepository.findById(response.applicationId()).orElseThrow();
        List<Notification> notifications = notificationRepository.findAll();
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.PROGRESS);
        assertThat(application.getUser().getId()).isEqualTo(fixture.applicant.getId());
        assertThat(application.getProject().getId()).isEqualTo(fixture.project.getId());
        assertThat(application.getRole().getId()).isEqualTo(fixture.role.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.getFirst().getType()).isEqualTo(NotificationType.NEW_APPLICANT);
        assertThat(notifications.getFirst().getUser().getId()).isEqualTo(fixture.owner.getId());
        assertThat(notifications.getFirst().getApplication().getId()).isEqualTo(response.applicationId());
    }

    @Test
    @DisplayName("create application rejects project owner")
    void createApplicationRejectsProjectOwner() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        authenticate(fixture.owner);

        assertProjectException(
                () -> applicationService.createApplication(fixture.project.getId(), new Message("backend", "지원합니다.")),
                ApplicationErrorCode.SELF_APPLICATION_NOT_ALLOWED
        );
    }

    @Test
    @DisplayName("create application rejects closed recruitment status")
    void createApplicationRejectsClosedRecruitmentStatus() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.COMPLETED, LocalDate.now().plusDays(1));
        authenticate(fixture.applicant);

        assertProjectException(
                () -> applicationService.createApplication(fixture.project.getId(), new Message("backend", "지원합니다.")),
                ApplicationErrorCode.RECRUITMENT_CLOSED
        );
    }

    @Test
    @DisplayName("create application rejects past deadline")
    void createApplicationRejectsPastDeadline() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().minusDays(1));
        authenticate(fixture.applicant);

        assertProjectException(
                () -> applicationService.createApplication(fixture.project.getId(), new Message("backend", "지원합니다.")),
                ApplicationErrorCode.DEADLINE_PASSED
        );
    }

    @Test
    @DisplayName("create application allows today deadline")
    void createApplicationAllowsTodayDeadline() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now());
        authenticate(fixture.applicant);

        ApplicationId response = applicationService.createApplication(fixture.project.getId(), new Message("backend", "지원합니다."));

        assertThat(response.applicationId()).isNotNull();
    }

    @Test
    @DisplayName("create application rejects exhausted recruit count")
    void createApplicationRejectsExhaustedRecruitCount() {
        ProjectFixture fixture = saveProjectFixture(0, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        authenticate(fixture.applicant);

        assertProjectException(
                () -> applicationService.createApplication(fixture.project.getId(), new Message("backend", "지원합니다.")),
                ApplicationErrorCode.RECRUIT_COUNT_EXHAUSTED
        );
    }

    @Test
    @DisplayName("create application rejects any existing application for same user project and role")
    void createApplicationRejectsAnyExistingApplicationForSameUserProjectAndRole() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application existingApplication = saveApplication(fixture.applicant, fixture.project, fixture.role, "기존 지원");
        existingApplication.fail();
        authenticate(fixture.applicant);

        assertProjectException(
                () -> applicationService.createApplication(fixture.project.getId(), new Message("backend", "재지원합니다.")),
                ApplicationErrorCode.DUPLICATE_APPLICATION
        );
    }

    @Test
    @DisplayName("create application rejects missing user")
    void createApplicationRejectsMissingUser() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        authenticate(Long.MAX_VALUE);

        assertProjectException(
                () -> applicationService.createApplication(fixture.project.getId(), new Message("backend", "지원합니다.")),
                ApplicationErrorCode.USER_NOT_FOUND
        );
    }

    @Test
    @DisplayName("create application rejects unknown project or role as project role not found")
    void createApplicationRejectsUnknownProjectOrRoleAsProjectRoleNotFound() {
        User applicant = saveUser(2L);
        authenticate(applicant);

        assertProjectException(
                () -> applicationService.createApplication(999L, new Message("backend", "지원합니다.")),
                ApplicationErrorCode.PROJECT_ROLE_NOT_FOUND
        );
    }

    @Test
    @DisplayName("get applications returns first page with next cursor")
    void getApplicationsReturnsFirstPageWithNextCursor() {
        ProjectFixture fixture = saveProjectFixture(5, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application first = saveApplication(fixture.applicant, fixture.project, fixture.role, "1");
        Application second = saveApplication(saveUser(3L), fixture.project, fixture.role, "2");
        Application third = saveApplication(saveUser(4L), fixture.project, fixture.role, "3");
        authenticate(fixture.owner);

        ApplicationSlice response = applicationService.getApplications(fixture.project.getId(), null, 2);

        assertThat(response.content()).extracting("applicationId")
                .containsExactly(third.getId(), second.getId());
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursorId()).isEqualTo(second.getId());
        assertThat(first.getId()).isLessThan(response.nextCursorId());
    }

    @Test
    @DisplayName("get applications returns next page with cursor")
    void getApplicationsReturnsNextPageWithCursor() {
        ProjectFixture fixture = saveProjectFixture(5, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application first = saveApplication(fixture.applicant, fixture.project, fixture.role, "1");
        Application second = saveApplication(saveUser(3L), fixture.project, fixture.role, "2");
        Application third = saveApplication(saveUser(4L), fixture.project, fixture.role, "3");
        authenticate(fixture.owner);

        ApplicationSlice response = applicationService.getApplications(fixture.project.getId(), second.getId(), 2);

        assertThat(response.content()).extracting("applicationId").containsExactly(first.getId());
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursorId()).isNull();
        assertThat(third.getId()).isGreaterThan(second.getId());
    }

    @Test
    @DisplayName("get applications includes every status")
    void getApplicationsIncludesEveryStatus() {
        ProjectFixture fixture = saveProjectFixture(5, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application progress = saveApplication(fixture.applicant, fixture.project, fixture.role, "progress");
        Application pass = saveApplication(saveUser(3L), fixture.project, fixture.role, "pass");
        pass.pass();
        Application fail = saveApplication(saveUser(4L), fixture.project, fixture.role, "fail");
        fail.fail();
        Application cancel = saveApplication(saveUser(5L), fixture.project, fixture.role, "cancel");
        cancel.cancel();
        authenticate(fixture.owner);

        ApplicationSlice response = applicationService.getApplications(fixture.project.getId(), null, 10);

        assertThat(response.content()).extracting("applicationId")
                .containsExactly(cancel.getId(), fail.getId(), pass.getId(), progress.getId());
        assertThat(response.content()).extracting("status")
                .containsExactly("CANCEL", "FAIL", "PASS", "PROGRESS");
    }

    @Test
    @DisplayName("get applications rejects invalid size")
    void getApplicationsRejectsInvalidSize() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        authenticate(fixture.owner);

        assertProjectException(
                () -> applicationService.getApplications(fixture.project.getId(), null, 0),
                ApplicationErrorCode.INVALID_SIZE
        );
    }

    @Test
    @DisplayName("get applications rejects null size")
    void getApplicationsRejectsNullSize() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        authenticate(fixture.owner);

        assertProjectException(
                () -> applicationService.getApplications(fixture.project.getId(), null, null),
                ApplicationErrorCode.INVALID_SIZE
        );
    }

    @Test
    @DisplayName("get applications rejects non owner")
    void getApplicationsRejectsNonOwner() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        authenticate(fixture.applicant);

        assertProjectException(
                () -> applicationService.getApplications(fixture.project.getId(), null, 10),
                ApplicationErrorCode.PROJECT_OWNER_ONLY_FOR_READ
        );
    }

    @Test
    @DisplayName("get applications rejects missing project")
    void getApplicationsRejectsMissingProject() {
        authenticate(Long.MAX_VALUE);

        assertProjectException(
                () -> applicationService.getApplications(999L, null, 10),
                ApplicationErrorCode.PROJECT_NOT_FOUND
        );
    }

    @Test
    @DisplayName("cancel application cancels progress application")
    void cancelApplicationCancelsProgressApplication() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application application = saveApplication(fixture.applicant, fixture.project, fixture.role, "지원합니다.");
        authenticate(fixture.applicant);

        com.brace.server.application.dto.ApplicationResDto.ApplicationStatus response =
                applicationService.cancelApplication(application.getId());

        assertThat(response.applicationId()).isEqualTo(application.getId());
        assertThat(response.status()).isEqualTo("CANCEL");
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.CANCEL);
    }

    @Test
    @DisplayName("cancel application rejects non applicant")
    void cancelApplicationRejectsNonApplicant() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application application = saveApplication(fixture.applicant, fixture.project, fixture.role, "지원합니다.");
        authenticate(fixture.owner);

        assertProjectException(
                () -> applicationService.cancelApplication(application.getId()),
                ApplicationErrorCode.APPLICANT_ONLY_FOR_CANCEL
        );
    }

    @Test
    @DisplayName("cancel application rejects missing application")
    void cancelApplicationRejectsMissingApplication() {
        authenticate(Long.MAX_VALUE);

        assertProjectException(
                () -> applicationService.cancelApplication(999L),
                ApplicationErrorCode.APPLICATION_NOT_FOUND
        );
    }

    @Test
    @DisplayName("cancel application rejects non progress statuses")
    void cancelApplicationRejectsNonProgressStatuses() {
        ProjectFixture fixture = saveProjectFixture(10L, 3, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application passed = saveApplication(fixture.applicant, fixture.project, fixture.role, "pass");
        passed.pass();
        ProjectFixture failedFixture = saveProjectFixture(20L, 3, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application failed = saveApplication(failedFixture.applicant, failedFixture.project, failedFixture.role, "fail");
        failed.fail();
        ProjectFixture canceledFixture = saveProjectFixture(30L, 3, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application canceled = saveApplication(canceledFixture.applicant, canceledFixture.project, canceledFixture.role, "cancel");
        canceled.cancel();
        authenticate(fixture.applicant);

        assertProjectException(
                () -> applicationService.cancelApplication(passed.getId()),
                ApplicationErrorCode.APPLICATION_NOT_CANCELABLE
        );
        assertProjectException(
                () -> applicationService.cancelApplication(failed.getId()),
                ApplicationErrorCode.APPLICATION_NOT_CANCELABLE
        );
        assertProjectException(
                () -> applicationService.cancelApplication(canceled.getId()),
                ApplicationErrorCode.APPLICATION_NOT_CANCELABLE
        );
    }

    @Test
    @DisplayName("update application passes application and decreases recruit count")
    void updateApplicationPassesApplicationAndDecreasesRecruitCount() {
        ProjectFixture fixture = saveProjectFixture(2, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application application = saveApplication(fixture.applicant, fixture.project, fixture.role, "지원합니다.");
        authenticate(fixture.owner);

        ApplicationResult response = applicationService.updateApplication(application.getId(), new Status("PASS"));
        flushAndClear();
        Application updatedApplication = applicationRepository.findById(response.applicationId()).orElseThrow();

        assertThat(response.applicationId()).isEqualTo(application.getId());
        assertThat(response.status()).isEqualTo("PASS");
        assertThat(response.applicantEmail()).isEqualTo("user2@example.com");
        assertThat(updatedApplication.getStatus()).isEqualTo(ApplicationStatus.PASS);
        assertThat(projectRoleRepository.findById(fixture.projectRole.getId()).orElseThrow().getRecruitCount()).isEqualTo(1);
        assertThat(notificationRepository.findAll()).extracting(Notification::getType)
                .containsExactly(NotificationType.APPLICATION_RESULT);
    }

    @Test
    @DisplayName("update application fails application without changing recruit count")
    void updateApplicationFailsApplicationWithoutChangingRecruitCount() {
        ProjectFixture fixture = saveProjectFixture(2, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application application = saveApplication(fixture.applicant, fixture.project, fixture.role, "지원합니다.");
        authenticate(fixture.owner);

        ApplicationResult response = applicationService.updateApplication(application.getId(), new Status("FAIL"));
        flushAndClear();
        Application updatedApplication = applicationRepository.findById(response.applicationId()).orElseThrow();

        assertThat(response.applicationId()).isEqualTo(application.getId());
        assertThat(response.status()).isEqualTo("FAIL");
        assertThat(response.applicantEmail()).isNull();
        assertThat(updatedApplication.getStatus()).isEqualTo(ApplicationStatus.FAIL);
        assertThat(projectRoleRepository.findById(fixture.projectRole.getId()).orElseThrow().getRecruitCount()).isEqualTo(2);
        assertThat(notificationRepository.findAll()).extracting(Notification::getType)
                .containsExactly(NotificationType.APPLICATION_RESULT);
    }

    @Test
    @DisplayName("update application rejects non owner")
    void updateApplicationRejectsNonOwner() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application application = saveApplication(fixture.applicant, fixture.project, fixture.role, "지원합니다.");
        authenticate(fixture.applicant);

        assertProjectException(
                () -> applicationService.updateApplication(application.getId(), new Status("PASS")),
                ApplicationErrorCode.PROJECT_OWNER_ONLY_FOR_UPDATE
        );
    }

    @Test
    @DisplayName("update application rejects missing application")
    void updateApplicationRejectsMissingApplication() {
        authenticate(Long.MAX_VALUE);

        assertProjectException(
                () -> applicationService.updateApplication(999L, new Status("PASS")),
                ApplicationErrorCode.APPLICATION_NOT_FOUND
        );
    }

    @Test
    @DisplayName("update application rejects already processed application")
    void updateApplicationRejectsAlreadyProcessedApplication() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application application = saveApplication(fixture.applicant, fixture.project, fixture.role, "지원합니다.");
        application.cancel();
        authenticate(fixture.owner);

        assertProjectException(
                () -> applicationService.updateApplication(application.getId(), new Status("PASS")),
                ApplicationErrorCode.APPLICATION_ALREADY_PROCESSED
        );
    }

    @Test
    @DisplayName("update application rejects pass when recruit count is zero")
    void updateApplicationRejectsPassWhenRecruitCountIsZero() {
        ProjectFixture fixture = saveProjectFixture(0, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application application = saveApplication(fixture.applicant, fixture.project, fixture.role, "지원합니다.");
        authenticate(fixture.owner);

        assertProjectException(
                () -> applicationService.updateApplication(application.getId(), new Status("PASS")),
                ApplicationErrorCode.RECRUIT_COUNT_EXHAUSTED
        );
    }

    @Test
    @DisplayName("update application auto fails remaining progress applications when recruitment closes")
    void updateApplicationAutoFailsRemainingProgressApplicationsWhenRecruitmentCloses() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application accepted = saveApplication(fixture.applicant, fixture.project, fixture.role, "accepted");
        Application remaining = saveApplication(saveUser(3L), fixture.project, fixture.role, "remaining");
        Application otherProjectApplication = saveApplication(saveUser(4L), saveOtherProject(fixture.owner), fixture.role, "other project");
        Role otherRole = roleRepository.save(Role.builder().id(20L).name("frontend").build());
        projectRoleRepository.save(ProjectRole.builder().project(fixture.project).role(otherRole).recruitCount(1).build());
        Application otherRoleApplication = saveApplication(saveUser(5L), fixture.project, otherRole, "other role");
        authenticate(fixture.owner);

        applicationService.updateApplication(accepted.getId(), new Status("PASS"));
        flushAndClear();

        assertThat(applicationRepository.findById(accepted.getId()).orElseThrow().getStatus()).isEqualTo(ApplicationStatus.PASS);
        assertThat(applicationRepository.findById(remaining.getId()).orElseThrow().getStatus()).isEqualTo(ApplicationStatus.FAIL);
        assertThat(applicationRepository.findById(otherProjectApplication.getId()).orElseThrow().getStatus()).isEqualTo(ApplicationStatus.PROGRESS);
        assertThat(applicationRepository.findById(otherRoleApplication.getId()).orElseThrow().getStatus()).isEqualTo(ApplicationStatus.PROGRESS);
        assertThat(notificationRepository.findAll()).hasSize(2);
        assertThat(notificationRepository.findAll()).extracting(notification -> notification.getUser().getId())
                .containsExactlyInAnyOrder(fixture.applicant.getId(), remaining.getUser().getId());
    }

    @Test
    @DisplayName("update application throws illegal argument exception for unknown status name")
    void updateApplicationThrowsIllegalArgumentExceptionForUnknownStatusName() {
        ProjectFixture fixture = saveProjectFixture(1, ProjectStatus.RECRUITING, LocalDate.now().plusDays(1));
        Application application = saveApplication(fixture.applicant, fixture.project, fixture.role, "지원합니다.");
        authenticate(fixture.owner);

        assertThatThrownBy(() -> applicationService.updateApplication(application.getId(), new Status("UNKNOWN")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ProjectFixture saveProjectFixture(Integer recruitCount, ProjectStatus status, LocalDate deadline) {
        return saveProjectFixture(10L, recruitCount, status, deadline);
    }

    private ProjectFixture saveProjectFixture(
            Long roleId,
            Integer recruitCount,
            ProjectStatus status,
            LocalDate deadline
    ) {
        User owner = saveUser(1L);
        User applicant = saveUser(2L);
        Role role = roleRepository.save(Role.builder().id(roleId).name("backend").build());
        Project project = projectRepository.save(Project.builder()
                .activityType(ActivityType.PERSONAL_PROJECT)
                .title("프로젝트")
                .description("설명")
                .projectName("brace")
                .projectUrl("https://example.com")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .deadline(deadline)
                .meetingType(MeetingType.ONLINE)
                .tags("java,spring")
                .status(status)
                .user(owner)
                .build());
        ProjectRole projectRole = projectRoleRepository.save(ProjectRole.builder()
                .project(project)
                .role(role)
                .recruitCount(recruitCount)
                .build());
        return new ProjectFixture(owner, applicant, role, project, projectRole);
    }

    private Project saveOtherProject(User owner) {
        return projectRepository.save(Project.builder()
                .activityType(ActivityType.PERSONAL_PROJECT)
                .title("다른 프로젝트")
                .description("설명")
                .projectName("other")
                .projectUrl("https://example.com/other")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .deadline(LocalDate.now().plusDays(1))
                .meetingType(MeetingType.ONLINE)
                .tags("java,spring")
                .status(ProjectStatus.RECRUITING)
                .user(owner)
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

    private User saveUser(Long id) {
        String email = "user" + id + "@example.com";
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = User.builder()
                    .email(email)
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
        });
    }

    private void authenticate(User user) {
        authenticate(user.getId());
    }

    private void authenticate(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, List.of())
        );
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void assertProjectException(Runnable action, ApplicationErrorCode expectedErrorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(expectedErrorCode);
    }

    private record ProjectFixture(
            User owner,
            User applicant,
            Role role,
            Project project,
            ProjectRole projectRole
    ) {
    }
}
