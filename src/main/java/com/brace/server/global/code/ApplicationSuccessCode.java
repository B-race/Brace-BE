package com.brace.server.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApplicationSuccessCode implements BaseSuccessCode {

    CREATED(HttpStatus.CREATED, "application201", "지원이 완료되었습니다."),
    READ_APPLICATIONS(HttpStatus.OK, "application200", "지원자 목록 조회가 완료되었습니다."),
    UPDATED(HttpStatus.OK, "application200", "지원 상태가 변경되었습니다."),
    CANCELED(HttpStatus.OK, "application200", "지원이 취소되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
