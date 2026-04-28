/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboard.client;

import io.sclera.assetonboard.dto.DeviceResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * WebClient-based client for asset-service.
 * All inter-service calls go through this class.
 */
@Slf4j
@Component
public class AssetServiceClient {

    private final WebClient webClient;

    public AssetServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.asset-service.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /** Fetch device details from asset-service. Throws NoSuchElementException if 404. */
    public DeviceResponseDTO getDevice(String deviceId, String vdmsId) {
        return webClient.get()
                .uri(u -> u.path("/api/devices/{id}").queryParam("vdmsId", vdmsId).build(deviceId))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        r -> Mono.error(new NoSuchElementException("Device not found: " + deviceId)))
                .bodyToMono(DeviceResponseDTO.class)
                .block();
    }

    /** PATCH onboard_status on the device record in asset-service. */
    public void patchOnboardStatus(String deviceId, Integer onboardStatus) {
        webClient.patch()
                .uri("/api/devices/{id}/onboard-status", deviceId)
                .bodyValue(Map.of("onboard_status", onboardStatus))
                .retrieve()
                .onStatus(HttpStatus::isError,
                        r -> r.bodyToMono(String.class)
                               .flatMap(body -> Mono.error(new RuntimeException(
                                       "Failed to patch onboard status: " + body))))
                .bodyToMono(Void.class)
                .block();
    }
}
