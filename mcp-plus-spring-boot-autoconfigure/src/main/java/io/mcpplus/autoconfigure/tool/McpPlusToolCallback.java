package io.mcpplus.autoconfigure.tool;

import io.mcpplus.autoconfigure.security.McpRoleChecker;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

public class McpPlusToolCallback implements ToolCallback {

    private final ToolCallback delegate;
    private final McpRoleChecker roleChecker;
    private final String[] requiredRoles;

    public McpPlusToolCallback(ToolCallback delegate, McpRoleChecker roleChecker, String[] requiredRoles) {
        this.delegate = delegate;
        this.roleChecker = roleChecker;
        this.requiredRoles = requiredRoles;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        roleChecker.checkAccess(requiredRoles);
        return delegate.call(toolInput);
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        roleChecker.checkAccess(requiredRoles);
        return delegate.call(toolInput, toolContext);
    }
}
