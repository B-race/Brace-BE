package com.brace.server.global.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApplicationErrorCode implements BaseErrorCode {

    INVALID_MESSAGE(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "지원 메시지는 필수입니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "지원 역할은 필수입니다."),
    INVALID_STATUS(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "지원 상태는 PASS 또는 FAIL이어야 합니다."),
    INVALID_SIZE(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "조회 개수는 1 이상이어야 합니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "유효하지 않은 토큰입니다."),
    PROJECT_OWNER_ONLY_FOR_READ(HttpStatus.FORBIDDEN, "FORBIDDEN", "프로젝트 작성자만 지원자 목록을 조회할 수 있습니다."),
    PROJECT_OWNER_ONLY_FOR_UPDATE(HttpStatus.FORBIDDEN, "FORBIDDEN", "프로젝트 작성자만 지원 상태를 변경할 수 있습니다."),
    APPLICANT_ONLY_FOR_CANCEL(HttpStatus.FORBIDDEN, "FORBIDDEN", "지원자 본인만 지원을 취소할 수 있습니다."),
    SELF_APPLICATION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "FORBIDDEN", "본인이 작성한 프로젝트에는 지원할 수 없습니다."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "APPLICATION_NOT_FOUND", "지원 내역을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND", "역할을 찾을 수 없습니다."),
    PROJECT_ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_ROLE_NOT_FOUND", "프로젝트의 모집 역할을 찾을 수 없습니다."),
    DUPLICATE_APPLICATION(HttpStatus.CONFLICT, "DUPLICATE_APPLICATION", "이미 해당 프로젝트 역할에 지원했습니다."),
    RECRUITMENT_CLOSED(HttpStatus.CONFLICT, "RECRUITMENT_CLOSED", "모집이 마감되었습니다."),
    DEADLINE_PASSED(HttpStatus.CONFLICT, "DEADLINE_PASSED", "지원 마감일이 지났습니다."),
    APPLICATION_ALREADY_PROCESSED(HttpStatus.CONFLICT, "APPLICATION_ALREADY_PROCESSED", "이미 처리된 지원입니다."),
    APPLICATION_NOT_CANCELABLE(HttpStatus.CONFLICT, "APPLICATION_NOT_CANCELABLE", "취소할 수 없는 지원 상태입니다."),
    RECRUIT_COUNT_EXHAUSTED(HttpStatus.CONFLICT, "RECRUIT_COUNT_EXHAUSTED", "해당 역할의 모집 인원이 마감되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;
}
