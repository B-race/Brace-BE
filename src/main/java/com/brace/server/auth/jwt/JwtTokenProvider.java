package com.brace.server.auth.jwt;

import com.brace.server.auth.exception.code.AuthErrorCode;
import com.brace.server.global.exception.ProjectException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final String secret;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret:brace-local-development-secret-key-change-me}") String secret,
            @Value("${jwt.access-token-expiration-millis:7200000}") long accessTokenExpirationMillis,
            @Value("${jwt.refresh-token-expiration-millis:1209600000}") long refreshTokenExpirationMillis
    ) {
        this.secret = secret;
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
    }

    public String createAccessToken(Long userId, String email, String role) {
        return createToken(userId, email, role, accessTokenExpirationMillis, ACCESS_TOKEN_TYPE);
    }

    public String createRefreshToken(Long userId, String email, String role) {
        return createToken(userId, email, role, refreshTokenExpirationMillis, REFRESH_TOKEN_TYPE);
    }

    public long getRefreshTokenExpirationMillis() {
        return refreshTokenExpirationMillis;
    }

    private String createToken(Long userId, String email, String role, long expirationMillis, String tokenType) {
        long now = Instant.now().toEpochMilli();
        long expiresAt = now + expirationMillis;

        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("""
                {"sub":"%s","email":"%s","role":"%s","tokenType":"%s","jti":"%s","iat":%d,"exp":%d}
                """.formatted(userId, escape(email), escape(role), tokenType, UUID.randomUUID(), now / 1000, expiresAt / 1000).trim());
        String unsignedToken = header + "." + payload;

        return unsignedToken + "." + sign(unsignedToken);
    }

    public Long getUserIdFromAccessToken(String token) {
        return getUserId(token, ACCESS_TOKEN_TYPE);
    }

    public Long getUserIdFromRefreshToken(String token) {
        return getUserId(token, REFRESH_TOKEN_TYPE);
    }

    public Long getUserId(String token) {
        return getUserId(token, null);
    }

    private Long getUserId(String token, String expectedTokenType) {
        validateToken(token, expectedTokenType);

        try {
            String payload = decodePayload(token);
            String subject = extractStringClaim(payload, "sub");
            return Long.valueOf(subject);
        } catch (Exception e) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    public boolean validateToken(String token) {
        return validateToken(token, null);
    }

    private boolean validateToken(String token, String expectedTokenType) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
            }

            long expiresAt = extractLongClaim(decode(parts[1]), "exp");
            if (Instant.now().getEpochSecond() >= expiresAt) {
                throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
            }

            if (expectedTokenType != null) {
                String tokenType = extractStringClaim(decode(parts[1]), "tokenType");
                if (!expectedTokenType.equals(tokenType)) {
                    throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
                }
            }

            return true;
        } catch (ProjectException e) {
            throw e;
        } catch (Exception e) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    private String decodePayload(String token) {
        return decode(token.split("\\.")[1]);
    }

    private String encode(String value) {
        return BASE64_URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(BASE64_URL_DECODER.decode(value), StandardCharsets.UTF_8);
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    private String extractStringClaim(String payload, String key) {
        String claimPrefix = "\"" + key + "\":\"";
        int start = payload.indexOf(claimPrefix);
        if (start < 0) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }

        int valueStart = start + claimPrefix.length();
        int valueEnd = payload.indexOf("\"", valueStart);
        if (valueEnd < 0) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }

        return payload.substring(valueStart, valueEnd);
    }

    private long extractLongClaim(String payload, String key) {
        String claimPrefix = "\"" + key + "\":";
        int start = payload.indexOf(claimPrefix);
        if (start < 0) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }

        int valueStart = start + claimPrefix.length();
        int valueEnd = payload.indexOf(",", valueStart);
        if (valueEnd < 0) {
            valueEnd = payload.indexOf("}", valueStart);
        }
        if (valueEnd < 0) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }

        return Long.parseLong(payload.substring(valueStart, valueEnd));
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
