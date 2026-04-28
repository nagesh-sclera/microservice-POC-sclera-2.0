/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboarding.controller;

import io.sclera.assetonboarding.dto.AssetDTO;
import io.sclera.assetonboarding.dto.MappingDTO;
import io.sclera.assetonboarding.model.AssetDeviceMapping;
import io.sclera.assetonboarding.model.AssetField;
import io.sclera.assetonboarding.service.AssetOnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class AssetOnboardingController {

    private final AssetOnboardingService service;

    /** GET /api/assets?vdmsId=&importType=&search=&page=0&size=20 */
    @GetMapping
    public ResponseEntity<List<AssetDTO>> list(
            @RequestParam String vdmsId,
            @RequestParam(required = false) String importType,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getPaginated(vdmsId, importType, search, page, size));
    }

    /** GET /api/assets/unmapped?vdmsId=&page=0&size=20 */
    @GetMapping("/unmapped")
    public ResponseEntity<List<AssetDTO>> unmapped(
            @RequestParam String vdmsId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getUnmapped(vdmsId, page, size));
    }

    /** POST /api/assets (upsert) */
    @PostMapping
    public ResponseEntity<AssetDTO> upsert(@RequestBody AssetDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(service.upsert(dto));
    }

    /** DELETE /api/assets/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** POST /api/assets/mappings?vdmsId= */
    @PostMapping("/mappings")
    public ResponseEntity<AssetDeviceMapping> createMapping(
            @Valid @RequestBody MappingDTO dto,
            @RequestParam String vdmsId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMapping(dto, vdmsId));
    }

    /** DELETE /api/assets/mappings/{mappingId}?vdmsId= */
    @DeleteMapping("/mappings/{mappingId}")
    public ResponseEntity<Void> deleteMapping(
            @PathVariable String mappingId,
            @RequestParam String vdmsId) {
        service.deleteMapping(mappingId, vdmsId);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/assets/mappings?deviceId= */
    @GetMapping("/mappings")
    public ResponseEntity<List<AssetDeviceMapping>> getMappings(@RequestParam String deviceId) {
        return ResponseEntity.ok(service.getMappingsByDevice(deviceId));
    }

    /** GET /api/assets/fields */
    @GetMapping("/fields")
    public ResponseEntity<List<AssetField>> getFields() {
        return ResponseEntity.ok(service.getAllFields());
    }
}
