package com.brace.server.user.service;

import com.brace.server.application.dto.ApplicationSummaryResponse;
import com.brace.server.application.entity.Application;
import com.brace.server.application.entity.ApplicationStatus;
import com.brace.server.application.repository.ApplicationRepository;
import com.brace.server.auth.exception.code.AuthErrorCode;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.project.converter.ProjectConverter;
import com.brace.server.project.dto.response.PageResponse;
import com.brace.server.project.dto.response.ProjectSummaryResponse;
import com.brace.server.project.repository.BookmarkRepository;
import com.brace.server.project.repository.ProjectRepository;
import com.brace.server.user.dto.UserReqDTO;
import com.brace.server.user.dto.UserResDTO;
import com.brace.server.user.entity.User;
import com.brace.server.user.exception.code.UserErrorCode;
import com.brace.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final BookmarkRepository bookmarkRepository;

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
                dto.skillTags(),
                dto.participationType(),
                dto.introduction(),
                dto.portfolioUrl()
        );

        return UserResDTO.profileOnboarding.builder()
                .userId(user.getId())
                .profileCompleted(user.getProfileCompleted())
                .build();
    }

    public PageResponse<ProjectSummaryResponse> getMyProjects(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return new PageResponse<>(
                projectRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable)
                        .map(ProjectConverter::toSummaryResponse)
        );
    }

    public PageResponse<ApplicationSummaryResponse> getMyApplications(Long userId, ApplicationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return new PageResponse<>(
                applicationRepository.findByUserIdAndStatus(userId, status, pageable)
                        .map(this::toApplicationSummaryResponse)
        );
    }

    public PageResponse<ProjectSummaryResponse> getMyBookmarks(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return new PageResponse<>(
                bookmarkRepository.findProjectsByUserId(userId, pageable)
                        .map(ProjectConverter::toSummaryResponse)
        );
    }

    private ApplicationSummaryResponse toApplicationSummaryResponse(Application application) {
        return ApplicationSummaryResponse.builder()
                .applicationId(application.getId())
                .projectId(application.getProject().getId())
                .projectTitle(application.getProject().getTitle())
                .activityType(application.getProject().getActivityType())
                .roleName(application.getRole().getName())
                .status(application.getStatus())
                .appliedAt(application.getCreatedAt())
                .projectStatus(application.getProject().getStatus())
                .build();
    }
}
