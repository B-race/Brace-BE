package com.brace.server.auth.oauth;

public record GoogleUserInfo(
        String socialId,
        String email,
        String name,
        String profileImageUrl
) {
}
