package io.mcpplus.autoconfigure.scanner;

import io.mcpplus.autoconfigure.properties.McpPlusProperties;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;

public class McpServerTypeResolver {

    private static final String MCP_SERVER_TYPE_PROPERTY = "spring.ai.mcp.server.type";

    private final McpServerType serverType;

    public McpServerTypeResolver(McpPlusProperties properties, Environment environment) {
        if (properties.getServerType() != null) {
            this.serverType = properties.getServerType();
        }
        else {
            this.serverType = McpServerType.from(environment.getProperty(MCP_SERVER_TYPE_PROPERTY, "SYNC"));
        }
    }

    public McpServerType getServerType() {
        return serverType;
    }

    public boolean matchesServerType(Method method) {
        boolean reactive = McpReactiveTypeUtils.isReactiveReturnType(method);
        return switch (serverType) {
            case SYNC -> !reactive;
            case ASYNC -> reactive;
        };
    }
}
