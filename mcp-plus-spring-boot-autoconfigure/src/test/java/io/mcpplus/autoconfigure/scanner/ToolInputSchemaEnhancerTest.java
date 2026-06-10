package io.mcpplus.autoconfigure.scanner;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ToolInputSchemaEnhancerTest {

    @Test
    void shouldInjectParameterDescriptionsWithoutBreakingSchema() {
        String schema = """
                {
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "type": "object",
                  "properties": {
                    "id": { "type": "string" }
                  },
                  "required": [ "id" ],
                  "additionalProperties": false
                }
                """;

        String enhanced = ToolInputSchemaEnhancer.enhance(schema, Map.of("id", "Product id"));

        assertThat(enhanced).contains("\"description\":\"Product id\"");
        assertThatCode(() -> new com.fasterxml.jackson.databind.ObjectMapper().readTree(enhanced))
                .doesNotThrowAnyException();
    }
}
