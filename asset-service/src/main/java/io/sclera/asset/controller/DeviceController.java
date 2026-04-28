/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.asset.controller;

import io.sclera.asset.dto.DeviceDTO;
import io.sclera.asset.dto.OnboardStatusPatchDTO;
import io.sclera.asset.dto.PageResponse;
import io.sclera.asset.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /** GET /api/devices/{id}?vdmsId= */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDTO> getById(
            @PathVariable String id,
            @RequestParam String vdmsId) {
        return ResponseEntity.ok(deviceService.getById(id, vdmsId));
    }

    /** GET /api/devices?vdmsId=&dockerId=&search=&virtualType=&status=&page=0&size=20 */
    @GetMapping
    public ResponseEntity<PageResponse<DeviceDTO>> filter(
            @RequestParam String  vdmsId,
            @RequestParam(required = false) String  dockerId,
            @RequestParam(required = false) String  search,
            @RequestParam(required = false) Integer virtualType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                deviceService.filter(vdmsId, dockerId, search, virtualType, status, page, size));
    }

    /** POST /api/devices?username= */
    @PostMapping
    public ResponseEntity<DeviceDTO> create(
            @Valid @RequestBody DeviceDTO dto,
            @RequestParam String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceService.create(dto, username));
    }

    /** PUT /api/devices/{id}?username= */
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDTO> update(
            @PathVariable String id,
            @Valid @RequestBody DeviceDTO dto,
            @RequestParam String username) {
        return ResponseEntity.ok(deviceService.update(id, dto, username));
    }

    /** PATCH /api/devices/{id}/onboard-status */
    @PatchMapping("/{id}/onboard-status")
    public ResponseEntity<Void> patchOnboardStatus(
            @PathVariable String id,
            @Valid @RequestBody OnboardStatusPatchDTO patch) {
        deviceService.patchOnboardStatus(id, patch);
        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/devices/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/devices/onboard-count?dockerId= */
    @GetMapping("/onboard-count")
    public ResponseEntity<Map<String, Long>> onboardCount(
            @RequestParam String dockerId) {
        return ResponseEntity.ok(deviceService.onboardCountByDocker(dockerId));
    }
}
