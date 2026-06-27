package com.brace.server.project.dto.response;

import com.brace.server.project.entity.ActivityType;
import com.brace.server.project.entity.MeetingType;
import com.brace.server.project.entity.ProjectStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectSummaryResponse {
    private Long projectId;
    private String title;
    private String description;
    private ActivityType activityType;
    private MeetingType meetingType;
    private LocalDate deadline;
    private ProjectStatus status;
    private String tags;
    private String writerName;
}
