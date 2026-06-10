package io.mcpplus.autoconfigure.scanner;

import io.mcpplus.autoconfigure.annotation.McpExclude;
import io.mcpplus.autoconfigure.annotation.McpExpose;
import io.mcpplus.autoconfigure.properties.McpPlusProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class McpMethodFilter {

    private static final Logger log = LoggerFactory.getLogger(McpMethodFilter.class);

    private static final List<String> UNSUPPORTED_PARAMETER_TYPE_NAMES = List.of(
            "jakarta.servlet.ServletRequest",
            "jakarta.servlet.ServletResponse",
            "jakarta.servlet.http.HttpServletRequest",
            "jakarta.servlet.http.HttpServletResponse",
            "org.springframework.web.context.request.WebRequest",
            "org.springframework.ui.Model",
            "org.springframework.validation.BindingResult",
            "java.security.Principal",
            "org.springframework.http.HttpHeaders",
            "org.springframework.web.multipart.MultipartFile");

    private static final List<String> STATELESS_UNSUPPORTED_PARAMETER_TYPE_NAMES = List.of(
            "org.springaicommunity.mcp.context.McpSyncRequestContext",
            "org.springaicommunity.mcp.context.McpAsyncRequestContext",
            "io.modelcontextprotocol.server.McpSyncServerExchange",
            "io.modelcontextprotocol.server.McpAsyncServerExchange",
            "org.springframework.ai.mcp.annotation.McpSyncRequestContext",
            "org.springframework.ai.mcp.annotation.McpAsyncRequestContext");

    private static final List<String> OBJECT_METHOD_NAMES = List.of(
            "equals", "hashCode", "toString", "getClass", "notify", "notifyAll", "wait");

    private final McpPlusProperties properties;
    private final McpServerTypeResolver serverTypeResolver;
    private final McpServerModeResolver serverModeResolver;
    private final List<Pattern> includePatterns;
    private final List<Pattern> excludeMethodPatterns;
    private final List<Pattern> excludeClassPatterns;

    public McpMethodFilter(McpPlusProperties properties,
            McpServerTypeResolver serverTypeResolver,
            McpServerModeResolver serverModeResolver) {
        this.properties = properties;
        this.serverTypeResolver = serverTypeResolver;
        this.serverModeResolver = serverModeResolver;
        this.includePatterns = compilePatterns(properties.getIncludeMethodPatterns());
        this.excludeMethodPatterns = compilePatterns(properties.getExcludeMethodPatterns());
        this.excludeClassPatterns = compilePatterns(properties.getExcludeClassPatterns());
    }

    public boolean isEligibleBean(Class<?> beanClass) {
        Optional<String> reason = getBeanIneligibilityReason(beanClass);
        reason.ifPresent(value -> logFilterDecision(beanClass, null, value));
        return reason.isEmpty();
    }

    public boolean isEligibleMethod(Method method, Class<?> beanClass) {
        Optional<String> reason = getMethodIneligibilityReason(method, beanClass);
        reason.ifPresent(value -> logFilterDecision(beanClass, method, value));
        return reason.isEmpty();
    }

    public String[] requiredRoles(Method method, Class<?> beanClass) {
        McpExpose methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, McpExpose.class);
        if (methodAnnotation != null && methodAnnotation.roles().length > 0) {
            return methodAnnotation.roles();
        }
        McpExpose typeAnnotation = AnnotatedElementUtils.findMergedAnnotation(beanClass, McpExpose.class);
        if (typeAnnotation != null && typeAnnotation.roles().length > 0) {
            return typeAnnotation.roles();
        }
        return new String[0];
    }

    private Optional<String> getBeanIneligibilityReason(Class<?> beanClass) {
        if (beanClass == null || beanClass.isInterface() || beanClass.isAnnotation()) {
            return Optional.of("not a concrete class");
        }
        if (!matchesBasePackage(beanClass)) {
            return Optional.of("outside configured base packages");
        }
        if (matchesExcludeClassPattern(beanClass)) {
            return Optional.of("matched exclude-class-patterns");
        }
        if (isExcludedByMcpExclude(beanClass)) {
            return Optional.of("annotated with @McpExclude");
        }
        return switch (properties.getBeanTypes()) {
            case CONTROLLER -> isController(beanClass) ? Optional.empty() : Optional.of("not a controller bean");
            case SERVICE -> isService(beanClass) ? Optional.empty() : Optional.of("not a service bean");
            case ALL -> (isController(beanClass) || isService(beanClass))
                    ? Optional.empty()
                    : Optional.of("not a controller or service bean");
        };
    }

    private Optional<String> getMethodIneligibilityReason(Method method, Class<?> beanClass) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return Optional.of("not public");
        }
        if (Modifier.isStatic(method.getModifiers())) {
            return Optional.of("static method");
        }
        if (method.isBridge() || method.isSynthetic()) {
            return Optional.of("bridge or synthetic method");
        }
        if (OBJECT_METHOD_NAMES.contains(method.getName())) {
            return Optional.of("object base method");
        }
        if (properties.isExcludeDeprecated() && method.isAnnotationPresent(Deprecated.class)) {
            return Optional.of("@Deprecated");
        }
        if (hasMcpToolAnnotation(method)) {
            return Optional.of("already annotated with @McpTool");
        }
        if (!isEnabledByMcpExpose(method, beanClass)) {
            return Optional.of("@McpExpose(enabled=false)");
        }
        if (!matchesIncludePattern(method.getName())) {
            return Optional.of("did not match include-method-patterns");
        }
        if (matchesExcludePattern(method.getName())) {
            return Optional.of("matched exclude-method-patterns");
        }
        if (hasUnsupportedParameter(method)) {
            return Optional.of("contains unsupported parameter type");
        }
        if (serverModeResolver.isStateless() && hasStatelessUnsupportedParameter(method)) {
            return Optional.of("contains stateless-unsupported context parameter");
        }
        if (isExcludedByMcpExclude(method)) {
            return Optional.of("annotated with @McpExclude");
        }
        if (!serverTypeResolver.matchesServerType(method)) {
            return Optional.of("return type incompatible with server type "
                    + serverTypeResolver.getServerType());
        }
        return Optional.empty();
    }

    private void logFilterDecision(Class<?> beanClass, Method method, String reason) {
        if (!properties.isDebug() || !log.isDebugEnabled()) {
            return;
        }
        if (method == null) {
            log.debug("Skipping bean {}: {}", beanClass.getName(), reason);
        }
        else {
            log.debug("Skipping method {}#{}: {}", beanClass.getSimpleName(), method.getName(), reason);
        }
    }

    private boolean isEnabledByMcpExpose(Method method, Class<?> beanClass) {
        McpExpose methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, McpExpose.class);
        if (methodAnnotation != null && !methodAnnotation.enabled()) {
            return false;
        }
        McpExpose typeAnnotation = AnnotatedElementUtils.findMergedAnnotation(beanClass, McpExpose.class);
        return typeAnnotation == null || typeAnnotation.enabled();
    }

    private static boolean isExcludedByMcpExclude(Class<?> beanClass) {
        return AnnotatedElementUtils.hasAnnotation(beanClass, McpExclude.class);
    }

    private static boolean isExcludedByMcpExclude(Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, McpExclude.class);
    }

    private boolean matchesBasePackage(Class<?> beanClass) {
        List<String> basePackages = properties.getBasePackages();
        if (basePackages == null || basePackages.isEmpty()) {
            return true;
        }
        String className = ClassUtils.getUserClass(beanClass).getName();
        return basePackages.stream().anyMatch(pkg -> className.startsWith(normalizePackage(pkg)));
    }

    private boolean matchesExcludeClassPattern(Class<?> beanClass) {
        if (excludeClassPatterns.isEmpty()) {
            return false;
        }
        String className = ClassUtils.getUserClass(beanClass).getName();
        return excludeClassPatterns.stream().anyMatch(pattern -> pattern.matcher(className).matches());
    }

    private static String normalizePackage(String pkg) {
        return pkg.endsWith(".") ? pkg.substring(0, pkg.length() - 1) : pkg;
    }

    private static boolean isController(Class<?> beanClass) {
        return AnnotatedElementUtils.hasAnnotation(beanClass, RestController.class)
                || AnnotatedElementUtils.hasAnnotation(beanClass, Controller.class);
    }

    private static boolean isService(Class<?> beanClass) {
        return AnnotatedElementUtils.hasAnnotation(beanClass, Service.class);
    }

    private boolean matchesIncludePattern(String methodName) {
        if (includePatterns.isEmpty()) {
            return true;
        }
        return includePatterns.stream().anyMatch(pattern -> pattern.matcher(methodName).matches());
    }

    private boolean matchesExcludePattern(String methodName) {
        return excludeMethodPatterns.stream().anyMatch(pattern -> pattern.matcher(methodName).matches());
    }

    private static boolean hasUnsupportedParameter(Method method) {
        return Arrays.stream(method.getParameterTypes()).anyMatch(McpMethodFilter::isUnsupportedParameterType);
    }

    private static boolean hasStatelessUnsupportedParameter(Method method) {
        return Arrays.stream(method.getParameterTypes()).anyMatch(McpMethodFilter::isStatelessUnsupportedParameterType);
    }

    private static boolean isUnsupportedParameterType(Class<?> type) {
        return matchesAnyAssignableType(UNSUPPORTED_PARAMETER_TYPE_NAMES, type);
    }

    private static boolean isStatelessUnsupportedParameterType(Class<?> type) {
        return matchesAnyAssignableType(STATELESS_UNSUPPORTED_PARAMETER_TYPE_NAMES, type);
    }

    private static boolean matchesAnyAssignableType(List<String> typeNames, Class<?> target) {
        for (String typeName : typeNames) {
            try {
                Class<?> unsupportedType = Class.forName(typeName);
                if (unsupportedType.isAssignableFrom(target)) {
                    return true;
                }
            }
            catch (ClassNotFoundException ignored) {
                // Optional type not on classpath
            }
        }
        return false;
    }

    private static boolean hasMcpToolAnnotation(Method method) {
        for (String annotationName : List.of(
                "org.springframework.ai.mcp.annotation.McpTool",
                "org.springaicommunity.mcp.annotation.McpTool")) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends java.lang.annotation.Annotation> annotationClass =
                        (Class<? extends java.lang.annotation.Annotation>) Class.forName(annotationName);
                if (AnnotatedElementUtils.findMergedAnnotation(method, annotationClass) != null) {
                    return true;
                }
            }
            catch (ClassNotFoundException ignored) {
                // MCP annotation not on classpath
            }
        }
        return false;
    }

    private static List<Pattern> compilePatterns(List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return List.of();
        }
        return patterns.stream().map(Pattern::compile).toList();
    }
}
