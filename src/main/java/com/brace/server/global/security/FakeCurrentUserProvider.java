package com.brace.server.global.security;

import com.brace.server.global.code.ApplicationErrorCode;
import com.brace.server.global.exception.ProjectException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class FakeCurrentUserProvider {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ProjectException(ApplicationErrorCode.UNAUTHORIZED);
        }

        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw new ProjectException(ApplicationErrorCode.UNAUTHORIZED);
        }
    }
}
