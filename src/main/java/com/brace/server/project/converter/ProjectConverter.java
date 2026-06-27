package com.brace.server.project.converter;

import com.brace.server.project.dto.request.ProjectCreateRequest;
import com.brace.server.project.dto.response.ProjectDetailResponse;
import com.brace.server.project.dto.response.ProjectSummaryResponse;
import com.brace.server.project.dto.response.RecruitmentStatusResponse;
import com.brace.server.project.entity.Project;
import com.brace.server.project.entity.ProjectRole;
import com.brace.server.project.entity.ProjectStatus;
import com.brace.server.user.entity.User;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectConverter {

    private ProjectConverter() {}

    public static Project toEntity(ProjectCreateRequest request, User user) {
        return Project.builder()
                .activityType(request.getActivityType())
                .title(request.getTitle())
                .description(request.getDescription())
                .projectName(request.getProjectName())
                .projectUrl(request.getProjectUrl())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .deadline(request.getDeadline())
                .meetingType(request.getMeetingType())
                .tags(request.getTags())
                .status(ProjectStatus.RECRUITING)
                .user(user)
                .build();
    }

    public static ProjectSummaryResponse toSummaryResponse(Project project) {
        return ProjectSummaryResponse.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .activityType(project.getActivityType())
                .meetingType(project.getMeetingType())
                .deadline(project.getDeadline())
                .status(project.getStatus())
                .tags(project.getTags())
                .writerName(project.getUser().getName())
                .build();
    }

    public static ProjectDetailResponse toDetailResponse(Project project, List<RecruitmentStatusResponse> recruitmentStatus) {
        return ProjectDetailResponse.builder()
                .projectId(project.getId())
                .activityType(project.getActivityType())
                .title(project.getTitle())
                .description(project.getDescription())
                .projectName(project.getProjectName())
                .projectUrl(project.getProjectUrl())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .deadline(project.getDeadline())
                .meetingType(project.getMeetingType())
                .tags(project.getTags())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .writerName(project.getUser().getName())
                .writerRole(project.getUser().getRole())
                .writerIntroduction(project.getUser().getIntroduction())
                .writerProfileImageUrl(project.getUser().getProfileImageUrl())
                .recruitmentStatus(recruitmentStatus)
                .build();
    }

    public static List<RecruitmentStatusResponse> toRecruitmentStatusResponses(
            List<ProjectRole> projectRoles,
            List<Object[]> passCounts
    ) {
        Map<Long, Long> countByRoleId = passCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return projectRoles.stream()
                .map(pr -> RecruitmentStatusResponse.builder()
                        .roleId(pr.getRole().getId())
                        .roleName(pr.getRole().getName())
                        .recruitCount(pr.getRecruitCount())
                        .currentCount(countByRoleId.getOrDefault(pr.getRole().getId(), 0L))
                        .build())
                .toList();
    }
}
