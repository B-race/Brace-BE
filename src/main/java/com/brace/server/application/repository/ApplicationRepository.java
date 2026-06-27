package com.brace.server.application.repository;

import com.brace.server.application.entity.Application;
import com.brace.server.application.entity.ApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("SELECT a.role.id, COUNT(a) FROM Application a WHERE a.project.id = :projectId " +
            "AND a.status = :status GROUP BY a.role.id")
    List<Object[]> countByProjectIdAndStatusGroupByRole(
            @Param("projectId") Long projectId,
            @Param("status") ApplicationStatus status);

    Integer countByUser_Id(Long userId);
}
