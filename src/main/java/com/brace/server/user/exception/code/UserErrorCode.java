package com.brace.server.user.exception.code;

import com.brace.server.global.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    ALREADY_COMPLETED_ONBOARDING(
            HttpStatus.CONFLICT,
            "USER_409_1",
            "이미 온보딩을 완료했습니다."
    ),
    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;
}
