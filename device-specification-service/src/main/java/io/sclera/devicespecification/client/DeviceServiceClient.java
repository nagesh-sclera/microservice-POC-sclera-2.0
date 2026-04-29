package io.sclera.devicespecification.client;

import io.sclera.devicespecification.dto.DeviceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class DeviceServiceClient {

    private final WebClient webClient;

    public DeviceServiceClient(WebClient.Builder builder,
                               @Value("${services.device.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    /** Find a device by its serial number. Returns null if not found. */
    public DeviceDTO findBySerialNumber(String serialNumber) {
        try {
            return webClient.get()
                    .uri("/api/devices/serial/{serial}", serialNumber)
                    .retrieve()
                    .bodyToMono(DeviceDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.error("DeviceServiceClient.findBySerialNumber failed for {}: {}", serialNumber, e.getMessage());
            return null;
        }
    }

    /** Patch mutable fields (IP, MAC, model, name, vendor) on an existing device. */
    public void updateDeviceInfo(String deviceId, String ipAddress, String macAddress,
                                 String model, String deviceName, String vendor) {
        Map<String, Object> body = new HashMap<>();
        if (ipAddress  != null) body.put("ip_address",       ipAddress);
        if (macAddress != null) body.put("mac_address",      macAddress);
        if (model      != null) body.put("user_data_model",  model);
        if (deviceName != null) body.put("user_data_name",   deviceName);
        if (vendor     != null) body.put("user_data_vendor", vendor);
        if (body.isEmpty()) return;

        try {
            webClient.patch()
                    .uri("/api/devices/{id}", deviceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("DeviceServiceClient.updateDeviceInfo failed for {}: {}", deviceId, e.getMessage());
        }
    }

    /** Set a device's connection status to online (status=1). */
    public void setDeviceOnline(String deviceId) {
        try {
            webClient.patch()
                    .uri("/api/devices/{id}/status/online", deviceId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("DeviceServiceClient.setDeviceOnline failed for {}: {}", deviceId, e.getMessage());
        }
    }

    /** Add a virtual child device. Returns the newly created device's ID. */
    public String addVirtualDevice(String name, String parentDeviceId, String vdmsId,
                                   String dockerName, String assetGroup) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_data_name",       name);
        body.put("subsystem_parent_id",  parentDeviceId);
        body.put("vdms_id",              vdmsId);
        body.put("docker_name",          dockerName);
        body.put("asset_group",          assetGroup);
        body.put("virtual_device_type",  2);

        try {
            DeviceDTO created = webClient.post()
                    .uri("/api/devices/virtual")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(DeviceDTO.class)
                    .block();
            return created != null ? created.getId() : null;
        } catch (Exception e) {
            log.error("DeviceServiceClient.addVirtualDevice failed for name={}: {}", name, e.getMessage());
            return null;
        }
    }

    /** Find a virtual child device by display name and parent device ID. */
    public Optional<DeviceDTO> findByDisplayNameAndParentId(String displayName, String parentId) {
        try {
            DeviceDTO result = webClient.get()
                    .uri(u -> u.path("/api/devices/search")
                               .queryParam("displayName", displayName)
                               .queryParam("parentId",    parentId)
                               .build())
                    .retrieve()
                    .bodyToMono(DeviceDTO.class)
                    .block();
            return Optional.ofNullable(result);
        } catch (WebClientResponseException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("DeviceServiceClient.findByDisplayNameAndParentId failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** Increment the subsystem count on a parent device. */
    public void incrementSubsystemCount(String parentDeviceId) {
        try {
            webClient.put()
                    .uri("/api/devices/{id}/subsystem-count/increment", parentDeviceId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("DeviceServiceClient.incrementSubsystemCount failed for {}: {}", parentDeviceId, e.getMessage());
        }
    }

    /** Mark virtual devices as onboarded. */
    public void onboardVirtualDevices(Set<String> deviceIds, String username) {
        Map<String, Object> body = new HashMap<>();
        body.put("deviceIds", deviceIds);
        body.put("username",  username);
        try {
            webClient.post()
                    .uri("/api/devices/virtual/onboard")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("DeviceServiceClient.onboardVirtualDevices failed: {}", e.getMessage());
        }
    }
}
