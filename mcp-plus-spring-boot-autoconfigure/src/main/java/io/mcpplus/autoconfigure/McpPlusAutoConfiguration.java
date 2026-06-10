package io.mcpplus.autoconfigure;

import io.mcpplus.autoconfigure.description.CompositeDescriptionResolver;
import io.mcpplus.autoconfigure.description.DescriptionResolver;
import io.mcpplus.autoconfigure.description.McpExposeDescriptionResolver;
import io.mcpplus.autoconfigure.description.SwaggerDescriptionResolver;
import io.mcpplus.autoconfigure.description.JavadocDescriptionResolver;
import io.mcpplus.autoconfigure.properties.McpPlusProperties;
import io.mcpplus.autoconfigure.registry.McpPlusToolRegistry;
import io.mcpplus.autoconfigure.scanner.McpMethodFilter;
import io.mcpplus.autoconfigure.scanner.McpServerModeResolver;
import io.mcpplus.autoconfigure.scanner.McpServerTypeResolver;
import io.mcpplus.autoconfigure.scanner.McpToolNameGenerator;
import io.mcpplus.autoconfigure.scanner.McpToolScanner;
import io.mcpplus.autoconfigure.security.McpRoleChecker;
import io.mcpplus.autoconfigure.tool.McpPlusToolCallbackProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.List;

@AutoConfiguration
@AutoConfigureBefore(name = "org.springframework.ai.mcp.server.common.autoconfigure.ToolCallbackConverterAutoConfiguration")
@ConditionalOnClass(ToolCallbackProvider.class)
@ConditionalOnProperty(prefix = "mcp.plus", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(McpPlusProperties.class)
public class McpPlusAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(McpPlusAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public McpPlusToolRegistry mcpPlusToolRegistry() {
        return new McpPlusToolRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public McpServerTypeResolver mcpServerTypeResolver(McpPlusProperties properties, Environment environment) {
        return new McpServerTypeResolver(properties, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public McpServerModeResolver mcpServerModeResolver(McpPlusProperties properties, Environment environment) {
        return new McpServerModeResolver(properties, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public McpMethodFilter mcpMethodFilter(McpPlusProperties properties,
            McpServerTypeResolver serverTypeResolver,
            McpServerModeResolver serverModeResolver) {
        return new McpMethodFilter(properties, serverTypeResolver, serverModeResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public McpToolNameGenerator mcpToolNameGenerator(McpPlusProperties properties) {
        return new McpToolNameGenerator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CompositeDescriptionResolver compositeDescriptionResolver(McpPlusProperties properties,
            ObjectProvider<DescriptionResolver> descriptionResolvers) {
        List<DescriptionResolver> customResolvers = descriptionResolvers.orderedStream()
                .filter(resolver -> !(resolver instanceof McpExposeDescriptionResolver
                        || resolver instanceof SwaggerDescriptionResolver
                        || resolver instanceof JavadocDescriptionResolver
                        || resolver instanceof CompositeDescriptionResolver))
                .toList();
        return new CompositeDescriptionResolver(properties, customResolvers);
    }

    @Bean
    @ConditionalOnMissingBean
    public McpRoleChecker mcpRoleChecker() {
        return new McpRoleChecker();
    }

    @Bean
    @ConditionalOnMissingBean(name = "mcpPlusToolCallbackProvider")
    public ToolCallbackProvider mcpPlusToolCallbackProvider(ApplicationContext applicationContext,
            McpPlusProperties properties,
            McpMethodFilter methodFilter,
            McpToolNameGenerator nameGenerator,
            CompositeDescriptionResolver descriptionResolver,
            McpRoleChecker roleChecker,
            McpPlusToolRegistry toolRegistry) {
        McpToolScanner scanner = new McpToolScanner(
                applicationContext, properties, methodFilter, nameGenerator, descriptionResolver, roleChecker,
                toolRegistry);
        ToolCallback[] callbacks = scanner.scan().toArray(ToolCallback[]::new);
        log.info("MCP Plus auto-configuration completed with {} tool callback(s)", callbacks.length);
        return new McpPlusToolCallbackProvider(callbacks);
    }
}
