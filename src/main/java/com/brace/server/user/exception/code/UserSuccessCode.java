package com.brace.server.user.exception.code;

import com.brace.server.global.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserSuccessCode implements BaseSuccessCode {

    SUCCESS_COMPLETE_ONBOARDING(
            HttpStatus.OK,
            "USER200_1",
            "온보딩을 완료했습니다."
    ),

    MYPAGE_OK(HttpStatus.OK,
            "USER200_1",
            "마이페이지 조회에 성공했습니다."),

    UPDATE_PROFILE_OK(HttpStatus.OK,
            "USER200_1",
            "개인 정보 수정에 성공했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
