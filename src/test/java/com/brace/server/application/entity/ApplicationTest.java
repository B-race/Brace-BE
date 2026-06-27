package com.brace.server.application.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest {

    @Test
    @DisplayName("default status is progress")
    void defaultStatusIsProgress() {
        Application application = Application.builder()
                .message("지원합니다.")
                .build();

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.PROGRESS);
    }

    @Test
    @DisplayName("pass changes status to pass")
    void passChangesStatusToPass() {
        Application application = Application.builder()
                .message("지원합니다.")
                .build();

        application.pass();

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.PASS);
    }

    @Test
    @DisplayName("fail changes status to fail")
    void failChangesStatusToFail() {
        Application application = Application.builder()
                .message("지원합니다.")
                .build();

        application.fail();

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.FAIL);
    }

    @Test
    @DisplayName("cancel changes status to cancel")
    void cancelChangesStatusToCancel() {
        Application application = Application.builder()
                .message("지원합니다.")
                .build();

        application.cancel();

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.CANCEL);
    }
}
