package com.brace.server.user.controller;

import com.brace.server.application.dto.ApplicationSummaryResponse;
import com.brace.server.application.entity.ApplicationStatus;
import com.brace.server.auth.security.CustomUserDetails;
import com.brace.server.global.apiPayload.ApiResponse;
import com.brace.server.global.code.BaseSuccessCode;
import com.brace.server.project.dto.response.PageResponse;
import com.brace.server.project.dto.response.ProjectSummaryResponse;
import com.brace.server.user.dto.UserReqDTO;
import com.brace.server.user.dto.UserResDTO;
import com.brace.server.user.exception.code.UserSuccessCode;
import com.brace.server.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PatchMapping("/me/onboarding")
    public ApiResponse<UserResDTO.profileOnboarding> profileOnboarding(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserReqDTO.profileOnboarding dto
    ) {
        BaseSuccessCode code = UserSuccessCode.SUCCESS_COMPLETE_ONBOARDING;
        return ApiResponse.success(code, userService.profileOnboarding(userDetails.getUserId(), dto));
    }

    @GetMapping("/me/projects")
    public ResponseEntity<ApiResponse<PageResponse<ProjectSummaryResponse>>> getMyProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getMyProjects(userDetails.getUserId(), page, size)));
    }

    @GetMapping("/me/applications")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationSummaryResponse>>> getMyApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getMyApplications(userDetails.getUserId(), status, page, size)));
    }

    @GetMapping("/me/bookmarks")
    public ResponseEntity<ApiResponse<PageResponse<ProjectSummaryResponse>>> getMyBookmarks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getMyBookmarks(userDetails.getUserId(), page, size)));
    }

    @GetMapping("/me")
    public ApiResponse<UserResDTO.myPage> myPage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BaseSuccessCode code = UserSuccessCode.MYPAGE_OK;
        return ApiResponse.success(code, userService.myPage(userDetails.getUserId()));
    }

    @PatchMapping("/me")
    public ApiResponse<UserResDTO.updateProfile> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserReqDTO.updateProfile dto
    ) {
        BaseSuccessCode code = UserSuccessCode.UPDATE_PROFILE_OK;
        return ApiResponse.success(code, userService.updateProfile(userDetails.getUserId(), dto));
    }
}
