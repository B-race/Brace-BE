package com.brace.server.auth.dto;

import jakarta.validation.constraints.*;

public class AuthReqDTO {
    public record signUp(
            @NotBlank
            @Size(max = 20)
            String name,

            @NotBlank
            @Email
            String email,

            @NotBlank(message = "비밀번호는 필수입니다.")
            @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+=-]{8,20}$"
            )
            String password
    ) {}

    public record login(
            @NotBlank
            @Email
            String email,

            @NotBlank
            String password
    ) {}

    public record logout(
            String refreshToken
    ) {}

    public record reissue(
            @NotBlank
            String refreshToken
    ) {}

    public record googleLogin(
            @NotBlank
            String idToken
    ) {}

    public record naverLogin(
            @NotBlank
            String code,

            @NotBlank
            String state
    ) {}
}
