package io.sclera.devicespecification.repository;

import io.sclera.devicespecification.model.DeviceSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Repository
public interface DeviceSpecificationRepository extends JpaRepository<DeviceSpecification, String> {

    DeviceSpecification findByDeviceId(String deviceId);

    void deleteByDeviceId(String deviceId);

    @Query(value = "SELECT DISTINCT email FROM device_specification WHERE email IS NOT NULL", nativeQuery = true)
    List<String> findDistinctEmail();

    @Query(value = "SELECT DISTINCT os_type FROM device_specification WHERE os_type IS NOT NULL", nativeQuery = true)
    List<String> findDistinctOsType();

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_specification SET device_id = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceIdBySerialNumber(String serialNumber, String deviceId);

    @Query(value = "SELECT child_devices FROM device_specification WHERE device_id = ?1", nativeQuery = true)
    String getChildDeviceByDeviceId(String deviceId);

    List<DeviceSpecification> findByIdIn(Set<String> ids);
}
