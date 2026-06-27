package com.brace.server.project.repository;

import com.brace.server.project.entity.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    Optional<Project> findByIdAndDeletedAtIsNull(Long id);

    Integer countByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);
  
    @Query("""
            select p
            from Project p
            join fetch p.user
            where p.id = :projectId
            """)
    Optional<Project> findByIdWithOwner(@Param("projectId") Long projectId);
}
