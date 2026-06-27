package com.brace.server.project.dto.response;

import com.brace.server.project.entity.ActivityType;
import com.brace.server.project.entity.MeetingType;
import com.brace.server.project.entity.ProjectStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectDetailResponse {
    private Long projectId;
    private ActivityType activityType;
    private String title;
    private String description;
    private String projectName;
    private String projectUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate deadline;
    private MeetingType meetingType;
    private String tags;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private String writerName;
    private String writerRole;
    private String writerIntroduction;
    private String writerProfileImageUrl;
    private List<RecruitmentStatusResponse> recruitmentStatus;
}
