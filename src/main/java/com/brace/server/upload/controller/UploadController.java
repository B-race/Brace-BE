package com.brace.server.upload.controller;

import com.brace.server.auth.security.CustomUserDetails;
import com.brace.server.global.apiPayload.ApiResponse;
import com.brace.server.global.code.GeneralSuccessCode;
import com.brace.server.upload.dto.UploadReqDTO;
import com.brace.server.upload.dto.UploadResDTO;
import com.brace.server.upload.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UploadController {
    private final UploadService uploadService;

    @PostMapping("/uploads/profile-image/presigned-url")
    public ApiResponse<UploadResDTO.presignedUrl> createProfileImagePresignedUrl(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UploadReqDTO.profileImagePresignedUrl dto
    ) {
        return ApiResponse.success(
                GeneralSuccessCode.OK,
                uploadService.createProfileImagePresignedUrl(userDetails.getUserId(), dto)
        );
    }
}
