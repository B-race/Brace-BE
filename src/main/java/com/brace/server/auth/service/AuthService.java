package com.brace.server.auth.service;

import com.brace.server.auth.dto.AuthReqDTO;
import com.brace.server.auth.dto.AuthResDTO;
import com.brace.server.auth.exception.code.AuthErrorCode;
import com.brace.server.auth.jwt.JwtTokenProvider;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.user.entity.ParticipationType;
import com.brace.server.user.entity.SocialProvider;
import com.brace.server.user.entity.User;
import com.brace.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
                .participationType(ParticipationType.BOTH)
                .profileCompleted(false)
                .build();

        User savedUser = userRepository.save(user);
        String accessToken = jwtTokenProvider.createAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return AuthResDTO.signUp.builder()
                .userId(savedUser.getId())
                .accessToken(accessToken)
                .tokenType("Bearer")
                .profileCompleted(savedUser.getProfileCompleted())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResDTO.login login(AuthReqDTO.login dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ProjectException(AuthErrorCode.NOT_FOUND));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new ProjectException(AuthErrorCode.INVALID_PASSWORD);
        }

        return AuthResDTO.login.builder()
                .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole()))
                .tokenType("Bearer")
                .profileCompleted(user.getProfileCompleted())
                .build();
    }
}
