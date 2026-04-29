package io.sclera.devicespecification.model;

import lombok.*;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "device_installed_apps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceInstalledApps {

    @Id
    private String id;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "publisher", length = 255)
    private String publisher;

    @Column(name = "version", length = 255)
    private String version;

    @Column(name = "device_id")
    private String deviceId;   // hanging FK — no DB constraint

    @Column(name = "device_specification_id")
    private String deviceSpecificationId;   // hanging FK — no DB constraint

    @Column(name = "managed_software_id")
    private String managedSoftwareId;   // hanging FK — no DB constraint

    @Column(name = "risk_status")
    private Integer riskStatus;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
