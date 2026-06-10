package io.mcpplus.autoconfigure.actuator;

import io.mcpplus.autoconfigure.registry.McpPlusToolDescriptor;
import io.mcpplus.autoconfigure.registry.McpPlusToolRegistry;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Endpoint(id = "mcpplus")
public class McpPlusToolsEndpoint {

    private final McpPlusToolRegistry toolRegistry;

    public McpPlusToolsEndpoint(McpPlusToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @ReadOperation
    public Map<String, Object> tools() {
        List<McpPlusToolDescriptor> tools = toolRegistry.getTools();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", tools.size());
        response.put("tools", tools);
        return response;
    }
}
