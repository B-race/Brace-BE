package com.brace.server.user.service;

import com.brace.server.application.repository.ApplicationRepository;
import com.brace.server.auth.exception.code.AuthErrorCode;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.project.repository.BookmarkRepository;
import com.brace.server.project.repository.ProjectRepository;
import com.brace.server.user.dto.UserReqDTO;
import com.brace.server.user.dto.UserResDTO;
import com.brace.server.user.entity.Skill;
import com.brace.server.user.entity.User;
import com.brace.server.user.exception.code.UserErrorCode;
import com.brace.server.user.repository.SkillRepository;
import com.brace.server.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public UserResDTO.myPage myPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(AuthErrorCode.NOT_FOUND));

        // skills 조회 로직
        List<Skill> userSkills = skillRepository.findByUserId(userId);

        List<UserResDTO.myPageSkill> userSkillsDTO = userSkills.stream()
                .map(skill -> UserResDTO.myPageSkill.builder()
                        .skillId(skill.getId())
                        .skillTag(skill.getSkillTag())
                        .build())
                .toList();

        //등록한 프로젝트 개수 구하기
        Integer registeredProjects = projectRepository.countByUser_Id(userId);

        //지원한 프로젝트 개수 구하기
        Integer appliedProjects = applicationRepository.countByUser_Id(userId);

        //북마크 프로젝트 개수 구하기
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
                .extraUrl(user.getExtraUrl())
                .registeredProjects(registeredProjects)
                .appliedProjects(appliedProjects)
                .bookmarkedProjects(bookmarkedProjects)
                .build();
    }
}
