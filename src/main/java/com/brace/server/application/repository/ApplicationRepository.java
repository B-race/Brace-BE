package com.brace.server.application.repository;

import com.brace.server.application.entity.Application;
import com.brace.server.application.entity.ApplicationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByUserIdAndProjectIdAndRoleId(
            Long userId,
            Long projectId,
            Long roleId
    );

    @Query("""
            select a
            from Application a
            join fetch a.user
            join fetch a.role
            where a.project.id = :projectId
              and (:cursorId is null or a.id < :cursorId)
            order by a.id desc
            """)
    List<Application> findByProjectIdWithCursor(
            @Param("projectId") Long projectId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            select a
            from Application a
            join fetch a.user
            where a.id = :applicationId
            """)
    java.util.Optional<Application> findByIdWithUser(@Param("applicationId") Long applicationId);

    @Query("""
            select a
            from Application a
            join fetch a.user
            join fetch a.project p
            join fetch p.user
            join fetch a.role
            where a.id = :applicationId
            """)
    java.util.Optional<Application> findByIdWithUserProjectRole(@Param("applicationId") Long applicationId);

    @Query("""
            select a
            from Application a
            join fetch a.user
            join fetch a.project
            where a.project.id = :projectId
              and a.role.id = :roleId
              and a.status = :status
              and a.id <> :excludedApplicationId
            """)
    List<Application> findByProjectIdAndRoleIdAndStatusExcept(
            @Param("projectId") Long projectId,
            @Param("roleId") Long roleId,
            @Param("status") ApplicationStatus status,
            @Param("excludedApplicationId") Long excludedApplicationId
    );


    @Query("""
            select a from Application a
            join fetch a.project
            join fetch a.role
            where a.user.id = :userId
            and (:status is null or a.status = :status)
            order by a.createdAt desc
            """)
    org.springframework.data.domain.Page<Application> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") ApplicationStatus status,
            Pageable pageable
    );

    @Query("SELECT a.role.id, COUNT(a) FROM Application a WHERE a.project.id = :projectId " +
            "AND a.status = :status GROUP BY a.role.id")
    List<Object[]> countByProjectIdAndStatusGroupByRole(
            @Param("projectId") Long projectId,
            @Param("status") ApplicationStatus status);

    Integer countByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);

    void deleteByProject_User_Id(Long userId);
}
