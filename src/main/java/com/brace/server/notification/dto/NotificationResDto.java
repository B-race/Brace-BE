package com.brace.server.notification.dto;

import com.brace.server.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationResDto {

    public record NotificationSlice(
            List<NotificationResponse> content,
            Integer size,
            Long nextCursorId,
            Boolean hasNext
    ) {
        public static NotificationSlice from(List<Notification> notifications, int size) {
            boolean hasNext = notifications.size() > size;
            List<NotificationResponse> content = notifications.stream()
                    .limit(size)
                    .map(NotificationResponse::from)
                    .toList();
            Long nextCursorId = hasNext && !content.isEmpty()
                    ? content.getLast().id()
                    : null;

            return new NotificationSlice(content, size, nextCursorId, hasNext);
        }
    }

    public record NotificationResponse(
            Long id,
            String type,
            String content,
            Boolean isRead,
            Long applicationId,
            LocalDateTime createdAt
    ) {
        public static NotificationResponse from(Notification notification) {
            Long applicationId = notification.getApplication() == null
                    ? null
                    : notification.getApplication().getId();

            return new NotificationResponse(
                    notification.getId(),
                    notification.getType().name(),
                    notification.getContent(),
                    notification.getIsRead(),
                    applicationId,
                    notification.getCreatedAt()
            );
        }
    }
}
