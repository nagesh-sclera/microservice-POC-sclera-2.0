package io.sclera.devicelifecycle.model;

import io.sclera.devicelifecycle.dto.DeviceLifecycleHistoryDTO;
import lombok.*;
import jakarta.persistence.*;
import java.math.BigInteger;

@SqlResultSetMapping(
        name = "deviceLifeCycleHistoryMapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceLifecycleHistoryDTO.class,
                        columns = {
                                @ColumnResult(name = "id",                  type = String.class),
                                @ColumnResult(name = "operational_status",  type = String.class),
                                @ColumnResult(name = "usage_status",        type = String.class),
                                @ColumnResult(name = "assigned_user_id",    type = String.class),
                                @ColumnResult(name = "assignment_count",    type = Integer.class),
                                @ColumnResult(name = "created_timestamp",   type = BigInteger.class),
                                @ColumnResult(name = "assigned_timestamp",  type = BigInteger.class),
                                @ColumnResult(name = "device_id",           type = String.class),
                                @ColumnResult(name = "description",         type = String.class),
                                @ColumnResult(name = "assigned_by_user_id", type = String.class)
                        })
        })
@NamedNativeQuery(
        name = "DeviceLifecycleHistory.getDeviceLifeCycleHistory",
        query = "SELECT dlc.id, dlc.operational_status, dlc.usage_status, dlc.assigned_user_id, " +
                "dlc.assignment_count, dlc.created_timestamp, dlc.assigned_timestamp, dlc.device_id, " +
                "dlc.description, dlc.assigned_by_user_id " +
                "FROM device_lifecycle_history dlc " +
                "WHERE dlc.device_id = ?1 " +
                "ORDER BY dlc.created_timestamp DESC, dlc.assignment_count DESC " +
                "LIMIT ?2 OFFSET ?3",
        resultSetMapping = "deviceLifeCycleHistoryMapping"
)
@Entity
@Table(name = "device_lifecycle_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceLifecycleHistory {

    @Id
    private String id;

    @Column(name = "device_id", length = 255)
    private String deviceId;   // hanging FK — no DB constraint

    @Column(name = "operational_status", length = 32)
    private String operationalStatus;

    @Column(name = "usage_status", length = 32)
    private String usageStatus;

    @Column(name = "assigned_user_id", length = 128)
    private String assignedUserId;

    @Column(name = "assignment_count")
    private Integer assignmentCount;

    @Column(name = "created_timestamp")
    private BigInteger createdTimestamp;

    @Column(name = "assigned_timestamp")
    private BigInteger assignedTimestamp;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "assigned_by_user_id", length = 255)
    private String assignedByUserId;
}
