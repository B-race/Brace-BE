package com.brace.server.auth.service;

import com.brace.server.auth.dto.AuthReqDTO;
import com.brace.server.auth.dto.AuthResDTO;
import com.brace.server.auth.exception.code.AuthErrorCode;
import com.brace.server.auth.jwt.JwtTokenProvider;
import com.brace.server.auth.oauth.GoogleTokenVerifier;
import com.brace.server.auth.oauth.GoogleUserInfo;
import com.brace.server.auth.oauth.NaverOAuthClient;
import com.brace.server.auth.oauth.NaverUserInfo;
import com.brace.server.auth.repository.RefreshTokenRepository;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.user.entity.ParticipationType;
import com.brace.server.user.entity.SocialProvider;
import com.brace.server.user.entity.User;
import com.brace.server.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @Mock
    private NaverOAuthClient naverOAuthClient;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Spy
    private JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "test-secret-key-for-google-login-test",
            7_200_000L,
            1_209_600_000L
    );

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("google login creates user and issues service tokens")
    void googleLoginCreatesUserAndIssuesServiceTokens() {
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(
                "google-sub-1",
                "google@example.com",
                "Google User",
                "https://example.com/profile.png"
        );
        User savedUser = googleUser(1L, googleUserInfo);

        when(googleTokenVerifier.verify("id-token")).thenReturn(googleUserInfo);
        when(userRepository.findBySocialProviderAndSocialIdAndDeletedAtIsNull(SocialProvider.GOOGLE, "google-sub-1"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndDeletedAtIsNull("google@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResDTO.googleLogin response = authService.googleLogin(new AuthReqDTO.googleLogin("id-token"));

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.profileCompleted()).isFalse();
        verify(refreshTokenRepository).save(any());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User newUser = userCaptor.getValue();
        assertThat(newUser.getEmail()).isEqualTo("google@example.com");
        assertThat(newUser.getSocialProvider()).isEqualTo(SocialProvider.GOOGLE);
        assertThat(newUser.getSocialId()).isEqualTo("google-sub-1");
        assertThat(newUser.getName()).isEqualTo("Google User");
        assertThat(newUser.getProfileImageUrl()).isEqualTo("https://example.com/profile.png");
        assertThat(newUser.getParticipationType()).isEqualTo(ParticipationType.BOTH);
        assertThat(newUser.getProfileCompleted()).isFalse();
    }

    @Test
    @DisplayName("google login signs in existing google user")
    void googleLoginSignsInExistingGoogleUser() {
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(
                "google-sub-1",
                "google@example.com",
                "Google User",
                "https://example.com/profile.png"
        );
        User existingUser = googleUser(1L, googleUserInfo);

        when(googleTokenVerifier.verify("id-token")).thenReturn(googleUserInfo);
        when(userRepository.findBySocialProviderAndSocialIdAndDeletedAtIsNull(SocialProvider.GOOGLE, "google-sub-1"))
                .thenReturn(Optional.of(existingUser));

        AuthResDTO.googleLogin response = authService.googleLogin(new AuthReqDTO.googleLogin("id-token"));

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(userRepository, never()).save(any(User.class));
        verify(refreshTokenRepository).save(any());
    }

    @Test
    @DisplayName("google login rejects email already used by another account")
    void googleLoginRejectsEmailAlreadyUsedByAnotherAccount() {
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(
                "google-sub-1",
                "google@example.com",
                "Google User",
                "https://example.com/profile.png"
        );
        User existingEmailUser = User.builder()
                .id(2L)
                .email("google@example.com")
                .password("password")
                .socialProvider(SocialProvider.NONE)
                .socialId("")
                .name("Local User")
                .role("")
                .profileImageUrl("")
                .participationType(ParticipationType.BOTH)
                .profileCompleted(false)
                .build();

        when(googleTokenVerifier.verify("id-token")).thenReturn(googleUserInfo);
        when(userRepository.findBySocialProviderAndSocialIdAndDeletedAtIsNull(SocialProvider.GOOGLE, "google-sub-1"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndDeletedAtIsNull("google@example.com"))
                .thenReturn(Optional.of(existingEmailUser));

        assertThatThrownBy(() -> authService.googleLogin(new AuthReqDTO.googleLogin("id-token")))
                .isInstanceOf(ProjectException.class)
                .extracting(exception -> ((ProjectException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.ALREADY_EXISTS_EMAIL);

        verify(userRepository, never()).save(any(User.class));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("naver login creates user and issues service tokens")
    void naverLoginCreatesUserAndIssuesServiceTokens() {
        NaverUserInfo naverUserInfo = new NaverUserInfo(
                "naver-id-1",
                "naver@example.com",
                "Naver User",
                "https://example.com/naver-profile.png"
        );
        User savedUser = naverUser(1L, naverUserInfo);

        when(naverOAuthClient.getUserInfo("code", "state")).thenReturn(naverUserInfo);
        when(userRepository.findBySocialProviderAndSocialIdAndDeletedAtIsNull(SocialProvider.NAVER, "naver-id-1"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailAndDeletedAtIsNull("naver@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResDTO.naverLogin response = authService.naverLogin(new AuthReqDTO.naverLogin("code", "state"));

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.profileCompleted()).isFalse();
        verify(refreshTokenRepository).save(any());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User newUser = userCaptor.getValue();
        assertThat(newUser.getEmail()).isEqualTo("naver@example.com");
        assertThat(newUser.getSocialProvider()).isEqualTo(SocialProvider.NAVER);
        assertThat(newUser.getSocialId()).isEqualTo("naver-id-1");
        assertThat(newUser.getName()).isEqualTo("Naver User");
        assertThat(newUser.getProfileImageUrl()).isEqualTo("https://example.com/naver-profile.png");
        assertThat(newUser.getParticipationType()).isEqualTo(ParticipationType.BOTH);
        assertThat(newUser.getProfileCompleted()).isFalse();
    }

    private User googleUser(Long id, GoogleUserInfo googleUserInfo) {
        return User.builder()
                .id(id)
                .email(googleUserInfo.email())
                .password("encoded-password")
                .socialProvider(SocialProvider.GOOGLE)
                .socialId(googleUserInfo.socialId())
                .name(googleUserInfo.name())
                .role("")
                .profileImageUrl(googleUserInfo.profileImageUrl())
                .participationType(ParticipationType.BOTH)
                .profileCompleted(false)
                .build();
    }

    private User naverUser(Long id, NaverUserInfo naverUserInfo) {
        return User.builder()
                .id(id)
                .email(naverUserInfo.email())
                .password("encoded-password")
                .socialProvider(SocialProvider.NAVER)
                .socialId(naverUserInfo.socialId())
                .name(naverUserInfo.name())
                .role("")
                .profileImageUrl(naverUserInfo.profileImageUrl())
                .participationType(ParticipationType.BOTH)
                .profileCompleted(false)
                .build();
    }
}
