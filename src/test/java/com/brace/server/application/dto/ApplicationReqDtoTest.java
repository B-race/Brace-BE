package com.brace.server.application.dto;

import com.brace.server.application.dto.ApplicationReqDto.Status;
import com.brace.server.application.entity.ApplicationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationReqDtoTest {

    @Test
    @DisplayName("to application status converts pass")
    void toApplicationStatusConvertsPass() {
        Status request = new Status("PASS");

        assertThat(request.toApplicationStatus()).isEqualTo(ApplicationStatus.PASS);
    }

    @Test
    @DisplayName("to application status converts fail")
    void toApplicationStatusConvertsFail() {
        Status request = new Status("FAIL");

        assertThat(request.toApplicationStatus()).isEqualTo(ApplicationStatus.FAIL);
    }

    @Test
    @DisplayName("to application status throws for unknown status name")
    void toApplicationStatusThrowsForUnknownStatusName() {
        Status request = new Status("UNKNOWN");

        assertThatThrownBy(request::toApplicationStatus)
                .isInstanceOf(IllegalArgumentException.class);
    }
}
