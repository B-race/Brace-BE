package com.brace.server.user.dto;

import com.brace.server.user.entity.Skill;
import com.brace.server.user.entity.SkillTag;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UserResDTO {

    @Builder
    public record profileOnboarding(
            Long userId,
            Boolean profileCompleted
    ) {
    }

    @Builder
    public record myPage(
            Long userId,
            String name,
            String role,
            String profileImageUrl,
            List<myPageSkill> skills,
            String introduction,
            Integer registeredProjects,
            Integer appliedProjects,
            Integer bookmarkedProjects,
            String email,
            LocalDateTime createdAt,
            String githubUrl,
            String notionUrl,
            String extraUrl
    ) {}

    @Builder
    public record myPageSkill(
            Long skillId,
            SkillTag skillTag
    ) {}

    @Builder
    public record updateProfile(
            Long userId,
            Boolean profileCompleted
    ) {}
}
