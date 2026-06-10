package io.mcpplus.autoconfigure.description;

import io.mcpplus.autoconfigure.annotation.McpExpose;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class McpExposeDescriptionResolver implements DescriptionResolver {

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ToolDescription resolve(Method method, Class<?> beanClass) {
        McpExpose methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, McpExpose.class);
        McpExpose typeAnnotation = AnnotatedElementUtils.findMergedAnnotation(beanClass, McpExpose.class);

        String description = firstNonBlank(
                methodAnnotation != null ? methodAnnotation.description() : null,
                typeAnnotation != null ? typeAnnotation.description() : null);

        return new ToolDescription(description, "", Map.of());
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
