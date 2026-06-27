package com.brace.server.project.service;

import com.brace.server.application.entity.ApplicationStatus;
import com.brace.server.application.repository.ApplicationRepository;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.global.security.SecurityUtil;
import com.brace.server.project.converter.ProjectConverter;
import com.brace.server.project.dto.request.ProjectCreateRequest;
import com.brace.server.project.dto.request.ProjectUpdateRequest;
import com.brace.server.project.dto.request.RoleRequest;
import com.brace.server.project.dto.response.ProjectDetailResponse;
import com.brace.server.project.dto.response.ProjectSummaryResponse;
import com.brace.server.project.dto.response.RecruitmentStatusResponse;
import com.brace.server.project.entity.ActivityType;
import com.brace.server.project.entity.Bookmark;
import com.brace.server.project.entity.MeetingType;
import com.brace.server.project.entity.Project;
import com.brace.server.project.entity.ProjectRole;
import com.brace.server.project.exception.ProjectErrorCode;
import com.brace.server.project.repository.BookmarkRepository;
import com.brace.server.project.repository.ProjectRepository;
import com.brace.server.project.repository.ProjectRoleRepository;
import com.brace.server.project.repository.ProjectSpecification;
import com.brace.server.user.entity.Role;
import com.brace.server.user.entity.User;
import com.brace.server.user.repository.RoleRepository;
import com.brace.server.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public Long createProject(ProjectCreateRequest request) {
        validateContestFields(request.getActivityType(), request.getProjectName(), request.getProjectUrl());

        User user = findUser(SecurityUtil.getCurrentUserId());
        Project project = projectRepository.save(ProjectConverter.toEntity(request, user));
        saveProjectRoles(project, request.getRoles());

        return project.getId();
    }

    public Page<ProjectSummaryResponse> getProjects(
            String sort,
            ActivityType activityType,
            Long roleId,
            MeetingType meetingType,
            String keyword,
            List<String> tags,
            int page,
            int size
    ) {
        Sort sortOrder = switch (sort) {
            case "latest" -> Sort.by("createdAt").descending();
            case "deadline_asc" -> Sort.by("deadline").ascending();
            case "deadline_desc" -> Sort.by("deadline").descending();
            case "view_count" -> Sort.by("viewCount").descending();
            case "bookmark_count" -> Sort.unsorted();
            default -> throw new ProjectException(ProjectErrorCode.INVALID_SORT_TYPE);
        };

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Specification<Project> spec = ProjectSpecification.filter(activityType, roleId, meetingType, keyword, tags, sort);

        return projectRepository.findAll(spec, pageable)
                .map(ProjectConverter::toSummaryResponse);
    }

    @Transactional
    public ProjectDetailResponse getProject(Long projectId) {
        Project project = findActiveProject(projectId);
        project.incrementViewCount();
        return ProjectConverter.toDetailResponse(project, buildRecruitmentStatus(project));
    }

    @Transactional
    public ProjectDetailResponse updateProject(Long projectId, ProjectUpdateRequest request) {
        Project project = findActiveProject(projectId);
        verifyOwner(project);

        project.update(
                request.getActivityType(),
                request.getTitle(),
                request.getDescription(),
                request.getProjectName(),
                request.getProjectUrl(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDeadline(),
                request.getMeetingType(),
                request.getTags()
        );

        validateContestFields(project.getActivityType(), project.getProjectName(), project.getProjectUrl());

        if (request.getRoles() != null) {
            projectRoleRepository.deleteByProject(project);
            saveProjectRoles(project, request.getRoles());
        }

        return ProjectConverter.toDetailResponse(project, buildRecruitmentStatus(project));
    }

    @Transactional
    public void deleteProject(Long projectId) {
        Project project = findActiveProject(projectId);
        verifyOwner(project);
        project.softDelete();
    }

    @Transactional
    public void addBookmark(Long projectId) {
        User user = findUser(SecurityUtil.getCurrentUserId());
        Project project = findActiveProject(projectId);

        if (bookmarkRepository.existsByUserAndProject(user, project)) {
            throw new ProjectException(ProjectErrorCode.BOOKMARK_ALREADY_EXISTS);
        }

        bookmarkRepository.save(Bookmark.builder().user(user).project(project).build());
    }

    @Transactional
    public void removeBookmark(Long projectId) {
        User user = findUser(SecurityUtil.getCurrentUserId());
        Project project = findActiveProject(projectId);

        Bookmark bookmark = bookmarkRepository.findByUserAndProject(user, project)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.BOOKMARK_NOT_FOUND));

        bookmarkRepository.delete(bookmark);
    }

    private void validateContestFields(ActivityType activityType, String projectName, String projectUrl) {
        if (ActivityType.CONTEST.equals(activityType)) {
            if (projectName == null || projectName.isBlank() || projectUrl == null || projectUrl.isBlank()) {
                throw new ProjectException(ProjectErrorCode.CONTEST_FIELDS_REQUIRED);
            }
        }
    }

    private void saveProjectRoles(Project project, List<RoleRequest> roleRequests) {
        if (roleRequests == null) return;
        for (RoleRequest req : roleRequests) {
            Role role = roleRepository.findById(req.getRoleId())
                    .orElseThrow(() -> new ProjectException(ProjectErrorCode.ROLE_NOT_FOUND));
            projectRoleRepository.save(ProjectRole.builder()
                    .project(project)
                    .role(role)
                    .recruitCount(req.getRecruitCount())
                    .build());
        }
    }

    private List<RecruitmentStatusResponse> buildRecruitmentStatus(Project project) {
        List<Object[]> passCounts = applicationRepository.countByProjectIdAndStatusGroupByRole(
                project.getId(), ApplicationStatus.PASS);
        return ProjectConverter.toRecruitmentStatusResponses(project.getProjectRoles(), passCounts);
    }

    private Project findActiveProject(Long projectId) {
        return projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(ProjectErrorCode.UNAUTHORIZED));
    }

    private void verifyOwner(Project project) {
        if (!project.getUser().getId().equals(SecurityUtil.getCurrentUserId())) {
            throw new ProjectException(ProjectErrorCode.PROJECT_FORBIDDEN);
        }
    }
}
