package com.brace.server.project.repository;

import com.brace.server.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("""
            select p
            from Project p
            join fetch p.user
            where p.id = :projectId
            """)
    Optional<Project> findByIdWithOwner(@Param("projectId") Long projectId);
}
