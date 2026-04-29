package io.sclera.devicespecification.model;

import lombok.*;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "device_network_specification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceNetworkSpecification {

    @Id
    private String id;   // serial number / mac-address

    @Column(name = "device_id", length = 255)
    private String deviceId;   // hanging FK — no DB constraint

    @Lob
    @Column(name = "network_interfaces", columnDefinition = "LONGTEXT")
    private String networkInterfaces;

    @Lob
    @Column(name = "network_settings", columnDefinition = "LONGTEXT")
    private String networkSettings;

    @Lob
    @Column(name = "network_ports", columnDefinition = "LONGTEXT")
    private String networkPorts;

    @Lob
    @Column(name = "network_processes", columnDefinition = "LONGTEXT")
    private String networkProcesses;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
