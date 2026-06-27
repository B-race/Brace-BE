package com.brace.server.notification.dto;

import com.brace.server.notification.dto.NotificationReqDto.NotificationSearch;
import com.brace.server.notification.dto.NotificationResDto.NotificationSlice;
import com.brace.server.notification.entity.Notification;
import com.brace.server.notification.entity.NotificationType;
import com.brace.server.user.entity.ParticipationType;
import com.brace.server.user.entity.SocialProvider;
import com.brace.server.user.entity.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("notification search applies default size")
    void notificationSearchAppliesDefaultSize() {
        NotificationSearch request = new NotificationSearch(null, null, null);

        assertThat(request.size()).isEqualTo(20);
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    @DisplayName("notification search rejects invalid cursor and size")
    void notificationSearchRejectsInvalidCursorAndSize() {
        NotificationSearch request = new NotificationSearch(0L, 101, null);

        Set<ConstraintViolation<NotificationSearch>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "커서 ID는 1 이상이어야 합니다.",
                        "조회 개수는 1 이상 100 이하이어야 합니다."
                );
    }

    @Test
    @DisplayName("notification slice from maps notifications and next cursor")
    void notificationSliceFromMapsNotificationsAndNextCursor() {
        Notification first = notification(1L, "첫 번째");
        Notification second = notification(2L, "두 번째");
        Notification third = notification(3L, "세 번째");

        NotificationSlice response = NotificationSlice.from(List.of(third, second, first), 2);

        assertThat(response.size()).isEqualTo(2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursorId()).isEqualTo(second.getId());
        assertThat(response.content()).extracting("id").containsExactly(third.getId(), second.getId());
        assertThat(response.content()).extracting("content").containsExactly("세 번째", "두 번째");
    }

    @Test
    @DisplayName("notification slice from returns no next cursor without next page")
    void notificationSliceFromReturnsNoNextCursorWithoutNextPage() {
        Notification notification = notification(1L, "알림");

        NotificationSlice response = NotificationSlice.from(List.of(notification), 2);

        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursorId()).isNull();
        assertThat(response.content()).hasSize(1);
    }

    private Notification notification(Long id, String content) {
        return Notification.builder()
                .id(id)
                .type(NotificationType.SYSTEM)
                .content(content)
                .user(user())
                .build();
    }

    private User user() {
        return User.builder()
                .id(1L)
                .email("user@example.com")
                .password("password")
                .socialProvider(SocialProvider.NONE)
                .socialId("social")
                .name("user")
                .role("개발자")
                .profileImageUrl("https://example.com/profile.png")
                .participationType(ParticipationType.BOTH)
                .introduction("소개")
                .portfolioUrl("https://example.com/portfolio")
                .profileCompleted(true)
                .build();
    }
}
