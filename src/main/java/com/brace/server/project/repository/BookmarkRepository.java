package com.brace.server.project.repository;

import com.brace.server.project.entity.Bookmark;
import com.brace.server.project.entity.Project;
import com.brace.server.user.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserAndProject(User user, Project project);

    Optional<Bookmark> findByUserAndProject(User user, Project project);

    @Query("""
            select b.project from Bookmark b
            where b.user.id = :userId
            and b.project.deletedAt is null
            order by b.id desc
            """)
    Page<Project> findProjectsByUserId(@Param("userId") Long userId, Pageable pageable);
}
