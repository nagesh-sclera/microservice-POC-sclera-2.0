package io.sclera.devicespecification.repository;

import io.sclera.devicespecification.model.DeviceNetworkSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface DeviceNetworkSpecificationRepository extends JpaRepository<DeviceNetworkSpecification, String> {

    DeviceNetworkSpecification findByDeviceId(String deviceId);

    void deleteByDeviceId(String deviceId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_network_specification SET device_id = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDeviceIdBySerialNumber(String serialNumber, String deviceId);
}
