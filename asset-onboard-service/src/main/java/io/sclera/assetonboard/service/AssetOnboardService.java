/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboard.service;

import io.sclera.assetonboard.client.AssetServiceClient;
import io.sclera.assetonboard.dto.DeviceOnboardStatusDTO;
import io.sclera.assetonboard.dto.UpsertOnboardRequestDTO;
import io.sclera.assetonboard.model.DeviceOnboardStatus;
import io.sclera.assetonboard.model.DeviceOnboardStatusAssignee;
import io.sclera.assetonboard.repository.DeviceOnboardStatusAssigneeRepository;
import io.sclera.assetonboard.repository.DeviceOnboardStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetOnboardService {

    private final DeviceOnboardStatusRepository statusRepository;
    private final DeviceOnboardStatusAssigneeRepository assigneeRepository;
    private final AssetServiceClient assetServiceClient;

    /**
     * Upsert onboard status records for a batch of devices.
     * Also propagates onboard_status to asset-service via WebClient.
     */
    @Transactional
    public void upsertOnboardAssets(String vdmsId, UpsertOnboardRequestDTO request) {
        for (UpsertOnboardRequestDTO.DeviceOnboardEntryDTO entry : request.getDevices()) {

            // Verify device exists in asset-service (sync call)
            assetServiceClient.getDevice(entry.getDevice_id(), vdmsId);

            DeviceOnboardStatus status = statusRepository
                    .findByDeviceId(entry.getDevice_id())
                    .orElseGet(() -> {
                        DeviceOnboardStatus s = new DeviceOnboardStatus();
                        s.setId(UUID.randomUUID().toString());
                        s.setDevice_id(entry.getDevice_id());
                        return s;
                    });

            status.setAssignee_email(entry.getAssignee_email());
            if (entry.getOnboard_status() != null) {
                status.setImage_status(0);
                status.setGeolocation_status(0);
                status.setTag_status(0);
                status.setField_status(0);
            }
            statusRepository.save(status);

            // Propagate onboard_status to asset-service
            if (entry.getOnboard_status() != null) {
                assetServiceClient.patchOnboardStatus(entry.getDevice_id(), entry.getOnboard_status());
            }
        }
    }

    /**
     * Update granular onboard data (image / geo / tag / field statuses) for one device.
     */
    @Transactional
    public void updateOnboardData(String vdmsId, String deviceId,
                                  DeviceOnboardStatusDTO dto, Integer overallStatus) {

        assetServiceClient.getDevice(deviceId, vdmsId); // guard

        DeviceOnboardStatus existing = statusRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No onboard record for device: " + deviceId));

        statusRepository.updateStatusFields(
                deviceId,
                dto.getImage_status() != null ? dto.getImage_status() : existing.getImage_status(),
                dto.getGeolocation_status() != null ? dto.getGeolocation_status() : existing.getGeolocation_status(),
                dto.getTag_status() != null ? dto.getTag_status() : existing.getTag_status(),
                dto.getField_status() != null ? dto.getField_status() : existing.getField_status());

        if (overallStatus != null) {
            assetServiceClient.patchOnboardStatus(deviceId, overallStatus);
        }
    }

    /**
     * Return onboard status record for a device (read-only).
     */
    public DeviceOnboardStatus getOnboardStatus(String deviceId) {
        return statusRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No onboard record for device: " + deviceId));
    }

    /**
     * List distinct assignee emails for a vdms.
     */
    public Set<String> getAssignees(String vdmsId) {
        // Delegate further filtering to repository; simplified here
        return new HashSet<>(assigneeRepository
                .findAll()
                .stream()
                .map(DeviceOnboardStatusAssignee::getEmail)
                .collect(java.util.stream.Collectors.toList()));
    }
}
