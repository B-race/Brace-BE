package com.brace.server.notification.service;

import com.brace.server.global.code.NotificationErrorCode;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.notification.dto.NotificationReqDto.NotificationSearch;
import com.brace.server.notification.dto.NotificationResDto.NotificationSlice;
import com.brace.server.notification.entity.Notification;
import com.brace.server.notification.repository.NotificationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // 커서 기반으로 알림 목록을 조회하고 다음 페이지 여부를 계산한다.
    public NotificationSlice getNotifications(Long userId, @Valid NotificationSearch request) {
        int size = request.size();

        // 요청 개수보다 1개 더 조회해 다음 페이지 존재 여부를 판단한다.
        List<Notification> notifications = notificationRepository.findByUserIdWithCursor(
                userId,
                request.cursorId(),
                request.isRead(),
                PageRequest.of(0, size + 1)
        );

        return NotificationSlice.from(notifications, size);
    }

    @Transactional
    // 알림 소유자만 단일 알림을 읽음 처리할 수 있다.
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdWithUser(notificationId)
                .orElseThrow(() -> new ProjectException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        // 다른 사용자의 알림 접근을 차단한다.
        if (!notification.getUser().getId().equals(userId)) {
            throw new ProjectException(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        notification.markAsRead();
    }

    @Transactional
    // 사용자의 모든 미읽음 알림을 한 번에 읽음 처리한다.
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}
