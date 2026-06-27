package com.brace.server.user.service;

import com.brace.server.application.repository.ApplicationRepository;
import com.brace.server.auth.repository.RefreshTokenRepository;
import com.brace.server.notification.repository.NotificationRepository;
import com.brace.server.project.repository.BookmarkRepository;
import com.brace.server.project.repository.ProjectRepository;
import com.brace.server.user.entity.User;
import com.brace.server.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCleanupScheduler {
    private static final int USER_RETENTION_DAYS = 30;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationRepository notificationRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredWithdrawnUsers() {
        LocalDateTime deletedBefore = LocalDateTime.now().minusDays(USER_RETENTION_DAYS);
        List<User> expiredUsers = userRepository.findAllByDeletedAtBefore(deletedBefore);

        expiredUsers.forEach(this::deleteExpiredWithdrawnUser);
    }

    private void deleteExpiredWithdrawnUser(User user) {
        Long userId = user.getId();

        notificationRepository.deleteByApplication_Project_User_Id(userId);
        notificationRepository.deleteByApplication_User_Id(userId);
        notificationRepository.deleteByUser_Id(userId);

        refreshTokenRepository.deleteByUserId(userId);
        bookmarkRepository.deleteByProject_User_Id(userId);
        bookmarkRepository.deleteByUser_Id(userId);
        applicationRepository.deleteByProject_User_Id(userId);
        applicationRepository.deleteByUser_Id(userId);
        projectRepository.deleteByUser_Id(userId);
        userRepository.delete(user);
    }
}
