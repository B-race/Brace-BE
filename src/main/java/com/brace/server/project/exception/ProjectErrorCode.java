package com.brace.server.project.exception;

import com.brace.server.global.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProjectErrorCode implements BaseErrorCode {

    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND404", "프로젝트를 찾을 수 없습니다"),
    PROJECT_FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN_PROJECT_ACCESS403", "작성자만 수정/삭제할 수 있습니다"),
    BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "BOOKMARK_DUPLICATED409", "이미 북마크한 프로젝트입니다"),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKMARK_NOT_FOUND404", "북마크를 찾을 수 없습니다"),
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND404", "역할을 찾을 수 없습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED401", "인증이 필요합니다"),
    CONTEST_FIELDS_REQUIRED(HttpStatus.BAD_REQUEST, "INVALID_PROJECT_REQUEST400", "공모전 등록 시 projectName과 projectUrl은 필수입니다"),
    INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST, "INVALID_SORT_TYPE400", "지원하지 않는 정렬 기준입니다"),
    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;
}
