/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboard.repository;

import io.sclera.assetonboard.model.DeviceOnboardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceOnboardStatusRepository extends JpaRepository<DeviceOnboardStatus, String> {

    @Query("SELECT d FROM DeviceOnboardStatus d WHERE d.device_id = :deviceId")
    Optional<DeviceOnboardStatus> findByDeviceId(@Param("deviceId") String deviceId);

    @Modifying
    @Query("UPDATE DeviceOnboardStatus d " +
           "SET d.image_status = :imageStatus, " +
           "    d.geolocation_status = :geoStatus, " +
           "    d.tag_status = :tagStatus, " +
           "    d.field_status = :fieldStatus " +
           "WHERE d.device_id = :deviceId")
    int updateStatusFields(@Param("deviceId")    String  deviceId,
                           @Param("imageStatus") Integer imageStatus,
                           @Param("geoStatus")   Integer geoStatus,
                           @Param("tagStatus")   Integer tagStatus,
                           @Param("fieldStatus") Integer fieldStatus);
}
