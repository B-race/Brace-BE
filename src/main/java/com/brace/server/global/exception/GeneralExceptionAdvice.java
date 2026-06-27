package com.brace.server.global.exception;


import com.brace.server.global.apiPayload.ApiResponse;
import com.brace.server.global.code.BaseErrorCode;
import com.brace.server.global.code.GeneralErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GeneralExceptionAdvice {
    @ExceptionHandler(ProjectException.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(ProjectException e) {
        BaseErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ApiResponse<>(false, errorCode.getErrorCode(), errorCode.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BaseErrorCode errorCode = GeneralErrorCode.BAD_REQUEST;
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ApiResponse<>(false, errorCode.getErrorCode(), message, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception e) {
        BaseErrorCode errorCode = GeneralErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ApiResponse<>(false, errorCode.getErrorCode(), errorCode.getMessage(), null));
    }
}
