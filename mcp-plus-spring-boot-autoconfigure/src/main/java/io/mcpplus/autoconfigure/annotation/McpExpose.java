package io.mcpplus.autoconfigure.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fine-grained control for MCP tool exposure on Controller/Service methods.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpExpose {

    /**
     * Custom MCP tool name. When empty, the global naming strategy applies.
     */
    String name() default "";

    /**
     * Custom MCP tool description. Takes highest priority when configured in description sources.
     */
    String description() default "";

    /**
     * Whether this method (or all methods on the type) should be exposed as an MCP tool.
     */
    boolean enabled() default true;

    /**
     * Required roles for invoking this tool. Requires Spring Security on the classpath.
     */
    String[] roles() default {};
}
