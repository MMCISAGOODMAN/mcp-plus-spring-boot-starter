package io.mcpplus.autoconfigure.actuator;

import io.mcpplus.autoconfigure.properties.McpPlusActuatorProperties;
import io.mcpplus.autoconfigure.registry.McpPlusToolRegistry;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
@EnableConfigurationProperties(McpPlusActuatorProperties.class)
@ConditionalOnProperty(prefix = "mcp.plus.actuator", name = "enabled", havingValue = "true", matchIfMissing = true)
public class McpPlusActuatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    public McpPlusToolsEndpoint mcpPlusToolsEndpoint(McpPlusToolRegistry toolRegistry) {
        return new McpPlusToolsEndpoint(toolRegistry);
    }
}
