package io.sclera.devicespecification.model;

import lombok.*;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "device_specification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceSpecification {

    @Id
    private String id;   // serial number / mac-address

    @Column(name = "device_id", length = 255)
    private String deviceId;   // hanging FK — no DB constraint

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "account_type", length = 255)
    private String accountType;

    @Column(name = "user_uuid", length = 255)
    private String userUUID;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "model", length = 255)
    private String model;

    @Column(name = "os_type", length = 255)
    private String osType;

    @Column(name = "location_info", length = 1024)
    private String locationInfo;

    @Column(name = "os_info", length = 1024)
    private String osInfo;

    @Column(name = "cpu_info", length = 512)
    private String cpuInfo;

    @Lob
    @Column(name = "disk_drives", columnDefinition = "LONGTEXT")
    private String diskDrives;

    @Lob
    @Column(name = "physical_disks", columnDefinition = "LONGTEXT")
    private String physicalDisks;

    @Column(name = "bios", length = 512)
    private String bios;

    @Column(name = "ram_info", length = 512)
    private String ramInfo;

    @Column(name = "video_cards", length = 1024)
    private String videoCards;

    @Column(name = "sound_devices", length = 1024)
    private String soundDevices;

    @Column(name = "battery_info", length = 255)
    private String batteryInfo;

    @Lob
    @Column(name = "processes", columnDefinition = "LONGTEXT")
    private String processes;

    @Lob
    @Column(name = "system_updates", columnDefinition = "LONGTEXT")
    private String systemUpdates;

    @Lob
    @Column(name = "child_devices", columnDefinition = "LONGTEXT")
    private String childDevices;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
