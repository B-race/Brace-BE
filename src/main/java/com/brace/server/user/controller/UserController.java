package com.brace.server.user.controller;

import com.brace.server.auth.security.CustomUserDetails;
import com.brace.server.global.apiPayload.ApiResponse;
import com.brace.server.global.code.BaseSuccessCode;
import com.brace.server.user.dto.UserReqDTO;
import com.brace.server.user.dto.UserResDTO;
import com.brace.server.user.exception.code.UserSuccessCode;
import com.brace.server.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PatchMapping("/users/me/onboarding")
    public ApiResponse<UserResDTO.profileOnboarding> profileOnboarding(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserReqDTO.profileOnboarding dto
    ) {
        BaseSuccessCode code = UserSuccessCode.SUCCESS_COMPLETE_ONBOARDING;
        return ApiResponse.success(code, userService.profileOnboarding(userDetails.getUserId(), dto));
    }

    @GetMapping("/users/me")
    public ApiResponse<UserResDTO.myPage> myPage(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BaseSuccessCode code = UserSuccessCode.MYPAGE_OK;
        return ApiResponse.success(code, userService.myPage(userDetails.getUserId()));
    }

    @PatchMapping("/users/me")
    public ApiResponse<UserResDTO.updateProfile> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserReqDTO.updateProfile dto
    ) {
        BaseSuccessCode code = UserSuccessCode.UPDATE_PROFILE_OK;
        return ApiResponse.success(code, userService.updateProfile(userDetails.getUserId(), dto));
    }
}
