package io.sclera.devicespecification.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder builder,
                             @Value("${services.user.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    /** Returns the master/admin user email, or null on failure. */
    public String getMasterUserEmail() {
        try {
            return webClient.get()
                    .uri("/api/users/master-email")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("UserServiceClient.getMasterUserEmail failed: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("UserServiceClient.getMasterUserEmail failed: {}", e.getMessage());
            return null;
        }
    }
}
