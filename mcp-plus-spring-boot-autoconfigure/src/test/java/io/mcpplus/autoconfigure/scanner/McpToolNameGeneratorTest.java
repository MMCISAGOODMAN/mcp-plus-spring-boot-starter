package io.mcpplus.autoconfigure.scanner;

import io.mcpplus.autoconfigure.annotation.McpExpose;
import io.mcpplus.autoconfigure.properties.McpPlusProperties;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class McpToolNameGeneratorTest {

    private final McpPlusProperties properties = new McpPlusProperties();
    private final McpToolNameGenerator generator = new McpToolNameGenerator(properties);

    @Test
    void shouldGenerateClassMethodNameByDefault() throws Exception {
        Method method = DemoService.class.getMethod("findById", Long.class);
        assertThat(generator.generate(method, DemoService.class)).isEqualTo("demoService_findById");
    }

    @Test
    void shouldUseCustomNameFromAnnotation() throws Exception {
        Method method = DemoService.class.getMethod("listAll");
        assertThat(generator.generate(method, DemoService.class)).isEqualTo("custom_list");
    }

    @Service
    static class DemoService {
        public String findById(Long id) {
            return "user";
        }

        @McpExpose(name = "custom_list")
        public String listAll() {
            return "all";
        }
    }
}
