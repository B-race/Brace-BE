package com.brace.server.auth.oauth;

public record NaverUserInfo(
        String socialId,
        String email,
        String name,
        String profileImageUrl
) {
}
