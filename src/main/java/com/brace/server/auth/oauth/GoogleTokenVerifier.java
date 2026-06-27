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
public class GoogleTokenVerifier {
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${google.oauth.client-id}")
    private String googleClientId;

    public GoogleUserInfo verify(String idToken) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_TOKEN_INFO_URL + URLEncoder.encode(idToken, StandardCharsets.UTF_8)))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
            }

            JsonNode payload = objectMapper.readTree(response.body());
            validatePayload(payload);

            return new GoogleUserInfo(
                    requiredText(payload, "sub"),
                    requiredText(payload, "email"),
                    textOrEmpty(payload, "name"),
                    textOrEmpty(payload, "picture")
            );
        } catch (ProjectException e) {
            throw e;
        } catch (Exception e) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    private void validatePayload(JsonNode payload) {
        if (!googleClientId.equals(requiredText(payload, "aud"))) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }

        if (!"true".equals(requiredText(payload, "email_verified"))) {
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
}
