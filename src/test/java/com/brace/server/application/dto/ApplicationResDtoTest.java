package com.brace.server.application.dto;

import com.brace.server.application.dto.ApplicationResDto.ApplicationSummary;
import com.brace.server.application.entity.Application;
import com.brace.server.project.entity.ActivityType;
import com.brace.server.project.entity.MeetingType;
import com.brace.server.project.entity.Project;
import com.brace.server.project.entity.ProjectStatus;
import com.brace.server.user.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationResDtoTest {

    @Test
    @DisplayName("application summary from maps application project applicant and role fields")
    void applicationSummaryFromMapsApplicationProjectApplicantAndRoleFields() {
        User owner = user(1L);
        User applicant = user(2L);
        Role role = Role.builder()
                .id(10L)
                .name("backend")
                .build();
        Project project = Project.builder()
                .id(100L)
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
                .build();
        Application application = Application.builder()
                .id(200L)
                .message("지원합니다.")
                .user(applicant)
                .project(project)
                .role(role)
                .build();

        ApplicationSummary summary = ApplicationSummary.from(application, project);

        assertThat(summary.applicationId()).isEqualTo(200L);
        assertThat(summary.projectId()).isEqualTo(100L);
        assertThat(summary.projectTitle()).isEqualTo("프로젝트");
        assertThat(summary.projectStatus()).isEqualTo("RECRUITING");
        assertThat(summary.status()).isEqualTo("PROGRESS");
        assertThat(summary.applicant().userId()).isEqualTo(2L);
        assertThat(summary.applicant().roleId()).isEqualTo(10L);
        assertThat(summary.applicant().roleName()).isEqualTo("backend");
        assertThat(summary.applicant().list()).containsExactly(SkillTag.JAVA, SkillTag.SPRING);
        assertThat(summary.applicant().message()).isEqualTo("지원합니다.");
    }

    private User user(Long id) {
        User user = User.builder()
                .id(id)
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
        return user;
    }
}
