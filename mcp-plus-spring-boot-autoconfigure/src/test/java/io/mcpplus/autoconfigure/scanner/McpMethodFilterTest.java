package io.mcpplus.autoconfigure.scanner;

import io.mcpplus.autoconfigure.annotation.McpExclude;
import io.mcpplus.autoconfigure.annotation.McpExpose;
import io.mcpplus.autoconfigure.properties.McpPlusProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class McpMethodFilterTest {

    private final McpPlusProperties properties = new McpPlusProperties();
    private MockEnvironment environment;
    private McpMethodFilter filter;

    @BeforeEach
    void setUp() {
        environment = new MockEnvironment();
        environment.setProperty("spring.ai.mcp.server.type", "SYNC");
        filter = newFilter();
    }

    @Test
    void shouldAcceptServiceAndControllerBeans() {
        assertThat(filter.isEligibleBean(UserServiceBean.class)).isTrue();
        assertThat(filter.isEligibleBean(UserControllerBean.class)).isTrue();
    }

    @Test
    void shouldRespectBeanTypeFilter() {
        properties.setBeanTypes(McpPlusProperties.BeanScanType.SERVICE);
        filter = newFilter();
        assertThat(filter.isEligibleBean(UserServiceBean.class)).isTrue();
        assertThat(filter.isEligibleBean(UserControllerBean.class)).isFalse();
    }

    @Test
    void shouldRespectMcpExposeEnabledFlag() throws Exception {
        Method disabled = DisabledService.class.getMethod("disabledMethod");
        assertThat(filter.isEligibleMethod(disabled, DisabledService.class)).isFalse();
    }

    @Test
    void shouldExcludeClassAnnotatedWithMcpExclude() {
        assertThat(filter.isEligibleBean(ExcludedService.class)).isFalse();
    }

    @Test
    void shouldExcludeMethodAnnotatedWithMcpExclude() throws Exception {
        Method excluded = PartiallyExcludedService.class.getMethod("excludedMethod");
        Method included = PartiallyExcludedService.class.getMethod("includedMethod");
        assertThat(filter.isEligibleMethod(excluded, PartiallyExcludedService.class)).isFalse();
        assertThat(filter.isEligibleMethod(included, PartiallyExcludedService.class)).isTrue();
    }

    @Test
    void shouldExcludeReactiveMethodsOnSyncServer() throws Exception {
        Method sync = ReactiveAwareService.class.getMethod("syncMethod");
        Method async = ReactiveAwareService.class.getMethod("asyncMethod", String.class);
        assertThat(filter.isEligibleMethod(sync, ReactiveAwareService.class)).isTrue();
        assertThat(filter.isEligibleMethod(async, ReactiveAwareService.class)).isFalse();
    }

    @Test
    void shouldIncludeReactiveMethodsOnAsyncServer() throws Exception {
        environment.setProperty("spring.ai.mcp.server.type", "ASYNC");
        filter = newFilter();
        Method sync = ReactiveAwareService.class.getMethod("syncMethod");
        Method async = ReactiveAwareService.class.getMethod("asyncMethod", String.class);
        assertThat(filter.isEligibleMethod(sync, ReactiveAwareService.class)).isFalse();
        assertThat(filter.isEligibleMethod(async, ReactiveAwareService.class)).isTrue();
    }

    @Test
    void shouldExcludeClassByPattern() {
        properties.setExcludeClassPatterns(List.of(".*Internal.*"));
        filter = newFilter();
        assertThat(filter.isEligibleBean(InternalService.class)).isFalse();
        assertThat(filter.isEligibleBean(UserServiceBean.class)).isTrue();
    }

    private McpMethodFilter newFilter() {
        return new McpMethodFilter(properties,
                new McpServerTypeResolver(properties, environment),
                new McpServerModeResolver(properties, environment));
    }

    @Service
    static class UserServiceBean {
        public String hello() {
            return "hello";
        }
    }

    @RestController
    static class UserControllerBean {
        public String ping() {
            return "pong";
        }
    }

    @Service
    @McpExpose(enabled = false)
    static class DisabledService {
        public String disabledMethod() {
            return "disabled";
        }
    }

    @Service
    @McpExclude
    static class ExcludedService {
        public String hidden() {
            return "hidden";
        }
    }

    @Service
    static class PartiallyExcludedService {
        @McpExclude
        public String excludedMethod() {
            return "excluded";
        }

        public String includedMethod() {
            return "included";
        }
    }

    @Service
    static class InternalService {
        public String internal() {
            return "internal";
        }
    }

    @Service
    static class ReactiveAwareService {
        public String syncMethod() {
            return "sync";
        }

        public Mono<String> asyncMethod(String input) {
            return Mono.just(input);
        }
    }
}
