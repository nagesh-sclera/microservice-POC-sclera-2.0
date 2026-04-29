package io.sclera.devicelifecycle.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigInteger;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceLifecycleHistoryDTO {

    private String id;
    private String operationalStatus;
    private String usageStatus;
    private String assignedUserId;
    private Integer assignmentCount;
    private BigInteger createdTimestamp;
    private BigInteger assignedTimestamp;
    private String deviceId;
    private String description;
    private String assignedByUserId;

    public DeviceLifecycleHistoryDTO() {}

    public DeviceLifecycleHistoryDTO(String id, String operationalStatus, String usageStatus,
                                     String assignedUserId, Integer assignmentCount,
                                     BigInteger createdTimestamp, BigInteger assignedTimestamp,
                                     String deviceId, String description, String assignedByUserId) {
        this.id = id; this.operationalStatus = operationalStatus; this.usageStatus = usageStatus;
        this.assignedUserId = assignedUserId; this.assignmentCount = assignmentCount;
        this.createdTimestamp = createdTimestamp; this.assignedTimestamp = assignedTimestamp;
        this.deviceId = deviceId; this.description = description; this.assignedByUserId = assignedByUserId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(String operationalStatus) { this.operationalStatus = operationalStatus; }
    public String getUsageStatus() { return usageStatus; }
    public void setUsageStatus(String usageStatus) { this.usageStatus = usageStatus; }
    public String getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(String assignedUserId) { this.assignedUserId = assignedUserId; }
    public Integer getAssignmentCount() { return assignmentCount; }
    public void setAssignmentCount(Integer assignmentCount) { this.assignmentCount = assignmentCount; }
    public BigInteger getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(BigInteger createdTimestamp) { this.createdTimestamp = createdTimestamp; }
    public BigInteger getAssignedTimestamp() { return assignedTimestamp; }
    public void setAssignedTimestamp(BigInteger assignedTimestamp) { this.assignedTimestamp = assignedTimestamp; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAssignedByUserId() { return assignedByUserId; }
    public void setAssignedByUserId(String assignedByUserId) { this.assignedByUserId = assignedByUserId; }
}
