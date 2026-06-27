package com.brace.server.global.code;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
    HttpStatus getHttpStatus();
    String getErrorCode();
    String getMessage();
}
