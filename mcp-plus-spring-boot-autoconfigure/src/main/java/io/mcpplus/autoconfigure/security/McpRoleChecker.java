package io.mcpplus.autoconfigure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class McpRoleChecker {

    private static final Logger log = LoggerFactory.getLogger(McpRoleChecker.class);

    private static final boolean SECURITY_AVAILABLE = isClassPresent(
            "org.springframework.security.core.context.SecurityContextHolder");

    public boolean isAvailable() {
        return SECURITY_AVAILABLE;
    }

    public void checkAccess(String[] requiredRoles) {
        if (requiredRoles == null || requiredRoles.length == 0) {
            return;
        }
        if (!SECURITY_AVAILABLE) {
            log.warn("Spring Security is not on the classpath; skipping role check for roles {}",
                    Arrays.toString(requiredRoles));
            return;
        }

        try {
            Class<?> securityContextHolder = Class.forName(
                    "org.springframework.security.core.context.SecurityContextHolder");
            Object context = securityContextHolder.getMethod("getContext").invoke(null);
            Object authentication = context.getClass().getMethod("getAuthentication").invoke(context);
            if (authentication == null
                    || !(boolean) authentication.getClass().getMethod("isAuthenticated").invoke(authentication)) {
                throw accessDenied("Authentication required to invoke MCP tool");
            }

            @SuppressWarnings("unchecked")
            Set<String> authorities = ((java.util.Collection<Object>) authentication.getClass()
                    .getMethod("getAuthorities")
                    .invoke(authentication)).stream()
                    .map(authority -> {
                        try {
                            return (String) authority.getClass().getMethod("getAuthority").invoke(authority);
                        }
                        catch (ReflectiveOperationException ex) {
                            throw new IllegalStateException("Failed to read granted authority", ex);
                        }
                    })
                    .collect(Collectors.toSet());

            boolean allowed = Arrays.stream(requiredRoles)
                    .anyMatch(role -> authorities.contains(role) || authorities.contains("ROLE_" + role));
            if (!allowed) {
                throw accessDenied("Insufficient roles to invoke MCP tool. Required: "
                        + Arrays.toString(requiredRoles));
            }
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to perform MCP role check", ex);
        }
    }

    private static RuntimeException accessDenied(String message) {
        try {
            Class<?> exceptionClass = Class.forName("org.springframework.security.access.AccessDeniedException");
            return (RuntimeException) exceptionClass.getConstructor(String.class).newInstance(message);
        }
        catch (ReflectiveOperationException ex) {
            return new IllegalStateException(message, ex);
        }
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, McpRoleChecker.class.getClassLoader());
            return true;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
