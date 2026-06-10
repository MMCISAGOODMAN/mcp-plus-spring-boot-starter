package io.mcpplus.autoconfigure;

import io.mcpplus.autoconfigure.registry.McpPlusToolRegistry;
import io.mcpplus.autoconfigure.support.ExtendedCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = McpPlusInheritanceIntegrationTest.TestApplication.class)
@TestPropertySource(properties = {
        "mcp.plus.enabled=true",
        "mcp.plus.base-packages=io.mcpplus.autoconfigure.support",
        "mcp.plus.bean-types=SERVICE",
        "mcp.plus.inheritance-dedup-strategy=SUBCLASS_WINS",
        "mcp.plus.tool-name-strategy=METHOD_NAME",
        "spring.ai.mcp.server.type=SYNC"
})
class McpPlusInheritanceIntegrationTest {

    @Autowired
    private McpPlusToolRegistry toolRegistry;

    @Test
    void shouldPreferSubclassWhenBothBeansExist() {
        Set<String> toolNames = toolRegistry.getTools().stream()
                .map(tool -> tool.name())
                .collect(Collectors.toSet());

        assertThat(toolNames).contains("baseLookup", "extendedOnly");
        assertThat(toolRegistry.getTools().stream()
                .filter(tool -> "baseLookup".equals(tool.name()))
                .map(tool -> tool.beanClass())
                .distinct()
                .toList()).containsOnly(ExtendedCatalogService.class.getName());
    }

    @SpringBootApplication
    @Import(McpPlusAutoConfiguration.class)
    static class TestApplication {
    }
}
