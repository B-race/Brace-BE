package com.brace.server.application.controller;

import com.brace.server.application.service.ApplicationService;
import com.brace.server.auth.jwt.JwtTokenProvider;
import com.brace.server.auth.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    @DisplayName("create application returns created response")
    void createApplicationReturnsCreatedResponse() throws Exception {
        when(applicationService.createApplication(eq(1L), any()))
                .thenReturn(new com.brace.server.application.dto.ApplicationResDto.ApplicationId(10L));

        mockMvc.perform(post("/projects/{projectId}/applications", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "backend",
                                  "message": "지원합니다."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.errorCode").value("application201"))
                .andExpect(jsonPath("$.message").value("지원이 완료되었습니다."))
                .andExpect(jsonPath("$.result.applicationId").value(10L));
    }

    @Test
    @WithMockUser
    @DisplayName("get applications returns application slice")
    void getApplicationsReturnsApplicationSlice() throws Exception {
        com.brace.server.application.dto.ApplicationResDto.Applicant applicant =
                new com.brace.server.application.dto.ApplicationResDto.Applicant(
                        2L,
                        10L,
                        "applicant",
                        "backend",
                        List.of(),
                        "지원합니다.",
                        "https://example.com/portfolio",
                        "https://example.com/profile.png",
                        "소개"
                );
        com.brace.server.application.dto.ApplicationResDto.ApplicationSummary summary =
                new com.brace.server.application.dto.ApplicationResDto.ApplicationSummary(
                        11L,
                        1L,
                        "프로젝트",
                        "RECRUITING",
                        "PROGRESS",
                        null,
                        applicant
                );
        when(applicationService.getApplications(1L, 20L, 2))
                .thenReturn(new com.brace.server.application.dto.ApplicationResDto.ApplicationSlice(
                        List.of(summary),
                        2,
                        null,
                        false
                ));

        mockMvc.perform(get("/projects/{projectId}/applications", 1L)
                        .param("cursorId", "20")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.errorCode").value("application200"))
                .andExpect(jsonPath("$.message").value("지원자 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.result.size").value(2))
                .andExpect(jsonPath("$.result.hasNext").value(false))
                .andExpect(jsonPath("$.result.content[0].applicationId").value(11L))
                .andExpect(jsonPath("$.result.content[0].applicant.userId").value(2L));
    }

    @Test
    @WithMockUser
    @DisplayName("get applications rejects invalid query parameters")
    void getApplicationsRejectsInvalidQueryParameters() throws Exception {
        mockMvc.perform(get("/projects/{projectId}/applications", 1L)
                        .param("cursorId", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.errorCode").value("common400"));
    }

    @Test
    @WithMockUser
    @DisplayName("cancel application returns canceled response")
    void cancelApplicationReturnsCanceledResponse() throws Exception {
        when(applicationService.cancelApplication(10L))
                .thenReturn(new com.brace.server.application.dto.ApplicationResDto.ApplicationStatus(10L, "CANCEL"));

        mockMvc.perform(delete("/applications/{applicationId}", 10L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.errorCode").value("application200"))
                .andExpect(jsonPath("$.message").value("지원이 취소되었습니다."))
                .andExpect(jsonPath("$.result.applicationId").value(10L))
                .andExpect(jsonPath("$.result.status").value("CANCEL"));
    }

    @Test
    @WithMockUser
    @DisplayName("update application returns updated response")
    void updateApplicationReturnsUpdatedResponse() throws Exception {
        when(applicationService.updateApplication(eq(10L), any()))
                .thenReturn(new com.brace.server.application.dto.ApplicationResDto.ApplicationResult(
                        10L,
                        "PASS",
                        "applicant@example.com"
                ));

        mockMvc.perform(patch("/applications/{applicationId}", 10L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PASS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.errorCode").value("application200"))
                .andExpect(jsonPath("$.message").value("지원 상태가 변경되었습니다."))
                .andExpect(jsonPath("$.result.applicationId").value(10L))
                .andExpect(jsonPath("$.result.status").value("PASS"))
                .andExpect(jsonPath("$.result.applicantEmail").value("applicant@example.com"));

        verify(applicationService).updateApplication(eq(10L), any());
    }

    @Test
    @WithMockUser
    @DisplayName("create application rejects blank role")
    void createApplicationRejectsBlankRole() throws Exception {
        mockMvc.perform(post("/projects/{projectId}/applications", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "",
                                  "message": "지원합니다."
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message").value("지원 역할은 필수입니다."));
    }

    @Test
    @WithMockUser
    @DisplayName("create application rejects blank message")
    void createApplicationRejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/projects/{projectId}/applications", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "backend",
                                  "message": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message").value("지원 메시지는 필수입니다."));
    }

    @Test
    @WithMockUser
    @DisplayName("update application rejects invalid status")
    void updateApplicationRejectsInvalidStatus() throws Exception {
        mockMvc.perform(patch("/applications/{applicationId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CANCEL"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message").value("지원 상태는 PASS 또는 FAIL이어야 합니다."));
    }
}
