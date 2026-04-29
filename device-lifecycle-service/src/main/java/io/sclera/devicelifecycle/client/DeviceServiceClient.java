package io.sclera.devicelifecycle.client;

import io.sclera.devicelifecycle.dto.DeviceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class DeviceServiceClient {

    private final WebClient webClient;

    public DeviceServiceClient(WebClient.Builder builder,
                               @Value("${services.device.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    /** Get full device details by device ID. Returns null if not found. */
    public DeviceDTO getDeviceById(String deviceId) {
        try {
            return webClient.get()
                    .uri("/api/devices/{id}", deviceId)
                    .retrieve()
                    .bodyToMono(DeviceDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.error("DeviceServiceClient.getDeviceById failed for {}: {}", deviceId, e.getMessage());
            return null;
        }
    }

    /** Update the operational status on the device record. */
    public void updateOperationalStatus(String deviceId, String operationalStatus) {
        Map<String, String> body = new HashMap<>();
        body.put("operational_status", operationalStatus);
        try {
            webClient.patch()
                    .uri("/api/devices/{id}/operational-status", deviceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("DeviceServiceClient.updateOperationalStatus failed for {}: {}", deviceId, e.getMessage());
        }
    }

    /** Clear (or update) the assigned user email on the device record. */
    public void updateAssignedUserEmail(String deviceId, String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("assigned_user_email", email);
        try {
            webClient.patch()
                    .uri("/api/devices/{id}/assigned-user-email", deviceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("DeviceServiceClient.updateAssignedUserEmail failed for {}: {}", deviceId, e.getMessage());
        }
    }

    /** Archive devices for an inventory device (retire flow). */
    public void archiveDevices(String username, String vdmsId, Set<String> deviceIds) {
        Map<String, Object> body = new HashMap<>();
        body.put("username",  username);
        body.put("vdms_id",   vdmsId);
        body.put("deviceIds", deviceIds);
        body.put("flag",      1);
        try {
            webClient.post()
                    .uri("/api/devices/archive")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("DeviceServiceClient.archiveDevices failed: {}", e.getMessage());
        }
    }
}
