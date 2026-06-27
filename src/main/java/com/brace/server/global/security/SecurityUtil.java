package com.brace.server.global.security;

import com.brace.server.global.exception.ProjectException;
import com.brace.server.project.exception.ProjectErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {}

    // TODO: Authentication 구현체 완성 후 principal 추출 방식 조정 필요
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ProjectException(ProjectErrorCode.UNAUTHORIZED);
        }
        return Long.parseLong(auth.getName());
    }
}
