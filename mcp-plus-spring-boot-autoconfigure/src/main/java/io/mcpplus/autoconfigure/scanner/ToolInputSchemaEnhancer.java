package io.mcpplus.autoconfigure.scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * Merges parameter descriptions into a JSON Schema document.
 */
public final class ToolInputSchemaEnhancer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ToolInputSchemaEnhancer() {
    }

    public static String enhance(String inputSchema, Map<String, String> parameterDescriptions) {
        if (parameterDescriptions == null || parameterDescriptions.isEmpty()) {
            return inputSchema;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(inputSchema);
            if (!(root instanceof ObjectNode objectNode)) {
                return inputSchema;
            }
            JsonNode propertiesNode = objectNode.get("properties");
            if (!(propertiesNode instanceof ObjectNode properties)) {
                return inputSchema;
            }
            parameterDescriptions.forEach((paramName, description) -> {
                JsonNode propertyNode = properties.get(paramName);
                if (propertyNode instanceof ObjectNode propertyObject && description != null && !description.isBlank()) {
                    propertyObject.put("description", description);
                }
            });
            return OBJECT_MAPPER.writeValueAsString(objectNode);
        }
        catch (Exception ex) {
            return inputSchema;
        }
    }
}
