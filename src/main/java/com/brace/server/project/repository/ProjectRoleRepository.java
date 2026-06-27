package com.brace.server.project.repository;

import com.brace.server.project.entity.Project;
import com.brace.server.project.entity.ProjectRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {

    List<ProjectRole> findByProject(Project project);

    void deleteByProject(Project project);
}
