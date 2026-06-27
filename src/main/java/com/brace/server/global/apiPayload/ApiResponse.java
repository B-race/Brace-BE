package com.brace.server.global.apiPayload;

import com.brace.server.global.code.BaseErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "errorCode", "message", "result"})
public class ApiResponse<T> {
    @JsonProperty("isSuccess")
    private final Boolean isSuccess;

    @JsonProperty("errorCode")
    private final String errorCode;

    @JsonProperty("message")
    private final String message;

    @JsonProperty("result")
    private final T result;

    public static <T> ApiResponse<T> success(BaseErrorCode errorCode, T result) {
        return new ApiResponse<>(true, errorCode.getErrorCode(), errorCode.getMessage(), result);
    }

    public static <T> ApiResponse<T> error(BaseErrorCode errorCode, T result) {
        return new ApiResponse<>(false, errorCode.getErrorCode(), errorCode.getMessage(), result);
    }
}
