package com.brace.server.application.controller;

import com.brace.server.application.dto.ApplicationReqDto.Message;
import com.brace.server.application.dto.ApplicationReqDto.Status;
import com.brace.server.application.dto.ApplicationResDto.ApplicationId;
import com.brace.server.application.dto.ApplicationResDto.ApplicationResult;
import com.brace.server.application.dto.ApplicationResDto.ApplicationSlice;
import com.brace.server.application.dto.ApplicationResDto.ApplicationStatus;
import com.brace.server.application.service.ApplicationService;
import com.brace.server.global.apiPayload.ApiResponse;
import com.brace.server.global.code.ApplicationSuccessCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/projects/{projectId}/applications")
    public ResponseEntity<ApiResponse<ApplicationId>> createApplication(
            @PathVariable Long projectId,
            @Valid @RequestBody Message request
    ) {
        ApplicationId response = applicationService.createApplication(projectId, request);
        return ResponseEntity
                .status(ApplicationSuccessCode.CREATED.getHttpStatus())
                .body(ApiResponse.success(ApplicationSuccessCode.CREATED, response));
    }

    @GetMapping("/projects/{projectId}/applications")
    public ResponseEntity<ApiResponse<ApplicationSlice>> getApplications(
            @Positive @PathVariable Long projectId,
            @Positive @RequestParam(required = false) Long cursorId,
            @Min(1) @RequestParam(defaultValue = "20") Integer size
    ) {
        ApplicationSlice response = applicationService.getApplications(projectId, cursorId, size);
        return ResponseEntity
                .status(ApplicationSuccessCode.READ_APPLICATIONS.getHttpStatus())
                .body(ApiResponse.success(ApplicationSuccessCode.READ_APPLICATIONS, response));
    }

    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<ApiResponse<ApplicationStatus>> cancelApplication(
            @PathVariable Long applicationId
    ) {
        ApplicationStatus response = applicationService.cancelApplication(applicationId);
        return ResponseEntity
                .status(ApplicationSuccessCode.CANCELED.getHttpStatus())
                .body(ApiResponse.success(ApplicationSuccessCode.CANCELED, response));
    }

    @PatchMapping("/applications/{applicationId}")
    public ResponseEntity<ApiResponse<ApplicationResult>> updateApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody Status request
    ) {
        ApplicationResult response = applicationService.updateApplication(applicationId, request);
        return ResponseEntity
                .status(ApplicationSuccessCode.UPDATED.getHttpStatus())
                .body(ApiResponse.success(ApplicationSuccessCode.UPDATED, response));
    }
}
