package com.brace.server.application.entity;

import com.brace.server.project.entity.Project;
import com.brace.server.user.entity.Role;
import com.brace.server.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "applications",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_APPLICATION_USER_PROJECT_ROLE", columnNames = {"user_id", "project_id", "role_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PROGRESS;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Builder
    public Application(Long id, String message, User user, Project project, Role role) {
        this.id = id;
        this.message = message;
        this.user = user;
        this.project = project;
        this.role = role;
    }

    public void pass() {
        this.status = ApplicationStatus.PASS;
    }

    public void fail() {
        this.status = ApplicationStatus.FAIL;
    }

    public void cancel() {
        this.status = ApplicationStatus.CANCEL;
    }
}
