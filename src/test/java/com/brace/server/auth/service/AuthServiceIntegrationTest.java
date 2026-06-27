package com.brace.server.auth.service;

import com.brace.server.TestcontainersConfiguration;
import com.brace.server.auth.dto.AuthReqDTO;
import com.brace.server.auth.dto.AuthResDTO;
import com.brace.server.auth.oauth.GoogleTokenVerifier;
import com.brace.server.auth.oauth.GoogleUserInfo;
import com.brace.server.auth.repository.RefreshTokenRepository;
import com.brace.server.user.entity.SocialProvider;
import com.brace.server.user.entity.User;
import com.brace.server.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Import({
        TestcontainersConfiguration.class,
        AuthServiceIntegrationTest.GoogleVerifierTestConfig.class
})
@SpringBootTest
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    @Test
    @DisplayName("google login stores new user and refresh token in database")
    void googleLoginStoresNewUserAndRefreshTokenInDatabase() {
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(
                "integration-google-sub-1",
                "integration-google@example.com",
                "Integration Google User",
                "https://example.com/integration-profile.png"
        );
        when(googleTokenVerifier.verify("integration-id-token")).thenReturn(googleUserInfo);

        AuthResDTO.googleLogin response = authService.googleLogin(new AuthReqDTO.googleLogin("integration-id-token"));

        Optional<User> savedUser = userRepository.findBySocialProviderAndSocialIdAndDeletedAtIsNull(
                SocialProvider.GOOGLE,
                "integration-google-sub-1"
        );

        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getId()).isEqualTo(response.userId());
        assertThat(savedUser.get().getEmail()).isEqualTo("integration-google@example.com");
        assertThat(savedUser.get().getSocialProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(savedUser.get().getSocialId()).isEqualTo("integration-google-sub-1");
        assertThat(savedUser.get().getName()).isEqualTo("Integration Google User");
        assertThat(savedUser.get().getProfileImageUrl()).isEqualTo("https://example.com/integration-profile.png");
        assertThat(savedUser.get().getProfileCompleted()).isFalse();

        assertThat(refreshTokenRepository.findAll())
                .anySatisfy(refreshToken -> {
                    assertThat(refreshToken.getUser().getId()).isEqualTo(response.userId());
                    assertThat(refreshToken.getTokenHash()).hasSize(64);
                    assertThat(refreshToken.getExpiresAt()).isNotNull();
                });
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @TestConfiguration
    static class GoogleVerifierTestConfig {

        @Bean
        @Primary
        GoogleTokenVerifier googleTokenVerifier() {
            return Mockito.mock(GoogleTokenVerifier.class);
        }
    }
}
