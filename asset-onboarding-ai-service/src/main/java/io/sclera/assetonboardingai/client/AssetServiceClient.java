/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboardingai.client;

import io.sclera.assetonboardingai.dto.DeviceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
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

    public DeviceDTO getDevice(String deviceId, String vdmsId) {
        return webClient.get()
                .uri(u -> u.path("/api/devices/{id}").queryParam("vdmsId", vdmsId).build(deviceId))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        r -> Mono.error(new NoSuchElementException("Device not found: " + deviceId)))
                .bodyToMono(DeviceDTO.class)
                .block();
    }

    public List<DeviceDTO> getDevicesWithAiEnabled(String vdmsId, int page, int size) {
        return webClient.get()
                .uri(u -> u.path("/api/devices")
                        .queryParam("vdmsId", vdmsId)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<java.util.Map<String, Object>>() {})
                .map(resp -> {
                    // extract content list from PageResponse
                    Object content = resp.get("content");
                    if (content instanceof List) return (List<DeviceDTO>) content;
                    return List.<DeviceDTO>of();
                })
                .block();
    }
}
