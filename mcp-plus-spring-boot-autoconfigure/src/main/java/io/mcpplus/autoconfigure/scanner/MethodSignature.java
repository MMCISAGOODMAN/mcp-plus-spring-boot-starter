package io.mcpplus.autoconfigure.scanner;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Stable method signature for inheritance deduplication.
 */
public record MethodSignature(String name, String parameterTypes) {

    public static MethodSignature of(Method method) {
        String params = Arrays.stream(method.getParameterTypes())
                .map(Class::getName)
                .collect(Collectors.joining(","));
        return new MethodSignature(method.getName(), params);
    }
}
