package io.mcpplus.autoconfigure.tool;

import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.ai.tool.execution.ToolCallResultConverter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Unwraps Mono / Flux / Publisher results before delegating to the default converter.
 */
public class ReactiveToolCallResultConverter implements ToolCallResultConverter {

    private static final String MONO_CLASS = "reactor.core.publisher.Mono";
    private static final String FLUX_CLASS = "reactor.core.publisher.Flux";
    private static final String PUBLISHER_CLASS = "org.reactivestreams.Publisher";

    private final ToolCallResultConverter delegate = new DefaultToolCallResultConverter();

    @Override
    public String convert(Object result, Type returnType) {
        Object unwrapped = unwrapReactiveResult(result, returnType);
        Type unwrappedType = unwrapReactiveReturnType(returnType);
        return delegate.convert(unwrapped, unwrappedType);
    }

    private static Object unwrapReactiveResult(Object result, Type returnType) {
        if (result == null || returnType == null) {
            return result;
        }
        if (!isReactiveType(returnType)) {
            return result;
        }
        try {
            Class<?> monoClass = Class.forName(MONO_CLASS);
            if (monoClass.isInstance(result)) {
                Method block = monoClass.getMethod("block");
                return block.invoke(result);
            }
            Class<?> fluxClass = Class.forName(FLUX_CLASS);
            if (fluxClass.isInstance(result)) {
                Method collectList = fluxClass.getMethod("collectList");
                Object mono = collectList.invoke(result);
                Method block = monoClass.getMethod("block");
                return block.invoke(mono);
            }
        }
        catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to unwrap reactive MCP tool result", ex);
        }
        return result;
    }

    private static Type unwrapReactiveReturnType(Type returnType) {
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> clazz && isReactiveRawType(clazz)) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length == 1) {
                    return typeArguments[0];
                }
            }
        }
        return returnType;
    }

    private static boolean isReactiveType(Type returnType) {
        if (returnType instanceof Class<?> clazz) {
            return isReactiveRawType(clazz);
        }
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            return rawType instanceof Class<?> clazz && isReactiveRawType(clazz);
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
            Class<?> type = Class.forName(className);
            return type.isAssignableFrom(target);
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
