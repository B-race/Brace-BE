package com.brace.server.application.service;

import com.brace.server.application.dto.ApplicationReqDto.Message;
import com.brace.server.application.dto.ApplicationReqDto.Status;
import com.brace.server.application.dto.ApplicationResDto;
import com.brace.server.application.dto.ApplicationResDto.ApplicationId;
import com.brace.server.application.dto.ApplicationResDto.ApplicationResult;
import com.brace.server.application.dto.ApplicationResDto.ApplicationSlice;
import com.brace.server.application.dto.ApplicationResDto.ApplicationSummary;
import com.brace.server.application.entity.Application;
import com.brace.server.application.entity.ApplicationStatus;
import com.brace.server.application.repository.ApplicationRepository;
import com.brace.server.global.code.ApplicationErrorCode;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.global.security.SecurityUtil;
import com.brace.server.notification.entity.Notification;
import com.brace.server.notification.entity.NotificationType;
import com.brace.server.notification.repository.NotificationRepository;
import com.brace.server.project.entity.Project;
import com.brace.server.project.entity.ProjectRole;
import com.brace.server.project.entity.ProjectStatus;
import com.brace.server.project.repository.ProjectRepository;
import com.brace.server.project.repository.ProjectRoleRepository;
import com.brace.server.user.entity.User;
import com.brace.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public ApplicationId createApplication(Long projectId, Message request) {
        Long applicantId = SecurityUtil.getCurrentUserId();

        // 지원할 프로젝트 역할과 프로젝트를 함께 조회한다.
        ProjectRole projectRole = projectRoleRepository.findByProjectIdAndRoleName(projectId, request.role())
                .orElseThrow(() -> new ProjectException(ApplicationErrorCode.PROJECT_ROLE_NOT_FOUND));
        Project project = projectRole.getProject();

        // 프로젝트 상태와 모집 조건이 지원 가능한 상태인지 확인한다.
        if (project.getUser().getId().equals(applicantId)) {
            throw new ProjectException(ApplicationErrorCode.SELF_APPLICATION_NOT_ALLOWED);
        }
        if (project.getStatus() != ProjectStatus.RECRUITING) {
            throw new ProjectException(ApplicationErrorCode.RECRUITMENT_CLOSED);
        }
        if (project.getDeadline().isBefore(LocalDate.now())) {
            throw new ProjectException(ApplicationErrorCode.DEADLINE_PASSED);
        }
        if (!projectRole.hasRecruitCount()) {
            throw new ProjectException(ApplicationErrorCode.RECRUIT_COUNT_EXHAUSTED);
        }

        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ProjectException(ApplicationErrorCode.USER_NOT_FOUND));

        // 한 번이라도 지원한 프로젝트 역할에는 다시 지원할 수 없다.
        boolean duplicated = applicationRepository.existsByUserIdAndProjectIdAndRoleId(
                applicantId,
                project.getId(),
                projectRole.getRole().getId()
        );
        if (duplicated) {
            throw new ProjectException(ApplicationErrorCode.DUPLICATE_APPLICATION);
        }

        // 지원서를 저장하고 프로젝트 작성자에게 새 지원자 알림을 보낸다.
        Application application = Application.builder()
                .message(request.message())
                .user(applicant)
                .project(project)
                .role(projectRole.getRole())
                .build();

        Application savedApplication = applicationRepository.save(application);
        notificationRepository.save(Notification.builder()
                .type(NotificationType.NEW_APPLICANT)
                .content(project.getTitle() + "에 새로운 지원자가 있습니다.")
                .user(project.getUser())
                .application(savedApplication)
                .build());

        return new ApplicationId(savedApplication.getId());
    }

    public ApplicationSlice getApplications(Long projectId, Long cursorId, Integer size) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        // 지원자 목록은 프로젝트 작성자만 조회할 수 있다.
        if (size == null || size < 1) {
            throw new ProjectException(ApplicationErrorCode.INVALID_SIZE);
        }

        Project project = projectRepository.findByIdWithOwner(projectId)
                .orElseThrow(() -> new ProjectException(ApplicationErrorCode.PROJECT_NOT_FOUND));
        if (!project.getUser().getId().equals(currentUserId)) {
            throw new ProjectException(ApplicationErrorCode.PROJECT_OWNER_ONLY_FOR_READ);
        }

        // cursorId보다 작은 지원서를 최신순으로 size + 1개 조회해 다음 페이지 여부를 판단한다.
        List<Application> applications = applicationRepository.findByProjectIdWithCursor(
                projectId,
                cursorId,
                PageRequest.of(0, size + 1)
        );

        boolean hasNext = applications.size() > size;
        List<ApplicationSummary> content = applications.stream()
                .limit(size)
                .map(application -> ApplicationSummary.from(application, project))
                .toList();
        Long nextCursorId = hasNext && !content.isEmpty()
                ? content.getLast().applicationId()
                : null;

        return new ApplicationSlice(content, size, nextCursorId, hasNext);
    }

    @Transactional
    public ApplicationResDto.ApplicationStatus cancelApplication(Long applicationId) {
        Long applicantId = SecurityUtil.getCurrentUserId();

        // 지원자 본인의 진행 중 지원만 취소할 수 있다.
        Application application = applicationRepository.findByIdWithUser(applicationId)
                .orElseThrow(() -> new ProjectException(ApplicationErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getUser().getId().equals(applicantId)) {
            throw new ProjectException(ApplicationErrorCode.APPLICANT_ONLY_FOR_CANCEL);
        }
        if (application.getStatus() != ApplicationStatus.PROGRESS) {
            throw new ProjectException(ApplicationErrorCode.APPLICATION_NOT_CANCELABLE);
        }

        application.cancel();
        return new ApplicationResDto.ApplicationStatus(application.getId(), application.getStatus().name());
    }

    @Transactional
    public ApplicationResult updateApplication(Long applicationId, Status request) {
        Long projectOwnerId = SecurityUtil.getCurrentUserId();
        ApplicationStatus targetStatus = request.toApplicationStatus();

        // 프로젝트 작성자만 진행 중인 지원을 합격 또는 불합격 처리할 수 있다.
        Application application = applicationRepository.findByIdWithUserProjectRole(applicationId)
                .orElseThrow(() -> new ProjectException(ApplicationErrorCode.APPLICATION_NOT_FOUND));
        Project project = application.getProject();
        User applicant = application.getUser();
        Long roleId = application.getRole().getId();

        if (!project.getUser().getId().equals(projectOwnerId)) {
            throw new ProjectException(ApplicationErrorCode.PROJECT_OWNER_ONLY_FOR_UPDATE);
        }
        if (application.getStatus() != ApplicationStatus.PROGRESS) {
            throw new ProjectException(ApplicationErrorCode.APPLICATION_ALREADY_PROCESSED);
        }

        if (targetStatus == ApplicationStatus.PASS) {
            // 합격 처리 시 모집 인원을 먼저 차감해 동시 처리 상황에서도 초과 합격을 막는다.
            int updated = projectRoleRepository.decreaseRecruitCountIfAvailable(
                    project.getId(),
                    roleId
            );
            if (updated == 0) {
                throw new ProjectException(ApplicationErrorCode.RECRUIT_COUNT_EXHAUSTED);
            }

            application.pass();
            notificationRepository.save(Notification.builder()
                    .type(NotificationType.APPLICATION_RESULT)
                    .content(project.getTitle() + " 지원이 수락되었습니다.")
                    .user(applicant)
                    .application(application)
                    .build());

            // 해당 역할의 모집 인원이 소진되면 남은 진행 중 지원을 자동으로 불합격 처리한다.
            boolean recruitmentClosed = projectRoleRepository.existsByProjectIdAndRoleIdAndRecruitCount(
                    project.getId(),
                    roleId,
                    0
            );
            if (recruitmentClosed) {
                List<Application> remainingApplications = applicationRepository.findByProjectIdAndRoleIdAndStatusExcept(
                        project.getId(),
                        roleId,
                        ApplicationStatus.PROGRESS,
                        application.getId()
                );

                List<Notification> notifications = remainingApplications.stream()
                        .peek(Application::fail)
                        .map(rejectedApplication -> Notification.builder()
                                .type(NotificationType.APPLICATION_RESULT)
                                .content(rejectedApplication.getProject().getTitle() + " 지원이 거절되었습니다.")
                                .user(rejectedApplication.getUser())
                                .application(rejectedApplication)
                                .build())
                        .toList();

                notificationRepository.saveAll(notifications);
            }

            return new ApplicationResult(
                    application.getId(),
                    application.getStatus().name(),
                    applicant.getEmail()
            );
        }

        // 불합격은 지원 상태 변경과 결과 알림만 수행한다.
        application.fail();
        notificationRepository.save(Notification.builder()
                .type(NotificationType.APPLICATION_RESULT)
                .content(project.getTitle() + " 지원이 거절되었습니다.")
                .user(applicant)
                .application(application)
                .build());

        return new ApplicationResult(application.getId(), application.getStatus().name(), null);
    }
}
