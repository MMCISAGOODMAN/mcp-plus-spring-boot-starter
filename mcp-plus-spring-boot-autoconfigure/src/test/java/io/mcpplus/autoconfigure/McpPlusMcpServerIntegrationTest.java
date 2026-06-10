package io.mcpplus.autoconfigure;

import io.mcpplus.autoconfigure.support.SampleCatalogService;
import io.mcpplus.autoconfigure.support.SampleOrderService;
import io.modelcontextprotocol.server.McpServerFeatures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = McpPlusMcpServerIntegrationTest.TestApplication.class)
@TestPropertySource(properties = {
        "mcp.plus.enabled=true",
        "mcp.plus.base-packages=io.mcpplus.autoconfigure.support",
        "spring.ai.mcp.server.enabled=true",
        "spring.ai.mcp.server.type=SYNC",
        "spring.ai.mcp.server.stdio=true",
        "spring.main.web-application-type=none"
})
class McpPlusMcpServerIntegrationTest {

    @Autowired(required = false)
    private List<McpServerFeatures.SyncToolSpecification> syncTools;

    @Test
    void shouldExposeMcpPlusToolsThroughMcpServer() {
        assertThat(syncTools).isNotNull();

        Set<String> toolNames = syncTools.stream()
                .map(spec -> spec.tool().name())
                .collect(Collectors.toSet());

        assertThat(toolNames)
                .contains("sampleCatalogService_findProduct", "sampleOrderService_createOrder")
                .doesNotContain("sampleCatalogService_hiddenProduct", "sampleCatalogService_asyncLookup");
    }

    @SpringBootApplication
    @Import(McpPlusAutoConfiguration.class)
    static class TestApplication {
    }
}
