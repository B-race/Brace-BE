package com.brace.server.project.repository;

import com.brace.server.TestcontainersConfiguration;
import com.brace.server.global.config.JpaAuditingConfig;
import com.brace.server.project.entity.*;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import({TestcontainersConfiguration.class, JpaAuditingConfig.class})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectRoleRepositoryTest {

    @Autowired
    private ProjectRoleRepository projectRoleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("find by project id and role name returns project role with project owner and role")
    void findByProjectIdAndRoleNameReturnsProjectRoleWithProjectOwnerAndRole() {
        ProjectFixture fixture = saveProjectFixture(2);
        flushAndClear();

        ProjectRole result = projectRoleRepository.findByProjectIdAndRoleName(fixture.project.getId(), "backend")
                .orElseThrow();

        assertThat(result.getProject().getId()).isEqualTo(fixture.project.getId());
        assertThat(result.getProject().getUser().getId()).isEqualTo(fixture.owner.getId());
        assertThat(result.getRole().getName()).isEqualTo("backend");
    }

    @Test
    @DisplayName("decrease recruit count if available decreases when count is positive")
    void decreaseRecruitCountIfAvailableDecreasesWhenCountIsPositive() {
        ProjectFixture fixture = saveProjectFixture(1);
        flushAndClear();

        int updated = projectRoleRepository.decreaseRecruitCountIfAvailable(
                fixture.project.getId(),
                fixture.role.getId()
        );
        flushAndClear();

        assertThat(updated).isEqualTo(1);
        assertThat(projectRoleRepository.findById(fixture.projectRole.getId()).orElseThrow().getRecruitCount())
                .isZero();
    }

    @Test
    @DisplayName("decrease recruit count if available does nothing when count is zero")
    void decreaseRecruitCountIfAvailableDoesNothingWhenCountIsZero() {
        ProjectFixture fixture = saveProjectFixture(0);
        flushAndClear();

        int updated = projectRoleRepository.decreaseRecruitCountIfAvailable(
                fixture.project.getId(),
                fixture.role.getId()
        );
        flushAndClear();

        assertThat(updated).isZero();
        assertThat(projectRoleRepository.findById(fixture.projectRole.getId()).orElseThrow().getRecruitCount())
                .isZero();
    }

    @Test
    @DisplayName("decrease recruit count if available does nothing for different project or role")
    void decreaseRecruitCountIfAvailableDoesNothingForDifferentProjectOrRole() {
        ProjectFixture fixture = saveProjectFixture(1);
        flushAndClear();

        int differentProject = projectRoleRepository.decreaseRecruitCountIfAvailable(999L, fixture.role.getId());
        int differentRole = projectRoleRepository.decreaseRecruitCountIfAvailable(fixture.project.getId(), 999L);
        flushAndClear();

        assertThat(differentProject).isZero();
        assertThat(differentRole).isZero();
        assertThat(projectRoleRepository.findById(fixture.projectRole.getId()).orElseThrow().getRecruitCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("exists by project id and role id and recruit count checks current count")
    void existsByProjectIdAndRoleIdAndRecruitCountChecksCurrentCount() {
        ProjectFixture fixture = saveProjectFixture(0);
        flushAndClear();

        boolean existsWithZero = projectRoleRepository.existsByProjectIdAndRoleIdAndRecruitCount(
                fixture.project.getId(),
                fixture.role.getId(),
                0
        );
        boolean existsWithOne = projectRoleRepository.existsByProjectIdAndRoleIdAndRecruitCount(
                fixture.project.getId(),
                fixture.role.getId(),
                1
        );

        assertThat(existsWithZero).isTrue();
        assertThat(existsWithOne).isFalse();
    }

    private ProjectFixture saveProjectFixture(Integer recruitCount) {
        User owner = User.builder()
                .email("owner@example.com")
                .password("password")
                .socialProvider(SocialProvider.NONE)
                .socialId("owner")
                .name("owner")
                .role("개발자")
                .profileImageUrl("https://example.com/owner.png")
                .participationType(ParticipationType.BOTH)
                .introduction("소개")
                .portfolioUrl("https://example.com/portfolio")
                .profileCompleted(true)
                .build();
        owner.profileOnboarding(
                "https://example.com/owner.png",
                "개발자",
                List.of(SkillTag.JAVA, SkillTag.SPRING),
                ParticipationType.BOTH,
                "소개",
                "https://example.com/portfolio"
        );
        owner = userRepository.save(owner);
        Role role = roleRepository.save(Role.builder().id(10L).name("backend").build());
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
        ProjectRole projectRole = projectRoleRepository.save(ProjectRole.builder()
                .project(project)
                .role(role)
                .recruitCount(recruitCount)
                .build());
        return new ProjectFixture(owner, role, project, projectRole);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private record ProjectFixture(User owner, Role role, Project project, ProjectRole projectRole) {
    }
}
