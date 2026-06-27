package com.brace.server.auth.dto;

import lombok.Builder;

public class AuthResDTO {

    @Builder
    public record signUp(
            Long userId,
            String accessToken,
            String refreshToken,
            String tokenType,
            Boolean profileCompleted
    ) {}

    @Builder
    public record login(
            String accessToken,
            String refreshToken,
            String tokenType,
            Boolean profileCompleted
    ) {}

    @Builder
    public record logout(

    ) {}

    @Builder
    public record reissue(
            String accessToken,
            String refreshToken,
            String tokenType
    ) {}

    @Builder
    public record googleLogin(
            Long userId,
            String accessToken,
            String refreshToken,
            String tokenType,
            Boolean profileCompleted
    ) {}
}
