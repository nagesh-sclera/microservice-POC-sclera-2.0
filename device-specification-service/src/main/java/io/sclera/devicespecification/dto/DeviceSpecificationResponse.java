package io.sclera.devicespecification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceSpecificationResponse {
    private String id;
    private String deviceId;
    private String username;
    private String email;
    private String accountType;
    private String userUUID;
    private String deviceName;
    private String model;
    private String osType;
    private String locationInfo;
    private String osInfo;
    private String cpuInfo;
    private String diskDrives;
    private String physicalDisks;
    private String bios;
    private String ramInfo;
    private String videoCards;
    private String soundDevices;
    private String batteryInfo;
    private String processes;
    private String systemUpdates;
    private String childDevices;
    private String networkInterfaces;
    private String networkSettings;
    private String networkPorts;
    private String networkProcesses;
    private Long createdAt;
    private Long updatedAt;
}
