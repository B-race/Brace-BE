package com.brace.server.project.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoleRequest {
    private Long roleId;
    private Integer recruitCount;
}
