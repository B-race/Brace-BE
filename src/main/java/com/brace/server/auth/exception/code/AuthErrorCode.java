package com.brace.server.auth.exception.code;

import com.brace.server.global.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND,
            "USER404_1",
            "사용자를 찾을 수 없습니다."),

    ALREADY_EXISTS_EMAIL(HttpStatus.CONFLICT,
            "MEMBER_409_1",
            "이미 존재하는 이메일입니다."),

    NOT_SUPPORT_SOCIAL_PROVIDER(
            HttpStatus.BAD_REQUEST,
            "MEMBER_400_3",
            "지원하지 않는 소셜 로그인 제공자입니다."
    ),

    INVALID_PASSWORD(
            HttpStatus.BAD_REQUEST,
            "MEMBER_400_3",
            "비밀번호가 일치하지 않습니다."
    ),

    INVALID_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_401_1",
            "유효하지 않은 토큰입니다."
    ),;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;
}
