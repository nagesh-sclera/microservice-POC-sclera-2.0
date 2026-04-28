/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboarding.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Component
public class AssetServiceClient {

    private final WebClient webClient;

    public AssetServiceClient(
            WebClient.Builder builder,
            @Value("${services.asset-service.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    /** Verify a device exists in asset-service; throws if not found. */
    public void verifyDevice(String deviceId, String vdmsId) {
        webClient.get()
                .uri(u -> u.path("/api/devices/{id}").queryParam("vdmsId", vdmsId).build(deviceId))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        r -> Mono.error(new NoSuchElementException("Device not found: " + deviceId)))
                .bodyToMono(Map.class)
                .block();
    }

    /** Update asset_match_status on the device after a mapping is created/deleted. */
    public void patchAssetMatchStatus(String deviceId, Integer matchStatus, String vdmsId, String username) {
        webClient.patch()
                .uri("/api/devices/{id}/asset-match-status", deviceId)
                .bodyValue(Map.of("asset_match_status", matchStatus))
                .retrieve()
                .onStatus(HttpStatus::isError,
                        r -> r.bodyToMono(String.class)
                               .flatMap(b -> Mono.error(new RuntimeException("Patch failed: " + b))))
                .bodyToMono(Void.class)
                .block();
    }
}
