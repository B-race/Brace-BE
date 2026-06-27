package com.brace.server.auth.service;

import com.brace.server.auth.dto.AuthReqDTO;
import com.brace.server.auth.dto.AuthResDTO;
import com.brace.server.auth.entity.RefreshToken;
import com.brace.server.auth.exception.code.AuthErrorCode;
import com.brace.server.auth.jwt.JwtTokenProvider;
import com.brace.server.auth.oauth.GoogleTokenVerifier;
import com.brace.server.auth.oauth.GoogleUserInfo;
import com.brace.server.auth.repository.RefreshTokenRepository;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.user.entity.ParticipationType;
import com.brace.server.user.entity.SocialProvider;
import com.brace.server.user.entity.User;
import com.brace.server.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Transactional
    public AuthResDTO.signUp signUp(AuthReqDTO.signUp dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ProjectException(AuthErrorCode.ALREADY_EXISTS_EMAIL);
        }

        User user = User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .socialProvider(SocialProvider.NONE)
                .socialId("")
                .name(dto.name())
                .role("")
                .profileImageUrl("")
                .participationType(ParticipationType.BOTH)
                .profileCompleted(false)
                .build();

        User savedUser = userRepository.save(user);
        String accessToken = jwtTokenProvider.createAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        saveRefreshToken(savedUser, refreshToken);

        return AuthResDTO.signUp.builder()
                .userId(savedUser.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .profileCompleted(savedUser.getProfileCompleted())
                .build();
    }

    @Transactional
    public AuthResDTO.login login(AuthReqDTO.login dto) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(dto.email())
                .orElseThrow(() -> new ProjectException(AuthErrorCode.NOT_FOUND));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new ProjectException(AuthErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), user.getRole());
        saveRefreshToken(user, refreshToken);

        return AuthResDTO.login.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .profileCompleted(user.getProfileCompleted())
                .build();
    }

    @Transactional
    public void logout(Long userId, AuthReqDTO.logout dto) {
        if (dto == null || dto.refreshToken() == null || dto.refreshToken().isBlank()) {
            refreshTokenRepository.deleteByUserId(userId);
            return;
        }

        refreshTokenRepository.deleteByUserIdAndTokenHash(userId, hashToken(dto.refreshToken()));
    }

    @Transactional
    public AuthResDTO.reissue reissue(AuthReqDTO.reissue dto) {
        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(dto.refreshToken());
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ProjectException(AuthErrorCode.NOT_FOUND));

        String tokenHash = hashToken(dto.refreshToken());
        RefreshToken storedRefreshToken = refreshTokenRepository.findByUserIdAndTokenHash(userId, tokenHash)
                .orElseThrow(() -> new ProjectException(AuthErrorCode.INVALID_TOKEN));

        if (storedRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedRefreshToken);
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }

        refreshTokenRepository.delete(storedRefreshToken);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), user.getRole());
        saveRefreshToken(user, refreshToken);

        return AuthResDTO.reissue.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Transactional
    public AuthResDTO.googleLogin googleLogin(AuthReqDTO.googleLogin dto) {
        GoogleUserInfo googleUserInfo = googleTokenVerifier.verify(dto.idToken());
        User user = userRepository.findBySocialProviderAndSocialIdAndDeletedAtIsNull(
                        SocialProvider.GOOGLE,
                        googleUserInfo.socialId()
                )
                .orElseGet(() -> createGoogleUser(googleUserInfo));

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail(), user.getRole());
        saveRefreshToken(user, refreshToken);

        return AuthResDTO.googleLogin.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .profileCompleted(user.getProfileCompleted())
                .build();
    }

    private User createGoogleUser(GoogleUserInfo googleUserInfo) {
        userRepository.findByEmailAndDeletedAtIsNull(googleUserInfo.email())
                .ifPresent(user -> {
                    throw new ProjectException(AuthErrorCode.ALREADY_EXISTS_EMAIL);
                });

        User user = User.builder()
                .email(googleUserInfo.email())
                .password(passwordEncoder.encode("GOOGLE:" + googleUserInfo.socialId()))
                .socialProvider(SocialProvider.GOOGLE)
                .socialId(googleUserInfo.socialId())
                .name(googleUserInfo.name().isBlank() ? googleUserInfo.email() : googleUserInfo.name())
                .role("")
                .profileImageUrl(googleUserInfo.profileImageUrl())
                .participationType(ParticipationType.BOTH)
                .profileCompleted(false)
                .build();

        return userRepository.save(user);
    }

    private void saveRefreshToken(User user, String refreshToken) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpirationMillis())))
                .build();

        refreshTokenRepository.save(token);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new ProjectException(AuthErrorCode.INVALID_TOKEN);
        }
    }
}
