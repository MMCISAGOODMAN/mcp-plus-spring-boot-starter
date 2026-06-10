package io.mcpplus.autoconfigure.scanner;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Detects reactive return types (Mono / Flux / Publisher), aligned with Spring AI MCP annotations.
 */
public final class McpReactiveTypeUtils {

    private static final String MONO_CLASS = "reactor.core.publisher.Mono";
    private static final String FLUX_CLASS = "reactor.core.publisher.Flux";
    private static final String PUBLISHER_CLASS = "org.reactivestreams.Publisher";

    private McpReactiveTypeUtils() {
    }

    public static boolean isReactiveReturnType(Method method) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof Class<?> clazz) {
            return isReactiveRawType(clazz);
        }
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> clazz) {
                return isReactiveRawType(clazz);
            }
        }
        return false;
    }

    private static boolean isReactiveRawType(Class<?> rawType) {
        return isAssignableFrom(MONO_CLASS, rawType)
                || isAssignableFrom(FLUX_CLASS, rawType)
                || isAssignableFrom(PUBLISHER_CLASS, rawType);
    }

    private static boolean isAssignableFrom(String className, Class<?> target) {
        try {
            Class<?> reactiveType = Class.forName(className);
            return reactiveType.isAssignableFrom(target);
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
