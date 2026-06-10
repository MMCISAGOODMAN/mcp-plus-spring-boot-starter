package io.mcpplus.autoconfigure.scanner;

/**
 * MCP Server operation mode, aligned with {@code spring.ai.mcp.server.type}.
 */
public enum McpServerType {

    SYNC,
    ASYNC;

    public static McpServerType from(String value) {
        if (value == null || value.isBlank()) {
            return SYNC;
        }
        return switch (value.trim().toUpperCase()) {
            case "ASYNC" -> ASYNC;
            default -> SYNC;
        };
    }
}
