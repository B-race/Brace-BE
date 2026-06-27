package com.brace.server.user.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false)
    private SocialProvider socialProvider;

    @Column(name = "social_id")
    private String socialId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String role;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_type")
    private ParticipationType participationType;

    @Column(name = "introduction", length = 255)
    private String introduction;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "notion_url", length = 500)
    private String notionUrl;

    @Column(name = "extra_url", length = 500)
    private String extraUrl;

    @Column(name = "profile_completed", nullable = false)
    private Boolean profileCompleted;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public User(
            Long id,
            String email,
            String password,
            SocialProvider socialProvider,
            String socialId,
            String name,
            String role,
            String profileImageUrl,
            List<Skill> skills,
            ParticipationType participationType,
            String introduction,
            String portfolioUrl,
            String githubUrl,
            String notionUrl,
            String extraUrl,
            Boolean profileCompleted
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.name = name;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
        this.skills = skills == null ? new ArrayList<>() : skills;
        this.participationType = participationType;
        this.introduction = introduction;
        this.portfolioUrl = portfolioUrl;
        this.githubUrl = githubUrl;
        this.notionUrl = notionUrl;
        this.extraUrl = extraUrl;
        this.profileCompleted = profileCompleted;
    }

    public void profileOnboarding(
            String profileImageUrl,
            String role,
            List<SkillTag> skillTags,
            ParticipationType participationType,
            String introduction,
            String portfolioUrl
    ) {
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.skills.clear();
        skillTags.stream()
                .map(skillTag -> Skill.of(this, skillTag))
                .forEach(this.skills::add);
        this.participationType = participationType;
        this.introduction = introduction;
        this.portfolioUrl = portfolioUrl;
        this.profileCompleted = true;
    }

    public void updateProfile(
            String profileImageUrl,
            String name,
            String introduction,
            String role,
            List<SkillTag> skillTags,
            String portfolioUrl,
            String githubUrl,
            String notionUrl,
            String extraUrl
    ) {
        if (hasText(profileImageUrl)) {
            this.profileImageUrl = profileImageUrl;
        }
        if (hasText(name)) {
            this.name = name;
        }
        if (hasText(introduction)) {
            this.introduction = introduction;
        }
        if (hasText(role)) {
            this.role = role;
        }
        if (skillTags != null) {
            this.skills.clear();
            skillTags.stream()
                    .map(skillTag -> Skill.of(this, skillTag))
                    .forEach(this.skills::add);
        }
        if (hasText(portfolioUrl)) {
            this.portfolioUrl = portfolioUrl;
        }
        if (hasText(githubUrl)) {
            this.githubUrl = githubUrl;
        }
        if (hasText(notionUrl)) {
            this.notionUrl = notionUrl;
        }
        if (hasText(extraUrl)) {
            this.extraUrl = extraUrl;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
