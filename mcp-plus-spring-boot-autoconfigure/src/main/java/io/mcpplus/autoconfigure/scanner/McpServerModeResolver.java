package io.mcpplus.autoconfigure.scanner;

import io.mcpplus.autoconfigure.properties.McpPlusProperties;
import org.springframework.core.env.Environment;

/**
 * Resolves whether the MCP server runs in stateless mode.
 */
public class McpServerModeResolver {

    private static final String MCP_SERVER_PROTOCOL_PROPERTY = "spring.ai.mcp.server.protocol";

    private final boolean stateless;

    public McpServerModeResolver(McpPlusProperties properties, Environment environment) {
        if (properties.getStateless() != null) {
            this.stateless = properties.getStateless();
        }
        else {
            String protocol = environment.getProperty(MCP_SERVER_PROTOCOL_PROPERTY, "");
            this.stateless = "STATELESS".equalsIgnoreCase(protocol);
        }
    }

    public boolean isStateless() {
        return stateless;
    }
}
