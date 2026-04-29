package io.sclera.devicespecification.repository;

import io.sclera.devicespecification.model.DeviceInstalledApps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface DeviceInstalledAppsRepository extends JpaRepository<DeviceInstalledApps, String> {

    void deleteByDeviceId(String deviceId);

    List<DeviceInstalledApps> findByDeviceId(String deviceId);

    boolean existsByDeviceIdAndName(String deviceId, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_installed_apps SET device_id = ?2 WHERE device_specification_id = ?1", nativeQuery = true)
    void updateDeviceIdBySerialNumber(String serialNumber, String deviceId);
}
