package io.mcpplus.autoconfigure;

import io.mcpplus.autoconfigure.support.SampleCatalogService;
import io.mcpplus.autoconfigure.support.SampleOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = McpPlusIntegrationTest.TestApplication.class)
@TestPropertySource(properties = {
        "mcp.plus.enabled=true",
        "mcp.plus.base-packages=io.mcpplus.autoconfigure.support",
        "mcp.plus.bean-types=ALL",
        "spring.ai.mcp.server.type=SYNC"
})
class McpPlusIntegrationTest {

    @Autowired
    private ToolCallbackProvider mcpPlusToolCallbackProvider;

    @Test
    void shouldRegisterExpectedSyncTools() {
        Set<String> toolNames = Arrays.stream(mcpPlusToolCallbackProvider.getToolCallbacks())
                .map(callback -> callback.getToolDefinition().name())
                .collect(Collectors.toSet());

        assertThat(toolNames)
                .contains("sampleCatalogService_findProduct", "sampleOrderService_createOrder")
                .doesNotContain("sampleCatalogService_hiddenProduct", "sampleOrderService_asyncStatus");
    }

    @Test
    void shouldInvokeRegisteredTool() {
        ToolCallback callback = Arrays.stream(mcpPlusToolCallbackProvider.getToolCallbacks())
                .filter(tool -> "sampleCatalogService_findProduct".equals(tool.getToolDefinition().name()))
                .findFirst()
                .orElseThrow();

        String result = callback.call("{\"id\": \"p-1\"}");
        assertThat(result).contains("p-1");
    }

    @SpringBootApplication
    @Import(McpPlusAutoConfiguration.class)
    static class TestApplication {
    }
}
