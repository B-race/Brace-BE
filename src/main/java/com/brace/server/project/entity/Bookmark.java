package com.brace.server.project.entity;

import com.brace.server.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "bookmark",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_BOOKMARK_USER_PROJECT", columnNames = {"user_id", "project_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Builder
    public Bookmark(Long id, User user, Project project) {
        this.id = id;
        this.user = user;
        this.project = project;
    }
}
