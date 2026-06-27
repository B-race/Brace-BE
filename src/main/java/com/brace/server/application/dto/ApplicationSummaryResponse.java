package com.brace.server.application.dto;

import com.brace.server.application.entity.ApplicationStatus;
import com.brace.server.project.entity.ActivityType;
import com.brace.server.project.entity.ProjectStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationSummaryResponse {
    private Long applicationId;
    private Long projectId;
    private String projectTitle;
    private ActivityType activityType;
    private String roleName;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private ProjectStatus projectStatus;
}
