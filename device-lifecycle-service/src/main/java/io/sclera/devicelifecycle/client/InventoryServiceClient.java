package io.sclera.devicelifecycle.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InventoryServiceClient {

    private final WebClient webClient;

    public InventoryServiceClient(WebClient.Builder builder,
                                  @Value("${services.inventory.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    /** Retire an inventory device (called during device lifecycle retire flow). */
    public void retireInventoryDevice(String vdmsId, String deviceId, String username,
                                      String description, String trackingId) {
        Map<String, Object> body = new HashMap<>();
        body.put("username",    username);
        body.put("description", description);
        body.put("trackingId",  trackingId);
        try {
            webClient.post()
                    .uri("/api/inventory/{vdmsId}/devices/{deviceId}/retire", vdmsId, deviceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Inventory retire call succeeded for deviceId: {}", deviceId);
        } catch (Exception e) {
            log.error("InventoryServiceClient.retireInventoryDevice failed for {}: {}", deviceId, e.getMessage());
        }
    }
}
