package com.brace.server.notification.controller;

import com.brace.server.auth.jwt.JwtTokenProvider;
import com.brace.server.auth.security.CustomUserDetails;
import com.brace.server.auth.security.CustomUserDetailsService;
import com.brace.server.notification.dto.NotificationResDto.NotificationResponse;
import com.brace.server.notification.dto.NotificationResDto.NotificationSlice;
import com.brace.server.notification.service.NotificationService;
import com.brace.server.user.entity.ParticipationType;
import com.brace.server.user.entity.SocialProvider;
import com.brace.server.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("get notifications returns notification slice")
    void getNotificationsReturnsNotificationSlice() throws Exception {
        CustomUserDetails userDetails = userDetails(1L);
        NotificationResponse notification = new NotificationResponse(
                10L,
                "SYSTEM",
                "알림",
                false,
                20L,
                null
        );
        when(notificationService.getNotifications(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(new NotificationSlice(List.of(notification), 2, null, false));

        mockMvc.perform(get("/notifications")
                        .with(authentication(authenticationToken(userDetails)))
                        .param("cursorId", "30")
                        .param("size", "2")
                        .param("isRead", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.errorCode").value("notification200"))
                .andExpect(jsonPath("$.message").value("알림 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.result.size").value(2))
                .andExpect(jsonPath("$.result.hasNext").value(false))
                .andExpect(jsonPath("$.result.content[0].id").value(10L))
                .andExpect(jsonPath("$.result.content[0].applicationId").value(20L));
    }

    @Test
    @WithMockUser
    @DisplayName("get notifications rejects missing custom principal")
    void getNotificationsRejectsMissingCustomPrincipal() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("get notifications rejects invalid request")
    void getNotificationsRejectsInvalidRequest() throws Exception {
        CustomUserDetails userDetails = userDetails(1L);

        mockMvc.perform(get("/notifications")
                        .with(authentication(authenticationToken(userDetails)))
                        .param("cursorId", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.errorCode").value("common400"));
    }

    @Test
    @DisplayName("mark as read returns no content")
    void markAsReadReturnsNoContent() throws Exception {
        CustomUserDetails userDetails = userDetails(1L);

        mockMvc.perform(patch("/notifications/{notificationId}/read", 10L)
                        .with(authentication(authenticationToken(userDetails)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(notificationService).markAsRead(1L, 10L);
    }

    @Test
    @DisplayName("mark all as read returns no content")
    void markAllAsReadReturnsNoContent() throws Exception {
        CustomUserDetails userDetails = userDetails(1L);

        mockMvc.perform(patch("/notifications/read")
                        .with(authentication(authenticationToken(userDetails)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(notificationService).markAllAsRead(1L);
    }

    private UsernamePasswordAuthenticationToken authenticationToken(CustomUserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private CustomUserDetails userDetails(Long id) {
        return new CustomUserDetails(User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .password("password")
                .socialProvider(SocialProvider.NONE)
                .socialId("social-" + id)
                .name("user" + id)
                .role("개발자")
                .participationType(ParticipationType.BOTH)
                .profileCompleted(true)
                .build());
    }
}
