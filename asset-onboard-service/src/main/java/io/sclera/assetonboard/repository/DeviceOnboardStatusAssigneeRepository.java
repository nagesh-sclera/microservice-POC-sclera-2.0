/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboard.repository;

import io.sclera.assetonboard.model.DeviceOnboardStatusAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DeviceOnboardStatusAssigneeRepository
        extends JpaRepository<DeviceOnboardStatusAssignee, String> {

    @Query("SELECT a FROM DeviceOnboardStatusAssignee a WHERE a.device_onboard_status_id = :statusId")
    List<DeviceOnboardStatusAssignee> findByDeviceOnboardStatusId(@Param("statusId") String statusId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT DISTINCT a.email FROM DeviceOnboardStatusAssignee a " +
        "JOIN DeviceOnboardStatus s ON s.id = a.device_onboard_status_id " +
        "WHERE s.device_id IN (" +
        "  SELECT d.device_id FROM DeviceOnboardStatus d WHERE d.device_id IN :deviceIds)")
    Set<String> findAssigneeEmailsByDeviceIds(@org.springframework.data.repository.query.Param("deviceIds")
                                              List<String> deviceIds);
}
