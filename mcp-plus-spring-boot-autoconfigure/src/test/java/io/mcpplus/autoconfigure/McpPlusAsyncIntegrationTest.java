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

@SpringBootTest(classes = McpPlusAsyncIntegrationTest.TestApplication.class)
@TestPropertySource(properties = {
        "mcp.plus.enabled=true",
        "mcp.plus.base-packages=io.mcpplus.autoconfigure.support",
        "spring.ai.mcp.server.type=ASYNC"
})
class McpPlusAsyncIntegrationTest {

    @Autowired
    private ToolCallbackProvider mcpPlusToolCallbackProvider;

    @Test
    void shouldRegisterOnlyReactiveToolsOnAsyncServer() {
        Set<String> toolNames = Arrays.stream(mcpPlusToolCallbackProvider.getToolCallbacks())
                .map(callback -> callback.getToolDefinition().name())
                .collect(Collectors.toSet());

        assertThat(toolNames)
                .contains("sampleCatalogService_asyncLookup", "sampleOrderService_asyncStatus")
                .doesNotContain("sampleCatalogService_findProduct", "sampleOrderService_createOrder");
    }

    @Test
    void shouldInvokeReactiveToolOnAsyncServer() {
        ToolCallback callback = Arrays.stream(mcpPlusToolCallbackProvider.getToolCallbacks())
                .filter(tool -> "sampleCatalogService_asyncLookup".equals(tool.getToolDefinition().name()))
                .findFirst()
                .orElseThrow();

        String result = callback.call("{\"id\": \"p-2\"}");
        assertThat(result).contains("async:p-2");
    }

    @SpringBootApplication
    @Import(McpPlusAutoConfiguration.class)
    static class TestApplication {
    }
}
