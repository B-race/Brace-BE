package com.brace.server.auth.exception.code;

import com.brace.server.global.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

    SUCCESS_SIGNUP(HttpStatus.CREATED,
            "MEMBER201_1",
            "회원가입에 성공했습니다."),

    SUCCESS_LOGIN(HttpStatus.OK,
            "AUTH200_1",
            "로그인에 성공했습니다."),

    SUCCESS_REISSUE(HttpStatus.OK,
            "AUTH200_2",
            "토큰 재발급에 성공했습니다."),

    SUCCESS_LOGOUT(HttpStatus.OK,
            "AUTH200_4",
            "로그아웃에 성공했습니다."),;



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
