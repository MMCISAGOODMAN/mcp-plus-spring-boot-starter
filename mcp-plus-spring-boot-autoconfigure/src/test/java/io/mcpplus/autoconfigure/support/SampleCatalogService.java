package io.mcpplus.autoconfigure.support;

import io.mcpplus.autoconfigure.annotation.McpExclude;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SampleCatalogService {

    @Operation(summary = "Find product by id")
    public String findProduct(@Parameter(description = "Product id") String id) {
        return "product:" + id;
    }

    @McpExclude
    public String hiddenProduct(String id) {
        return "hidden:" + id;
    }

    public Mono<String> asyncLookup(String id) {
        return Mono.just("async:" + id);
    }
}
