package com.brace.server.project.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecruitmentStatusResponse {
    private Long roleId;
    private String roleName;
    private Integer recruitCount;
    private Long currentCount;
}
