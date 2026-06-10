package io.mcpplus.autoconfigure.support;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SampleOrderService {

    @Operation(summary = "Create order")
    public String createOrder(String productId, int quantity) {
        return "order:" + productId + "x" + quantity;
    }

    public Mono<String> asyncStatus(String orderId) {
        return Mono.just("status:" + orderId);
    }
}
