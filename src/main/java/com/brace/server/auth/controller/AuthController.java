package com.brace.server.auth.controller;

import com.brace.server.auth.dto.AuthReqDTO;
import com.brace.server.auth.dto.AuthResDTO;
import com.brace.server.auth.exception.code.AuthSuccessCode;
import com.brace.server.auth.security.CustomUserDetails;
import com.brace.server.auth.service.AuthService;
import com.brace.server.global.apiPayload.ApiResponse;
import com.brace.server.global.code.BaseSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/signup")
    public ApiResponse<AuthResDTO.signUp> signup(
            @Valid @RequestBody AuthReqDTO.signUp dto
            ) {
        BaseSuccessCode code = AuthSuccessCode.SUCCESS_SIGNUP;
        return ApiResponse.success(code, authService.signUp(dto));
    }

    @PostMapping("/auth/login")
    public ApiResponse<AuthResDTO.login> login(
            @Valid @RequestBody AuthReqDTO.login dto
    ) {
        BaseSuccessCode code = AuthSuccessCode.SUCCESS_LOGIN;
        return ApiResponse.success(code, authService.login(dto));
    }

    @PostMapping("/auth/reissue")
    public ApiResponse<AuthResDTO.reissue> reissue(
            @Valid @RequestBody AuthReqDTO.reissue dto
    ) {
        BaseSuccessCode code = AuthSuccessCode.SUCCESS_REISSUE;
        return ApiResponse.success(code, authService.reissue(dto));
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) AuthReqDTO.logout dto
    ) {
        BaseSuccessCode code = AuthSuccessCode.SUCCESS_LOGOUT;
        authService.logout(userDetails.getUserId(), dto);
        return ApiResponse.success(code, null);
    }
}
