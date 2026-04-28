/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.asset.repository;

import io.sclera.asset.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {

    List<Device> findByVdmsIdAndDockerId(String vdmsId, String dockerId);

    @Query(value =
        "SELECT * FROM device " +
        "WHERE vdms_id = :vdmsId " +
        "  AND (:dockerId IS NULL OR docker_id = :dockerId) " +
        "  AND (:search IS NULL OR display_name LIKE CONCAT('%',:search,'%') " +
        "                       OR ip_address LIKE CONCAT('%',:search,'%') " +
        "                       OR mac_address LIKE CONCAT('%',:search,'%')) " +
        "  AND (:virtualType IS NULL OR virtual_device_type = :virtualType) " +
        "  AND (:status IS NULL OR status = :status) " +
        "ORDER BY created_timestamp DESC " +
        "LIMIT :pageSize OFFSET :offset",
        nativeQuery = true)
    List<Device> filterDevices(
            @Param("vdmsId")      String  vdmsId,
            @Param("dockerId")    String  dockerId,
            @Param("search")      String  search,
            @Param("virtualType") Integer virtualType,
            @Param("status")      Integer status,
            @Param("pageSize")    int     pageSize,
            @Param("offset")      int     offset);

    @Modifying
    @Query("UPDATE Device d SET d.onboardStatus = :status WHERE d.id = :deviceId")
    int updateOnboardStatus(@Param("deviceId") String deviceId,
                            @Param("status")   Integer status);

    @Query("SELECT COUNT(d) FROM Device d WHERE d.dockerId = :dockerId AND d.onboardStatus = :status")
    long countByDockerIdAndOnboardStatus(@Param("dockerId") String dockerId,
                                         @Param("status")   Integer status);

    Optional<Device> findByIdAndVdmsId(String id, String vdmsId);
}
