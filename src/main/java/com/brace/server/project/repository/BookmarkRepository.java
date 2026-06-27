package com.brace.server.project.repository;

import com.brace.server.project.entity.Bookmark;
import com.brace.server.project.entity.Project;
import com.brace.server.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserAndProject(User user, Project project);

    Optional<Bookmark> findByUserAndProject(User user, Project project);

    Integer countByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);

    void deleteByProject_User_Id(Long userId);
}
