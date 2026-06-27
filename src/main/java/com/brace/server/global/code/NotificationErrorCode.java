package com.brace.server.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {

    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "커서 ID는 1 이상이어야 합니다."),
    INVALID_SIZE(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "조회 개수는 1 이상 100 이하이어야 합니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "유효하지 않은 토큰입니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FORBIDDEN", "알림에 접근할 수 없습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "알림을 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;
}
