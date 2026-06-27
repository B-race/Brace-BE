package com.brace.server.user.dto;

import lombok.Builder;

public class UserResDTO {

    @Builder
    public record profileOnboarding(
            Long userId,
            Boolean profileCompleted
    ) {
    }
}
