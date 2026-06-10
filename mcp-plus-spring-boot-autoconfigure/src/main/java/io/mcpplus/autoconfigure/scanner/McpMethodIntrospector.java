package io.mcpplus.autoconfigure.scanner;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for discovering candidate MCP tool methods on Spring beans.
 */
public final class McpMethodIntrospector {

    private McpMethodIntrospector() {
    }

    public static List<Method> getCandidateMethods(Class<?> beanClass, InheritanceDedupStrategy strategy) {
        if (strategy == null || strategy == InheritanceDedupStrategy.DECLARED_ONLY) {
            return List.of(getDeclaredCandidateMethods(beanClass));
        }
        return getHierarchyCandidateMethods(beanClass);
    }

    private static Method[] getDeclaredCandidateMethods(Class<?> beanClass) {
        Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(beanClass, ReflectionUtils.USER_DECLARED_METHODS);
        sortMethods(methods);
        return methods;
    }

    private static List<Method> getHierarchyCandidateMethods(Class<?> beanClass) {
        Map<MethodSignature, Method> methodsBySignature = new LinkedHashMap<>();
        Class<?> current = beanClass;
        while (current != null && current != Object.class) {
            Method[] declaredMethods = ReflectionUtils.getUniqueDeclaredMethods(current,
                    ReflectionUtils.USER_DECLARED_METHODS);
            for (Method method : declaredMethods) {
                methodsBySignature.putIfAbsent(MethodSignature.of(method), method);
            }
            current = current.getSuperclass();
        }
        List<Method> methods = new ArrayList<>(methodsBySignature.values());
        methods.sort(Comparator.comparing(Method::getName).thenComparing(McpMethodIntrospector::methodSignature));
        return methods;
    }

    private static void sortMethods(Method[] methods) {
        java.util.Arrays.sort(methods,
                Comparator.comparing(Method::getName).thenComparing(McpMethodIntrospector::methodSignature));
    }

    private static String methodSignature(Method method) {
        return MethodSignature.of(method).parameterTypes();
    }
}
