package io.mcpplus.autoconfigure.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a class or method from MCP Plus auto-exposure.
 * <p>
 * When applied at class level, all public methods on the bean are excluded.
 * When applied at method level, only that method is excluded.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpExclude {
}
