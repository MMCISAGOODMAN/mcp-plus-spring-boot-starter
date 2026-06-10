package io.mcpplus.autoconfigure.tool;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class ReactiveToolCallResultConverterTest {

    private final ReactiveToolCallResultConverter converter = new ReactiveToolCallResultConverter();

    @Test
    void shouldUnwrapMonoResult() {
        String json = converter.convert(Mono.just("hello"), Mono.class);
        assertThat(json).contains("hello");
    }
}
