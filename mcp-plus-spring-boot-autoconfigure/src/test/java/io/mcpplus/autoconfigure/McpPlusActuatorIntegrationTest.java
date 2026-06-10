package io.mcpplus.autoconfigure;

import io.mcpplus.autoconfigure.registry.McpPlusToolRegistry;
import io.mcpplus.autoconfigure.scanner.InheritanceDedupStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = McpPlusActuatorIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "mcp.plus.enabled=true",
        "mcp.plus.base-packages=io.mcpplus.autoconfigure.support",
        "spring.ai.mcp.server.type=SYNC",
        "management.endpoints.web.exposure.include=mcpplus"
})
class McpPlusActuatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private McpPlusToolRegistry toolRegistry;

    @Test
    void shouldExposeToolsViaActuatorEndpoint() throws Exception {
        assertThat(toolRegistry.size()).isGreaterThan(0);

        mockMvc.perform(get("/actuator/mcpplus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").isNumber())
                .andExpect(jsonPath("$.tools[0].name").exists())
                .andExpect(jsonPath("$.tools[0].description").exists())
                .andExpect(jsonPath("$.tools[0].beanClass").exists());
    }

    @SpringBootApplication
    @Import(McpPlusAutoConfiguration.class)
    static class TestApplication {
    }
}
