package io.sclera.devicelifecycle.repository;

import io.sclera.devicelifecycle.dto.DeviceLifecycleHistoryDTO;
import io.sclera.devicelifecycle.model.DeviceLifecycleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.math.BigInteger;
import java.util.Set;

@Repository
public interface DeviceLifecycleHistoryRepository extends JpaRepository<DeviceLifecycleHistory, String> {

    void deleteByDeviceId(String deviceId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_lifecycle_history(" +
            "id, operational_status, usage_status, assigned_user_id, assignment_count, " +
            "created_timestamp, assigned_timestamp, device_id, description, assigned_by_user_id) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)", nativeQuery = true)
    void insertHistory(String id, String operationalStatus, String usageStatus,
                       String assignedUserId, Integer assignmentCount,
                       BigInteger createdTimestamp, BigInteger assignedTimestamp,
                       String deviceId, String description, String assignedByUserId);

    @Query(name = "DeviceLifecycleHistory.getDeviceLifeCycleHistory", nativeQuery = true)
    Set<DeviceLifecycleHistoryDTO> getDeviceLifeCycleHistory(String deviceId, Integer pageSize, Integer offset);

    @Query(value = "SELECT assignment_count FROM device_lifecycle_history " +
            "WHERE device_id = ?1 ORDER BY created_timestamp DESC LIMIT 1", nativeQuery = true)
    Integer getLatestAssignmentCount(String deviceId);

    @Query(value = "SELECT operational_status FROM device_lifecycle_history " +
            "WHERE device_id = ?1 ORDER BY created_timestamp DESC LIMIT 1", nativeQuery = true)
    String getLatestOperationalStatus(String deviceId);
}
