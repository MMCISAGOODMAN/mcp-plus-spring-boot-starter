package io.mcpplus.autoconfigure.description;

import java.util.Collections;
import java.util.Map;

/**
 * Resolved tool and parameter descriptions for MCP tool registration.
 */
public record ToolDescription(
        String toolDescription,
        String returnDescription,
        Map<String, String> parameterDescriptions) {

    public ToolDescription {
        parameterDescriptions = parameterDescriptions == null
                ? Collections.emptyMap()
                : Map.copyOf(parameterDescriptions);
    }

    public static ToolDescription empty() {
        return new ToolDescription("", "", Map.of());
    }

    public boolean hasToolDescription() {
        return toolDescription != null && !toolDescription.isBlank();
    }
}
