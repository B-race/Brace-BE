package com.brace.server.project.repository;

import com.brace.server.project.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {

    @Query("""
            select pr
            from ProjectRole pr
            join fetch pr.project p
            join fetch p.user
            join fetch pr.role r
            where p.id = :projectId
              and r.name = :roleName
            """)
    Optional<ProjectRole> findByProjectIdAndRoleName(
            @Param("projectId") Long projectId,
            @Param("roleName") String roleName
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            update ProjectRole pr
            set pr.recruitCount = pr.recruitCount - 1
            where pr.project.id = :projectId
              and pr.role.id = :roleId
              and pr.recruitCount > 0
            """)
    int decreaseRecruitCountIfAvailable(
            @Param("projectId") Long projectId,
            @Param("roleId") Long roleId
    );

    boolean existsByProjectIdAndRoleIdAndRecruitCount(Long projectId, Long roleId, Integer recruitCount);
}
