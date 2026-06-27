package com.brace.server.project.controller;

import com.brace.server.global.apiPayload.ApiResponse;
import com.brace.server.project.dto.request.ProjectCreateRequest;
import com.brace.server.project.dto.request.ProjectUpdateRequest;
import com.brace.server.project.dto.response.PageResponse;
import com.brace.server.project.dto.response.ProjectCreateResponse;
import com.brace.server.project.dto.response.ProjectDetailResponse;
import com.brace.server.project.dto.response.ProjectSummaryResponse;
import com.brace.server.project.entity.ActivityType;
import com.brace.server.project.entity.MeetingType;
import com.brace.server.project.service.ProjectService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectCreateResponse>> createProject(@RequestBody ProjectCreateRequest request) {
        Long projectId = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(new ProjectCreateResponse(projectId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectSummaryResponse>>> getProjects(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) ActivityType activityType,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) MeetingType meetingType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProjectSummaryResponse> result = projectService.getProjects(
                sort, activityType, roleId, meetingType, keyword, tags, page, size);
        return ResponseEntity.ok(ApiResponse.success(new PageResponse<>(result)));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProject(projectId)));
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> updateProject(
            @PathVariable Long projectId,
            @RequestBody ProjectUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(projectService.updateProject(projectId, request)));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{projectId}/bookmark")
    public ResponseEntity<ApiResponse<Void>> addBookmark(@PathVariable Long projectId) {
        projectService.addBookmark(projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @DeleteMapping("/{projectId}/bookmark")
    public ResponseEntity<ApiResponse<Void>> removeBookmark(@PathVariable Long projectId) {
        projectService.removeBookmark(projectId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
