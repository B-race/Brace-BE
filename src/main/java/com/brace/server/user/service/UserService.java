package com.brace.server.user.service;

import com.brace.server.application.dto.ApplicationSummaryResponse;
import com.brace.server.application.entity.Application;
import com.brace.server.application.entity.ApplicationStatus;
import com.brace.server.application.repository.ApplicationRepository;
import com.brace.server.auth.exception.code.AuthErrorCode;
import com.brace.server.auth.repository.RefreshTokenRepository;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.project.converter.ProjectConverter;
import com.brace.server.project.dto.response.PageResponse;
import com.brace.server.project.dto.response.ProjectSummaryResponse;
import com.brace.server.project.repository.BookmarkRepository;
import com.brace.server.project.repository.ProjectRepository;
import com.brace.server.user.dto.UserReqDTO;
import com.brace.server.user.dto.UserResDTO;
import com.brace.server.user.entity.Skill;
import com.brace.server.user.entity.User;
import com.brace.server.user.exception.code.UserErrorCode;
import com.brace.server.user.repository.SkillRepository;
import com.brace.server.user.repository.UserRepository;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final BookmarkRepository bookmarkRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public UserResDTO.profileOnboarding profileOnboarding(Long userId, UserReqDTO.profileOnboarding dto) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
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

    @Transactional(readOnly = true)
    public PageResponse<ProjectSummaryResponse> getMyProjects(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return new PageResponse<>(
                projectRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable)
                        .map(ProjectConverter::toSummaryResponse)
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationSummaryResponse> getMyApplications(Long userId, ApplicationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return new PageResponse<>(
                applicationRepository.findByUserIdAndStatus(userId, status, pageable)
                        .map(this::toApplicationSummaryResponse)
        );
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public UserResDTO.myPage myPage(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ProjectException(AuthErrorCode.NOT_FOUND));

        List<Skill> userSkills = skillRepository.findByUserId(userId);

        List<UserResDTO.myPageSkill> userSkillsDTO = userSkills.stream()
                .map(skill -> UserResDTO.myPageSkill.builder()
                        .skillId(skill.getId())
                        .skillTag(skill.getSkillTag())
                        .build())
                .toList();

        Integer registeredProjects = projectRepository.countByUser_Id(userId);
        Integer appliedProjects = applicationRepository.countByUser_Id(userId);
        Integer bookmarkedProjects = bookmarkRepository.countByUser_Id(userId);

        return UserResDTO.myPage.builder()
                .userId(userId)
                .name(user.getName())
                .role(user.getRole())
                .introduction(user.getIntroduction())
                .email(user.getEmail())
                .skills(userSkillsDTO)
                .createdAt(user.getCreatedAt())
                .githubUrl(user.getGithubUrl())
                .notionUrl(user.getNotionUrl())
                .extraUrl(user.getExtraUrl())
                .registeredProjects(registeredProjects)
                .appliedProjects(appliedProjects)
                .bookmarkedProjects(bookmarkedProjects)
                .build();
    }

    @Transactional
    public UserResDTO.updateProfile updateProfile(Long userId, UserReqDTO.updateProfile dto) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ProjectException(AuthErrorCode.NOT_FOUND));

        user.updateProfile(
                dto.profileImgUrl(),
                dto.name(),
                dto.introduction(),
                dto.role(),
                dto.skillTags(),
                dto.portfolioUrl(),
                dto.githubUrl(),
                dto.notionUrl(),
                dto.extraUrl()
        );

        return UserResDTO.updateProfile.builder()
                .userId(user.getId())
                .profileCompleted(user.getProfileCompleted())
                .build();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ProjectException(AuthErrorCode.NOT_FOUND));

        refreshTokenRepository.deleteByUserId(userId);
        user.softDelete();
    }
}
