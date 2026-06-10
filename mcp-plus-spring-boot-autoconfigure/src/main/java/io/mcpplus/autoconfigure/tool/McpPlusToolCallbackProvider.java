package io.mcpplus.autoconfigure.tool;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

public class McpPlusToolCallbackProvider implements ToolCallbackProvider {

    private final ToolCallback[] toolCallbacks;

    public McpPlusToolCallbackProvider(ToolCallback[] toolCallbacks) {
        this.toolCallbacks = toolCallbacks;
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        return toolCallbacks;
    }
}
