package com.brace.server.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", nullable = false)
    private SocialProvider socialProvider;

    @Column(name = "social_id", nullable = false)
    private String socialId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String role;

    @Column(name = "profile_image_url", nullable = false, length = 500)
    private String profileImageUrl;

    @Column(name = "tech_tags", nullable = false, length = 500)
    private String techTags;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_type", nullable = false)
    private ParticipationType participationType;

    private String introduction;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

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
            String techTags,
            ParticipationType participationType,
            String introduction,
            String portfolioUrl
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.name = name;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
        this.techTags = techTags;
        this.participationType = participationType;
        this.introduction = introduction;
        this.portfolioUrl = portfolioUrl;
    }
}
