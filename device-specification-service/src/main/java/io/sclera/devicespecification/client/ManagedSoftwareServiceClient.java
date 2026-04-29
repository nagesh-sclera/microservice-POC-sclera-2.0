package io.sclera.devicespecification.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ManagedSoftwareServiceClient {

    private final WebClient webClient;

    public ManagedSoftwareServiceClient(WebClient.Builder builder,
                                        @Value("${services.managed-software.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    /**
     * Upserts a managed software entry by name + publisher.
     * Returns the managed software ID, or null on failure.
     */
    public String insertManagedSoftware(String name, String publisher) {
        Map<String, String> body = new HashMap<>();
        body.put("name",      name);
        body.put("publisher", publisher);
        try {
            return webClient.post()
                    .uri("/api/managed-software")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("ManagedSoftwareServiceClient.insertManagedSoftware failed for {}: {}", name, e.getMessage());
            return null;
        }
    }
}
