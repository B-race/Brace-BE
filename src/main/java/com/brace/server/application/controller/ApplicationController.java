package com.brace.server.application.controller;

import com.brace.server.application.dto.ApplicationReqDto.Message;
import com.brace.server.application.dto.ApplicationReqDto.Status;
import com.brace.server.application.dto.ApplicationResDto.ApplicationId;
import com.brace.server.application.dto.ApplicationResDto.ApplicationResult;
import com.brace.server.application.dto.ApplicationResDto.ApplicationSlice;
import com.brace.server.application.dto.ApplicationResDto.ApplicationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class ApplicationController {

    @PostMapping("/projects/{projectId}/applications")
    public ResponseEntity<ApplicationId> createApplication(
            @PathVariable Long projectId,
            @Valid @RequestBody Message request
    ) {
        return null;
    }

    @GetMapping("/projects/{projectId}/applications")
    public ResponseEntity<ApplicationSlice> getApplications(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return null;
    }

    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<ApplicationStatus> cancelApplication(
            @PathVariable Long applicationId
    ) {
        return null;
    }

    @PatchMapping("/applications/{applicationId}")
    public ResponseEntity<ApplicationResult> updateApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody Status request
    ) {
        return null;
    }
}
