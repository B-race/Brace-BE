package com.brace.server.application.repository;

import com.brace.server.TestcontainersConfiguration;
import com.brace.server.application.entity.Application;
import com.brace.server.application.entity.ApplicationStatus;
import com.brace.server.global.config.JpaAuditingConfig;
import com.brace.server.project.entity.*;
import com.brace.server.project.repository.ProjectRepository;
import com.brace.server.project.repository.ProjectRoleRepository;
import com.brace.server.user.entity.ParticipationType;
import com.brace.server.user.entity.Role;
import com.brace.server.user.entity.SocialProvider;
import com.brace.server.user.entity.User;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({TestcontainersConfiguration.class, JpaAuditingConfig.class})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;

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
    @DisplayName("find by project id with cursor returns descending applications without cursor")
    void findByProjectIdWithCursorReturnsDescendingApplicationsWithoutCursor() {
        ProjectFixture fixture = saveProjectFixture();
        Application first = saveApplication(fixture.applicant, fixture.project, fixture.role, "1");
        Application second = saveApplication(saveUser(3L), fixture.project, fixture.role, "2");
        Application third = saveApplication(saveUser(4L), fixture.project, fixture.role, "3");
        saveApplication(saveUser(5L), saveOtherProject(2L, fixture.owner), fixture.role, "other");
        flushAndClear();

        List<Application> result = applicationRepository.findByProjectIdWithCursor(
                fixture.project.getId(),
                null,
                PageRequest.of(0, 2)
        );

        assertThat(result).extracting(Application::getId).containsExactly(third.getId(), second.getId());
        assertThat(first.getId()).isLessThan(second.getId());
    }

    @Test
    @DisplayName("find by project id with cursor applies cursor condition")
    void findByProjectIdWithCursorAppliesCursorCondition() {
        ProjectFixture fixture = saveProjectFixture();
        Application first = saveApplication(fixture.applicant, fixture.project, fixture.role, "1");
        Application second = saveApplication(saveUser(3L), fixture.project, fixture.role, "2");
        Application third = saveApplication(saveUser(4L), fixture.project, fixture.role, "3");
        flushAndClear();

        List<Application> result = applicationRepository.findByProjectIdWithCursor(
                fixture.project.getId(),
                second.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result).extracting(Application::getId).containsExactly(first.getId());
        assertThat(third.getId()).isGreaterThan(second.getId());
    }

    @Test
    @DisplayName("exists by user id and project id and role id returns true for any status")
    void existsByUserIdAndProjectIdAndRoleIdReturnsTrueForAnyStatus() {
        ProjectFixture fixture = saveProjectFixture();
        Application application = saveApplication(fixture.applicant, fixture.project, fixture.role, "지원합니다.");
        application.cancel();
        flushAndClear();

        boolean exists = applicationRepository.existsByUserIdAndProjectIdAndRoleId(
                fixture.applicant.getId(),
                fixture.project.getId(),
                fixture.role.getId()
        );

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("unique constraint rejects same user project and role")
    void uniqueConstraintRejectsSameUserProjectAndRole() {
        ProjectFixture fixture = saveProjectFixture();
        saveApplication(fixture.applicant, fixture.project, fixture.role, "기존 지원");

        assertThatThrownBy(() -> {
            saveApplication(fixture.applicant, fixture.project, fixture.role, "중복 지원");
            flushAndClear();
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("find by id with user fetches application and user")
    void findByIdWithUserFetchesApplicationAndUser() {
        ProjectFixture fixture = saveProjectFixture();
        Application application = saveApplication(fixture.applicant, fixture.project, fixture.role, "지원합니다.");
        flushAndClear();

        Application result = applicationRepository.findByIdWithUser(application.getId()).orElseThrow();

        assertThat(result.getUser().getId()).isEqualTo(fixture.applicant.getId());
    }

    @Test
    @DisplayName("find by id with user project role fetches application graph")
    void findByIdWithUserProjectRoleFetchesApplicationGraph() {
        ProjectFixture fixture = saveProjectFixture();
        Application application = saveApplication(fixture.applicant, fixture.project, fixture.role, "지원합니다.");
        flushAndClear();

        Application result = applicationRepository.findByIdWithUserProjectRole(application.getId()).orElseThrow();

        assertThat(result.getUser().getId()).isEqualTo(fixture.applicant.getId());
        assertThat(result.getProject().getUser().getId()).isEqualTo(fixture.owner.getId());
        assertThat(result.getRole().getId()).isEqualTo(fixture.role.getId());
    }

    @Test
    @DisplayName("find by project id and role id and status except returns only remaining progress applications")
    void findByProjectIdAndRoleIdAndStatusExceptReturnsOnlyRemainingProgressApplications() {
        ProjectFixture fixture = saveProjectFixture();
        Application excluded = saveApplication(fixture.applicant, fixture.project, fixture.role, "excluded");
        Application remaining = saveApplication(saveUser(3L), fixture.project, fixture.role, "remaining");
        Application failed = saveApplication(saveUser(4L), fixture.project, fixture.role, "failed");
        failed.fail();
        saveApplication(saveUser(5L), saveOtherProject(2L, fixture.owner), fixture.role, "other project");
        Role otherRole = roleRepository.save(Role.builder().id(20L).name("frontend").build());
        projectRoleRepository.save(ProjectRole.builder().id(20L).project(fixture.project).role(otherRole).recruitCount(1).build());
        saveApplication(saveUser(6L), fixture.project, otherRole, "other role");
        flushAndClear();

        List<Application> result = applicationRepository.findByProjectIdAndRoleIdAndStatusExcept(
                fixture.project.getId(),
                fixture.role.getId(),
                ApplicationStatus.PROGRESS,
                excluded.getId()
        );

        assertThat(result).extracting(Application::getId).containsExactly(remaining.getId());
    }

    private ProjectFixture saveProjectFixture() {
        User owner = saveUser(1L);
        User applicant = saveUser(2L);
        Role role = roleRepository.save(Role.builder().id(10L).name("backend").build());
        Project project = projectRepository.save(Project.builder()
                .id(1L)
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
                .id(10L)
                .project(project)
                .role(role)
                .recruitCount(3)
                .build());
        return new ProjectFixture(owner, applicant, role, project);
    }

    private Project saveOtherProject(Long projectId, User owner) {
        return projectRepository.save(Project.builder()
                .id(projectId)
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
        return userRepository.findById(id).orElseGet(() -> userRepository.save(User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .password("password")
                .socialProvider(SocialProvider.NONE)
                .socialId("social-" + id)
                .name("user" + id)
                .role("개발자")
                .profileImageUrl("https://example.com/" + id + ".png")
                .techTags("java,spring")
                .participationType(ParticipationType.BOTH)
                .introduction("소개")
                .portfolioUrl("https://example.com/portfolio/" + id)
                .build()));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private record ProjectFixture(User owner, User applicant, Role role, Project project) {
    }
}
