package com.brace.server.notification.repository;

import com.brace.server.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            select n
            from Notification n
            left join fetch n.application
            where n.user.id = :userId
              and (:cursorId is null or n.id < :cursorId)
              and (:isRead is null or n.isRead = :isRead)
            order by n.id desc
            """)
    List<Notification> findByUserIdWithCursor(
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            @Param("isRead") Boolean isRead,
            Pageable pageable
    );

    @Query("""
            select n
            from Notification n
            join fetch n.user
            where n.id = :notificationId
            """)
    Optional<Notification> findByIdWithUser(@Param("notificationId") Long notificationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Notification n
            set n.isRead = true
            where n.user.id = :userId
              and n.isRead = false
            """)
    int markAllAsReadByUserId(@Param("userId") Long userId);

    void deleteByUser_Id(Long userId);

    void deleteByApplication_User_Id(Long userId);

    void deleteByApplication_Project_User_Id(Long userId);
}
