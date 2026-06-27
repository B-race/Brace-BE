package com.brace.server.auth.oauth;

import com.brace.server.auth.exception.code.AuthErrorCode;
import com.brace.server.global.exception.ProjectException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NaverOAuthClient {
    private static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_PROFILE_URL = "https://openapi.naver.com/v1/nid/me";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${naver.oauth.client-id}")
    private String clientId;

    @Value("${naver.oauth.client-secret}")
    private String clientSecret;

    @Value("${naver.oauth.redirect-uri}")
    private String redirectUri;

    public NaverUserInfo getUserInfo(String code, String state) {
        validateProperties();
        String accessToken = requestAccessToken(code, state);
        return requestUserInfo(accessToken);
    }

    private String requestAccessToken(String code, String state) {
        try {
            String uri = NAVER_TOKEN_URL
                    + "?grant_type=authorization_code"
                    + "&client_id=" + encode(clientId)
                    + "&client_secret=" + encode(clientSecret)
                    + "&redirect_uri=" + encode(redirectUri)
                    + "&code=" + encode(code)
                    + "&state=" + encode(state);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
            }

            JsonNode payload = objectMapper.readTree(response.body());
            JsonNode accessToken = payload.get("access_token");
            if (accessToken == null || accessToken.asText().isBlank()) {
                throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
            }

            return accessToken.asText();
        } catch (ProjectException e) {
            throw e;
        } catch (Exception e) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    private NaverUserInfo requestUserInfo(String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(NAVER_PROFILE_URL))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
            }

            JsonNode payload = objectMapper.readTree(response.body());
            if (!"00".equals(requiredText(payload, "resultcode"))) {
                throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
            }

            JsonNode profile = payload.get("response");
            if (profile == null || profile.isNull()) {
                throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
            }

            return new NaverUserInfo(
                    requiredText(profile, "id"),
                    requiredText(profile, "email"),
                    textOrEmpty(profile, "name"),
                    textOrEmpty(profile, "profile_image")
            );
        } catch (ProjectException e) {
            throw e;
        } catch (Exception e) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    private void validateProperties() {
        if (clientId == null || clientId.isBlank()
                || clientSecret == null || clientSecret.isBlank()
                || redirectUri == null || redirectUri.isBlank()) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    private String requiredText(JsonNode payload, String fieldName) {
        JsonNode value = payload.get(fieldName);
        if (value == null || value.asText().isBlank()) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }

        return value.asText();
    }

    private String textOrEmpty(JsonNode payload, String fieldName) {
        JsonNode value = payload.get(fieldName);
        return value == null ? "" : value.asText("");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
