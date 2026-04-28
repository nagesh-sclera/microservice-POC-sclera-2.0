/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboard.controller;

import io.sclera.assetonboard.dto.DeviceOnboardStatusDTO;
import io.sclera.assetonboard.dto.UpsertOnboardRequestDTO;
import io.sclera.assetonboard.model.DeviceOnboardStatus;
import io.sclera.assetonboard.service.AssetOnboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/onboard")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class AssetOnboardController {

    private final AssetOnboardService assetOnboardService;

    /**
     * POST /api/onboard/assets/upsert
     * Body: { "devices": [{ "device_id", "onboard_status", "assignee_email" }] }
     */
    @PostMapping("/assets/upsert")
    public ResponseEntity<Void> upsert(
            @RequestParam String vdmsId,
            @RequestBody UpsertOnboardRequestDTO request) {
        assetOnboardService.upsertOnboardAssets(vdmsId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/onboard/devices/{deviceId}/data?vdmsId=&onboardStatus=
     */
    @PutMapping("/devices/{deviceId}/data")
    public ResponseEntity<Void> updateData(
            @PathVariable String deviceId,
            @RequestParam String  vdmsId,
            @RequestParam(required = false) Integer onboardStatus,
            @RequestBody DeviceOnboardStatusDTO dto) {
        assetOnboardService.updateOnboardData(vdmsId, deviceId, dto, onboardStatus);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/onboard/devices/{deviceId}/status?vdmsId=
     */
    @GetMapping("/devices/{deviceId}/status")
    public ResponseEntity<DeviceOnboardStatus> getStatus(
            @PathVariable String deviceId,
            @RequestParam String vdmsId) {
        return ResponseEntity.ok(assetOnboardService.getOnboardStatus(deviceId));
    }

    /**
     * GET /api/onboard/assignees?vdmsId=
     */
    @GetMapping("/assignees")
    public ResponseEntity<Set<String>> getAssignees(@RequestParam String vdmsId) {
        return ResponseEntity.ok(assetOnboardService.getAssignees(vdmsId));
    }
}
