package com.brace.server.project.dto.request;

import com.brace.server.project.entity.ActivityType;
import com.brace.server.project.entity.MeetingType;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectCreateRequest {
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
    private List<RoleRequest> roles;
}
