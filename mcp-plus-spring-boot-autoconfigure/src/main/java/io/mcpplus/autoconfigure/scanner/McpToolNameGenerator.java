package io.mcpplus.autoconfigure.scanner;

import io.mcpplus.autoconfigure.annotation.McpExpose;
import io.mcpplus.autoconfigure.properties.McpPlusProperties;
import io.mcpplus.autoconfigure.properties.McpPlusProperties.ToolNameStrategy;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class McpToolNameGenerator {

    private final McpPlusProperties properties;

    public McpToolNameGenerator(McpPlusProperties properties) {
        this.properties = properties;
    }

    public String generate(java.lang.reflect.Method method, Class<?> beanClass) {
        McpExpose methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, McpExpose.class);
        if (methodAnnotation != null && StringUtils.hasText(methodAnnotation.name())) {
            return sanitize(methodAnnotation.name());
        }

        ToolNameStrategy strategy = properties.getToolNameStrategy();
        if (strategy == ToolNameStrategy.CUSTOM) {
            strategy = ToolNameStrategy.CLASS_METHOD;
        }

        return switch (strategy) {
            case METHOD_NAME -> sanitize(method.getName());
            case CLASS_METHOD -> sanitize(ClassUtils.getShortNameAsProperty(beanClass) + "_" + method.getName());
            case CUSTOM -> sanitize(ClassUtils.getShortNameAsProperty(beanClass) + "_" + method.getName());
        };
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
