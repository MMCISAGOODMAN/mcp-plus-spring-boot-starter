package io.mcpplus.autoconfigure.scanner;

import io.mcpplus.autoconfigure.description.CompositeDescriptionResolver;
import io.mcpplus.autoconfigure.description.ToolDescription;
import io.mcpplus.autoconfigure.properties.McpPlusProperties;
import io.mcpplus.autoconfigure.registry.McpPlusToolDescriptor;
import io.mcpplus.autoconfigure.registry.McpPlusToolRegistry;
import io.mcpplus.autoconfigure.security.McpRoleChecker;
import io.mcpplus.autoconfigure.tool.McpPlusToolCallback;
import io.mcpplus.autoconfigure.tool.ReactiveToolCallResultConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class McpToolScanner {

    private static final Logger log = LoggerFactory.getLogger(McpToolScanner.class);

    private final ApplicationContext applicationContext;
    private final McpPlusProperties properties;
    private final McpMethodFilter methodFilter;
    private final McpToolNameGenerator nameGenerator;
    private final CompositeDescriptionResolver descriptionResolver;
    private final McpRoleChecker roleChecker;
    private final McpPlusToolRegistry toolRegistry;

    public McpToolScanner(ApplicationContext applicationContext,
            McpPlusProperties properties,
            McpMethodFilter methodFilter,
            McpToolNameGenerator nameGenerator,
            CompositeDescriptionResolver descriptionResolver,
            McpRoleChecker roleChecker,
            McpPlusToolRegistry toolRegistry) {
        this.applicationContext = applicationContext;
        this.properties = properties;
        this.methodFilter = methodFilter;
        this.nameGenerator = nameGenerator;
        this.descriptionResolver = descriptionResolver;
        this.roleChecker = roleChecker;
        this.toolRegistry = toolRegistry;
    }

    public List<ToolCallback> scan() {
        List<ToolCallback> callbacks = new ArrayList<>();
        Map<String, String> registeredTools = new LinkedHashMap<>();
        Collection<Object> candidateBeans = collectCandidateBeans().values();
        InheritanceDedupStrategy inheritanceStrategy = properties.getInheritanceDedupStrategy();

        for (Object bean : candidateBeans) {
            Class<?> beanClass = AopUtils.getTargetClass(bean);
            if (!methodFilter.isEligibleBean(beanClass)) {
                continue;
            }

            for (Method method : McpMethodIntrospector.getCandidateMethods(ClassUtils.getUserClass(beanClass),
                    inheritanceStrategy)) {
                if (InheritanceDedupHelper.isShadowedBySubclassBean(method, ClassUtils.getUserClass(beanClass),
                        candidateBeans, inheritanceStrategy)) {
                    log.debug("Skipping {}#{} shadowed by subclass bean", beanClass.getSimpleName(), method.getName());
                    continue;
                }
                if (!methodFilter.isEligibleMethod(method, beanClass)) {
                    continue;
                }
                String toolName = nameGenerator.generate(method, beanClass);
                if (registeredTools.containsKey(toolName)) {
                    handleDuplicateToolName(toolName, beanClass, method, registeredTools.get(toolName));
                    continue;
                }

                ToolCallback callback = buildToolCallback(bean, beanClass, method, toolName);
                registeredTools.put(toolName, beanClass.getName() + "#" + method.getName());
                callbacks.add(callback);
                log.info("Registered MCP Plus tool '{}' from {}#{}", toolName, beanClass.getSimpleName(),
                        method.getName());
            }
        }

        log.info("MCP Plus registered {} tool(s)", callbacks.size());
        return callbacks;
    }

    private Map<String, Object> collectCandidateBeans() {
        Map<String, Object> beans = new LinkedHashMap<>();
        switch (properties.getBeanTypes()) {
            case SERVICE -> beans.putAll(applicationContext.getBeansWithAnnotation(Service.class));
            case CONTROLLER -> {
                beans.putAll(applicationContext.getBeansWithAnnotation(RestController.class));
                beans.putAll(applicationContext.getBeansWithAnnotation(Controller.class));
            }
            case ALL -> {
                beans.putAll(applicationContext.getBeansWithAnnotation(Service.class));
                beans.putAll(applicationContext.getBeansWithAnnotation(RestController.class));
                beans.putAll(applicationContext.getBeansWithAnnotation(Controller.class));
            }
        }
        return beans;
    }

    private ToolCallback buildToolCallback(Object bean, Class<?> beanClass, Method method, String toolName) {
        ToolDescription description = descriptionResolver.resolve(method, beanClass, properties.isJavadocEnabled());
        ToolDefinition toolDefinition = buildToolDefinition(method, toolName, description);

        MethodToolCallback.Builder callbackBuilder = MethodToolCallback.builder()
                .toolDefinition(toolDefinition)
                .toolMethod(method)
                .toolObject(bean);
        if (McpReactiveTypeUtils.isReactiveReturnType(method)) {
            callbackBuilder.toolCallResultConverter(new ReactiveToolCallResultConverter());
        }

        MethodToolCallback delegate = callbackBuilder.build();
        toolRegistry.register(new McpPlusToolDescriptor(
                toolDefinition.name(),
                toolDefinition.description(),
                beanClass.getName(),
                method.getDeclaringClass().getName(),
                method.getName(),
                toolDefinition.inputSchema()));

        String[] roles = methodFilter.requiredRoles(method, beanClass);
        return roles.length > 0 ? new McpPlusToolCallback(delegate, roleChecker, roles) : delegate;
    }

    private void handleDuplicateToolName(String toolName, Class<?> beanClass, Method method, String existingSource) {
        String currentSource = beanClass.getName() + "#" + method.getName();
        String message = "Duplicate MCP tool name '" + toolName + "' from " + currentSource
                + " (already registered by " + existingSource + ")";
        if (properties.getDuplicateToolNameStrategy() == DuplicateToolNameStrategy.SKIP) {
            log.warn("{}. Skipping duplicate.", message);
            return;
        }
        throw new IllegalStateException(message);
    }

    private ToolDefinition buildToolDefinition(Method method, String toolName, ToolDescription description) {
        var builder = ToolDefinitions.builder(method)
                .name(toolName)
                .description(buildFullDescription(description));

        if (!description.parameterDescriptions().isEmpty()) {
            builder.inputSchema(buildInputSchema(method, description));
        }
        return builder.build();
    }

    private static String buildFullDescription(ToolDescription description) {
        StringBuilder fullDescription = new StringBuilder(description.toolDescription());
        if (description.returnDescription() != null && !description.returnDescription().isBlank()) {
            fullDescription.append(" Returns: ").append(description.returnDescription());
        }
        return fullDescription.toString();
    }

    private static String buildInputSchema(Method method, ToolDescription description) {
        String baseSchema = ToolDefinitions.from(method).inputSchema();
        return ToolInputSchemaEnhancer.enhance(baseSchema, description.parameterDescriptions());
    }
}
