package io.mcpplus.autoconfigure.registry;

/**
 * Read-only metadata for a registered MCP Plus tool.
 */
public record McpPlusToolDescriptor(
        String name,
        String description,
        String beanClass,
        String declaringClass,
        String method,
        String inputSchema) {
}
