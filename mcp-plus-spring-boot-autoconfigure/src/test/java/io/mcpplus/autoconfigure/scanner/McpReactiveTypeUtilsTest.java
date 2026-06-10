package io.mcpplus.autoconfigure.scanner;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class McpReactiveTypeUtilsTest {

    @Test
    void shouldDetectMonoReturnType() throws Exception {
        Method method = ReactiveService.class.getMethod("asyncHello", String.class);
        assertThat(McpReactiveTypeUtils.isReactiveReturnType(method)).isTrue();
    }

    @Test
    void shouldDetectNonReactiveReturnType() throws Exception {
        Method method = ReactiveService.class.getMethod("syncHello", String.class);
        assertThat(McpReactiveTypeUtils.isReactiveReturnType(method)).isFalse();
    }

    static class ReactiveService {
        public String syncHello(String name) {
            return name;
        }

        public Mono<String> asyncHello(String name) {
            return Mono.just(name);
        }
    }
}
