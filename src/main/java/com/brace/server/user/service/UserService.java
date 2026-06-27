package com.brace.server.user.service;

import com.brace.server.auth.exception.code.AuthErrorCode;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.user.dto.UserReqDTO;
import com.brace.server.user.dto.UserResDTO;
import com.brace.server.user.entity.User;
import com.brace.server.user.exception.code.UserErrorCode;
import com.brace.server.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResDTO.profileOnboarding profileOnboarding(Long userId, UserReqDTO.profileOnboarding dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(AuthErrorCode.NOT_FOUND));

        if (Boolean.TRUE.equals(user.getProfileCompleted())) {
            throw new ProjectException(UserErrorCode.ALREADY_COMPLETED_ONBOARDING);
        }

        user.profileOnboarding(
                dto.profileImg(),
                dto.role(),
                dto.techTags(),
                dto.participationType(),
                dto.introduction(),
                dto.portfolioUrl()
        );

        return UserResDTO.profileOnboarding.builder()
                .userId(user.getId())
                .profileCompleted(user.getProfileCompleted())
                .build();
    }
}
