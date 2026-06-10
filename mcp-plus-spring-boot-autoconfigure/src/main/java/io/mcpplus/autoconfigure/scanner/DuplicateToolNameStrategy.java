package io.mcpplus.autoconfigure.scanner;

/**
 * Strategy when duplicate MCP tool names are detected during scanning.
 */
public enum DuplicateToolNameStrategy {

    /**
     * Fail fast with {@link IllegalStateException}.
     */
    FAIL,

    /**
     * Skip the duplicate and keep the first registered tool.
     */
    SKIP
}
