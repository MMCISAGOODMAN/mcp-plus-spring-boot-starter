package io.mcpplus.autoconfigure.description;

import java.lang.reflect.Method;

/**
 * Strategy for extracting MCP tool descriptions from methods.
 */
public interface DescriptionResolver {

    /**
     * Whether this resolver is available on the current classpath.
     */
    boolean isAvailable();

    /**
     * Resolve tool description for the given method.
     */
    ToolDescription resolve(Method method, Class<?> beanClass);
}
