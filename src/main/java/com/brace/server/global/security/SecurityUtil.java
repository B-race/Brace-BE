package com.brace.server.global.security;

import com.brace.server.auth.security.CustomUserDetails;
import com.brace.server.global.exception.ProjectException;
import com.brace.server.project.exception.ProjectErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {}

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ProjectException(ProjectErrorCode.UNAUTHORIZED);
        }
        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }
        throw new ProjectException(ProjectErrorCode.UNAUTHORIZED);
    }
}
