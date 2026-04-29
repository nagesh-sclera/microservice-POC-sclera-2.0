package io.sclera.devicespecification.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
public class InventoryServiceClient {

    private final WebClient webClient;

    public InventoryServiceClient(WebClient.Builder builder,
                                  @Value("${services.inventory.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    /** Fire-and-forget: notify inventory about a newly discovered agent device. */
    public void notifyNewDevice(Map<String, Object> payload) {
        webClient.post()
                .uri("/api/inventory/agent-notify")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        r  -> log.info("Inventory notified successfully"),
                        ex -> log.error("InventoryServiceClient.notifyNewDevice failed: {}", ex.getMessage())
                );
    }
}
