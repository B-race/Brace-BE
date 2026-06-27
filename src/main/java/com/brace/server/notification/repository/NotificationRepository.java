package com.brace.server.notification.repository;

import com.brace.server.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    void deleteByUser_Id(Long userId);

    void deleteByApplication_User_Id(Long userId);

    void deleteByApplication_Project_User_Id(Long userId);
}
