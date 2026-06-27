package com.brace.server.application.dto;

import com.brace.server.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ApplicationReqDto {

    public record Message(
            @NotBlank(message = "지원 역할은 필수입니다.")
            String role,

            @NotBlank(message = "지원 메시지는 필수입니다.")
            String message
    ) {
    }

    public record Status(
            @NotBlank(message = "지원 상태는 PASS 또는 FAIL이어야 합니다.")
            @Pattern(regexp = "PASS|FAIL", message = "지원 상태는 PASS 또는 FAIL이어야 합니다.")
            String status
    ) {
        public ApplicationStatus toApplicationStatus() {
            return ApplicationStatus.valueOf(status);
        }
    }
}
