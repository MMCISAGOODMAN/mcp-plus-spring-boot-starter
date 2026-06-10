# MCP Plus Spring Boot Starter

在 [Spring AI MCP Server Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) 之上做增强：引入 Starter 后，`@RestController` / `@Service` 的 public 方法会自动注册为 MCP Tool。

## 依赖

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>3.4.5</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-bom</artifactId>
      <version>1.1.2</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.mmcisagoodman</groupId>
    <artifactId>mcp-plus-spring-boot-starter</artifactId>
    <version>0.1.0</version>
  </dependency>
</dependencies>
```

## 配置

```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        type: SYNC
        protocol: STREAMABLE

mcp:
  plus:
    enabled: true
    base-packages:
      - com.example
    bean-types: ALL
    tool-name-strategy: CLASS_METHOD
```

## 注解

- `@McpExpose` — 自定义工具名、描述、角色
- `@McpExclude` — 类或方法级别排除暴露

## 要求

Java 17+ · Spring Boot 3.4.x · Spring AI 1.1.x

## License

Apache 2.0
