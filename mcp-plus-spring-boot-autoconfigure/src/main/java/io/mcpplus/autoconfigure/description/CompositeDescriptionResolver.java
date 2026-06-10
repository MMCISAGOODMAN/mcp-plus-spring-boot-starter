package io.mcpplus.autoconfigure.description;

import io.mcpplus.autoconfigure.properties.McpPlusProperties;
import io.mcpplus.autoconfigure.properties.McpPlusProperties.DescriptionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositeDescriptionResolver {

    private static final Logger log = LoggerFactory.getLogger(CompositeDescriptionResolver.class);

    private final List<DescriptionSource> sourceOrder;
    private final Map<DescriptionSource, DescriptionResolver> builtInResolvers;
    private final List<DescriptionResolver> customResolvers;

    public CompositeDescriptionResolver(McpPlusProperties properties) {
        this(properties, List.of());
    }

    public CompositeDescriptionResolver(McpPlusProperties properties, List<DescriptionResolver> customResolvers) {
        this.sourceOrder = properties.getDescriptionSources();
        this.builtInResolvers = new EnumMap<>(DescriptionSource.class);
        this.builtInResolvers.put(DescriptionSource.MCP_EXPOSE, new McpExposeDescriptionResolver());
        this.builtInResolvers.put(DescriptionSource.SWAGGER, new SwaggerDescriptionResolver());
        this.builtInResolvers.put(DescriptionSource.JAVADOC, new JavadocDescriptionResolver());
        this.customResolvers = customResolvers == null ? List.of() : List.copyOf(customResolvers);
    }

    public ToolDescription resolve(Method method, Class<?> beanClass, boolean javadocEnabled) {
        ResolutionState state = new ResolutionState();

        for (DescriptionSource source : sourceOrder) {
            if (source == DescriptionSource.JAVADOC && !javadocEnabled) {
                continue;
            }
            merge(state, builtInResolvers.get(source), method, beanClass);
        }

        for (DescriptionResolver resolver : customResolvers) {
            merge(state, resolver, method, beanClass);
        }

        if (state.toolDescription.isBlank()) {
            state.toolDescription = "Invoke " + beanClass.getSimpleName() + "#" + method.getName();
            log.debug("Using fallback description for {}#{}", beanClass.getSimpleName(), method.getName());
        }

        return new ToolDescription(state.toolDescription, state.returnDescription, state.parameterDescriptions);
    }

    private static void merge(ResolutionState state, DescriptionResolver resolver, Method method, Class<?> beanClass) {
        if (resolver == null || !resolver.isAvailable()) {
            return;
        }
        ToolDescription resolved = resolver.resolve(method, beanClass);
        if (state.toolDescription.isBlank() && resolved.hasToolDescription()) {
            state.toolDescription = resolved.toolDescription();
        }
        if (state.returnDescription.isBlank() && resolved.returnDescription() != null
                && !resolved.returnDescription().isBlank()) {
            state.returnDescription = resolved.returnDescription();
        }
        resolved.parameterDescriptions().forEach((name, description) -> {
            if (!state.parameterDescriptions.containsKey(name) && description != null && !description.isBlank()) {
                state.parameterDescriptions.put(name, description);
            }
        });
    }

    private static final class ResolutionState {
        private String toolDescription = "";
        private String returnDescription = "";
        private final Map<String, String> parameterDescriptions = new HashMap<>();
    }
}
