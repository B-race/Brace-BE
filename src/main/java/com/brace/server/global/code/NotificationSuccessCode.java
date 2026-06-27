package com.brace.server.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationSuccessCode implements BaseSuccessCode {

    READ_NOTIFICATIONS(HttpStatus.OK, "notification200", "알림 목록 조회가 완료되었습니다."),
    MARKED_AS_READ(HttpStatus.NO_CONTENT, "notification204", "알림 읽음 처리가 완료되었습니다."),
    MARKED_ALL_AS_READ(HttpStatus.NO_CONTENT, "notification204", "모든 알림 읽음 처리가 완료되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
