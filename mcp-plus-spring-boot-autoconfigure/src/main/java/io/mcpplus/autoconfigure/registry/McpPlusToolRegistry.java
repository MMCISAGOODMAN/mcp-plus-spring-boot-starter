package io.mcpplus.autoconfigure.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry of MCP Plus tools discovered during scanning.
 */
public class McpPlusToolRegistry {

    private final List<McpPlusToolDescriptor> tools = new ArrayList<>();

    public void register(McpPlusToolDescriptor descriptor) {
        tools.add(descriptor);
    }

    public List<McpPlusToolDescriptor> getTools() {
        return Collections.unmodifiableList(tools);
    }

    public int size() {
        return tools.size();
    }
}
