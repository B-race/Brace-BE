package com.brace.server.project.repository;

import com.brace.server.project.entity.ActivityType;
import com.brace.server.project.entity.Bookmark;
import com.brace.server.project.entity.MeetingType;
import com.brace.server.project.entity.Project;
import com.brace.server.project.entity.ProjectRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ProjectSpecification {

    public static Specification<Project> filter(
            ActivityType activityType,
            Long roleId,
            MeetingType meetingType,
            String keyword,
            List<String> tags,
            String sort
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isNull(root.get("deletedAt")));

            if (activityType != null) {
                predicates.add(cb.equal(root.get("activityType"), activityType));
            }

            if (meetingType != null) {
                predicates.add(cb.equal(root.get("meetingType"), meetingType));
            }

            if (roleId != null) {
                Join<Project, ProjectRole> prJoin = root.join("projectRoles", JoinType.INNER);
                predicates.add(cb.equal(prJoin.get("role").get("id"), roleId));
                query.distinct(true);
            }

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), pattern),
                        cb.like(root.get("description"), pattern),
                        cb.like(cb.coalesce(root.get("projectName"), ""), pattern)
                ));
            }

            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags) {
                    predicates.add(cb.like(root.get("tags"), "%" + tag + "%"));
                }
            }

            if ("bookmark_count".equals(sort) && !Boolean.TRUE.equals(query.isDistinct())) {
                Subquery<Long> bookmarkCount = query.subquery(Long.class);
                var bookmarkRoot = bookmarkCount.from(Bookmark.class);
                bookmarkCount.select(cb.count(bookmarkRoot))
                        .where(cb.equal(bookmarkRoot.get("project"), root));
                query.orderBy(cb.desc(bookmarkCount));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
