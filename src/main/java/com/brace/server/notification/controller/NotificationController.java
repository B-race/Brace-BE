package com.brace.server.notification.controller;

import com.brace.server.auth.security.CustomUserDetails;
import com.brace.server.global.apiPayload.ApiResponse;
import com.brace.server.global.code.NotificationErrorCode;
import com.brace.server.global.code.NotificationSuccessCode;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.notification.dto.NotificationReqDto.NotificationSearch;
import com.brace.server.notification.dto.NotificationResDto.NotificationSlice;
import com.brace.server.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<NotificationSlice>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute NotificationSearch request
    ) {
        NotificationSlice response = notificationService.getNotifications(
                getCurrentUserId(userDetails), request);
        return ResponseEntity
                .status(NotificationSuccessCode.READ_NOTIFICATIONS.getHttpStatus())
                .body(ApiResponse.success(NotificationSuccessCode.READ_NOTIFICATIONS, response));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(getCurrentUserId(userDetails), notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/notifications/read")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.markAllAsRead(getCurrentUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new ProjectException(NotificationErrorCode.UNAUTHORIZED);
        }

        return userDetails.getUserId();
    }
}
