/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboardingai.client;

import io.sclera.assetonboardingai.dto.AssetDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AssetOnboardingServiceClient {

    private final WebClient webClient;

    public AssetOnboardingServiceClient(
            WebClient.Builder builder,
            @Value("${services.asset-onboarding-service.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public List<AssetDTO> getUnmappedAssets(String vdmsId, int page, int size) {
        return webClient.get()
                .uri(u -> u.path("/api/assets/unmapped")
                        .queryParam("vdmsId", vdmsId)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AssetDTO>>() {})
                .block();
    }

    /** Persist an AI-suggested mapping (score above threshold). */
    public void createMapping(String deviceId, String assetId, Integer score, String vdmsId) {
        Map<String, Object> body = Map.of(
                "device_id",  deviceId,
                "asset_id",   assetId,
                "matchScore", score);
        webClient.post()
                .uri(u -> u.path("/api/assets/mappings").queryParam("vdmsId", vdmsId).build())
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatus::isError,
                        r -> r.bodyToMono(String.class)
                               .flatMap(b -> Mono.error(new RuntimeException("Mapping failed: " + b))))
                .bodyToMono(Void.class)
                .block();
    }
}
