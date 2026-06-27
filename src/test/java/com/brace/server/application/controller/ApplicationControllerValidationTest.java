package com.brace.server.application.controller;

import com.brace.server.application.service.ApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

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
