package com.brace.server.project.entity;

import com.brace.server.user.entity.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "project_roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_PROJECT_ROLE", columnNames = {"project_id", "role_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recruit_count", nullable = false)
    private Integer recruitCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Builder
    public ProjectRole(Long id, Integer recruitCount, Project project, Role role) {
        this.id = id;
        this.recruitCount = recruitCount;
        this.project = project;
        this.role = role;
    }

    public boolean hasRecruitCount() {
        return recruitCount > 0;
    }
}
