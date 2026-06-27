package com.brace.server.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "skills")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_tag", nullable = false)
    private SkillTag skillTag;

    private Skill(User user, SkillTag skillTag) {
        this.user = user;
        this.skillTag = skillTag;
    }

    public static Skill of(User user, SkillTag skillTag) {
        return new Skill(user, skillTag);
    }
}
