package com.brace.server.notification.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public class NotificationReqDto {

    public record NotificationSearch(
            @Positive(message = "커서 ID는 1 이상이어야 합니다.")
            Long cursorId,

            @Min(value = 1, message = "조회 개수는 1 이상 100 이하이어야 합니다.")
            @Max(value = 100, message = "조회 개수는 1 이상 100 이하이어야 합니다.")
            Integer size,

            Boolean isRead
    ) {
        private static final int DEFAULT_SIZE = 20;

        public NotificationSearch {
            size = size == null ? DEFAULT_SIZE : size;
        }
    }
}
