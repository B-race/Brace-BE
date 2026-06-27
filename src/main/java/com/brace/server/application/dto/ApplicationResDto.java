package com.brace.server.application.dto;

import com.brace.server.application.entity.Application;
import com.brace.server.project.entity.Project;
import com.brace.server.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;

public class ApplicationResDto {

    public record ApplicationId(
            Long applicationId
    ) {
    }

    public record ApplicationStatus(
            Long applicationId,
            String status
    ) {
    }

    public record ApplicationResult(
            Long applicationId,
            String status,
            String applicantEmail
    ) {
    }

    public record ApplicationSlice(
            List<ApplicationSummary> content,
            Integer size,
            Long nextCursorId,
            Boolean hasNext
    ) {
    }

    public record ApplicationSummary(
            Long applicationId,
            Long projectId,
            String projectTitle,
            String projectStatus,
            String status,
            LocalDateTime createdAt,
            Applicant applicant
    ) {
        public static ApplicationSummary from(Application application) {
            return from(application, application.getProject());
        }

        public static ApplicationSummary from(Application application, Project project) {
            return new ApplicationSummary(
                    application.getId(),
                    project.getId(),
                    project.getTitle(),
                    project.getStatus().name(),
                    application.getStatus().name(),
                    application.getCreatedAt(),
                    Applicant.from(application)
            );
        }
    }

    public record Applicant(
            Long userId,
            Long roleId,
            String name,
            String roleName,
            String techTags,
            String message,
            String portfolioUrl,
            String profileImageUrl,
            String introduction
    ) {
        public static Applicant from(Application application) {
            User applicant = application.getUser();

            return new Applicant(
                    applicant.getId(),
                    application.getRole().getId(),
                    applicant.getName(),
                    application.getRole().getName(),
                    applicant.getTechTags(),
                    application.getMessage(),
                    applicant.getPortfolioUrl(),
                    applicant.getProfileImageUrl(),
                    applicant.getIntroduction()
            );
        }
    }
}
