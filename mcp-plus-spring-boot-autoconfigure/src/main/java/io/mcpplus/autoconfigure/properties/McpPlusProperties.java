package io.mcpplus.autoconfigure.properties;

import io.mcpplus.autoconfigure.scanner.DuplicateToolNameStrategy;
import io.mcpplus.autoconfigure.scanner.InheritanceDedupStrategy;
import io.mcpplus.autoconfigure.scanner.McpServerType;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "mcp.plus")
public class McpPlusProperties {

    /**
     * Whether MCP Plus auto-exposure is enabled.
     */
    private boolean enabled = true;

    /**
     * Base packages to scan. Empty means scan all beans in the application context.
     */
    private List<String> basePackages = new ArrayList<>();

    /**
     * Bean types to scan: CONTROLLER, SERVICE, or ALL.
     */
    private BeanScanType beanTypes = BeanScanType.ALL;

    /**
     * Tool name generation strategy.
     */
    private ToolNameStrategy toolNameStrategy = ToolNameStrategy.CLASS_METHOD;

    /**
     * Ordered description source priority.
     */
    private List<DescriptionSource> descriptionSources = List.of(
            DescriptionSource.MCP_EXPOSE,
            DescriptionSource.SWAGGER,
            DescriptionSource.JAVADOC);

    /**
     * Whether to parse Javadoc at runtime (requires therapi-runtime-javadoc on classpath).
     */
    private boolean javadocEnabled = false;

    /**
     * Exclude deprecated methods.
     */
    private boolean excludeDeprecated = true;

    /**
     * Method name patterns to include (regex). Empty means include all.
     */
    private List<String> includeMethodPatterns = new ArrayList<>();

    /**
     * MCP server type override. When unset, reads {@code spring.ai.mcp.server.type}.
     */
    private McpServerType serverType;

    /**
     * Whether the MCP server runs in stateless mode. When unset, reads {@code spring.ai.mcp.server.protocol}.
     */
    private Boolean stateless;

    /**
     * Fully-qualified class name patterns to exclude (regex).
     */
    private List<String> excludeClassPatterns = new ArrayList<>();

    /**
     * Strategy when duplicate tool names are detected.
     */
    private DuplicateToolNameStrategy duplicateToolNameStrategy = DuplicateToolNameStrategy.FAIL;

    /**
     * Log detailed filter decisions at DEBUG level.
     */
    private boolean debug = false;

    /**
     * Strategy for deduplicating methods in inheritance hierarchies.
     */
    private InheritanceDedupStrategy inheritanceDedupStrategy = InheritanceDedupStrategy.MOST_SPECIFIC;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(List<String> basePackages) {
        this.basePackages = basePackages;
    }

    public BeanScanType getBeanTypes() {
        return beanTypes;
    }

    public void setBeanTypes(BeanScanType beanTypes) {
        this.beanTypes = beanTypes;
    }

    public ToolNameStrategy getToolNameStrategy() {
        return toolNameStrategy;
    }

    public void setToolNameStrategy(ToolNameStrategy toolNameStrategy) {
        this.toolNameStrategy = toolNameStrategy;
    }

    public List<DescriptionSource> getDescriptionSources() {
        return descriptionSources;
    }

    public void setDescriptionSources(List<DescriptionSource> descriptionSources) {
        this.descriptionSources = descriptionSources;
    }

    public boolean isJavadocEnabled() {
        return javadocEnabled;
    }

    public void setJavadocEnabled(boolean javadocEnabled) {
        this.javadocEnabled = javadocEnabled;
    }

    public boolean isExcludeDeprecated() {
        return excludeDeprecated;
    }

    public void setExcludeDeprecated(boolean excludeDeprecated) {
        this.excludeDeprecated = excludeDeprecated;
    }

    public List<String> getIncludeMethodPatterns() {
        return includeMethodPatterns;
    }

    public void setIncludeMethodPatterns(List<String> includeMethodPatterns) {
        this.includeMethodPatterns = includeMethodPatterns;
    }

    /**
     * Method name patterns to exclude (regex).
     */
    private List<String> excludeMethodPatterns = new ArrayList<>();

    public List<String> getExcludeMethodPatterns() {
        return excludeMethodPatterns;
    }

    public void setExcludeMethodPatterns(List<String> excludeMethodPatterns) {
        this.excludeMethodPatterns = excludeMethodPatterns;
    }

    public McpServerType getServerType() {
        return serverType;
    }

    public void setServerType(McpServerType serverType) {
        this.serverType = serverType;
    }

    public Boolean getStateless() {
        return stateless;
    }

    public void setStateless(Boolean stateless) {
        this.stateless = stateless;
    }

    public List<String> getExcludeClassPatterns() {
        return excludeClassPatterns;
    }

    public void setExcludeClassPatterns(List<String> excludeClassPatterns) {
        this.excludeClassPatterns = excludeClassPatterns;
    }

    public DuplicateToolNameStrategy getDuplicateToolNameStrategy() {
        return duplicateToolNameStrategy;
    }

    public void setDuplicateToolNameStrategy(DuplicateToolNameStrategy duplicateToolNameStrategy) {
        this.duplicateToolNameStrategy = duplicateToolNameStrategy;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public InheritanceDedupStrategy getInheritanceDedupStrategy() {
        return inheritanceDedupStrategy;
    }

    public void setInheritanceDedupStrategy(InheritanceDedupStrategy inheritanceDedupStrategy) {
        this.inheritanceDedupStrategy = inheritanceDedupStrategy;
    }

    public enum BeanScanType {
        CONTROLLER,
        SERVICE,
        ALL
    }

    public enum ToolNameStrategy {
        METHOD_NAME,
        CLASS_METHOD,
        CUSTOM
    }

    public enum DescriptionSource {
        MCP_EXPOSE,
        SWAGGER,
        JAVADOC
    }
}
