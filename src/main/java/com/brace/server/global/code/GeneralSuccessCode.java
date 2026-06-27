package com.brace.server.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GeneralSuccessCode implements BaseSuccessCode {
    OK(HttpStatus.OK, "common200", "성공적으로 요청을 처리했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
