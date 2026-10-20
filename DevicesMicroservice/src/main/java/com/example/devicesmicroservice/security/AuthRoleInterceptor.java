package com.example.devicesmicroservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
public class AuthRoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        Method method = hm.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = hm.getBeanType().getAnnotation(RequireRole.class);
        }

        if (requireRole == null) {
            return true;
        }

        String roleHeader = request.getHeader("X-User-Role");
        if (roleHeader == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing role");
            return false;
        }

        String required = requireRole.value();
        if (!roleHeader.equalsIgnoreCase(required)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Insufficient role");
            return false;
        }
        return true;
    }
}

