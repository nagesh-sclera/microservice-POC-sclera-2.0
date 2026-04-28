/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboarding.service;

import io.sclera.assetonboarding.client.AssetServiceClient;
import io.sclera.assetonboarding.dto.AssetDTO;
import io.sclera.assetonboarding.dto.MappingDTO;
import io.sclera.assetonboarding.model.Asset;
import io.sclera.assetonboarding.model.AssetDeviceMapping;
import io.sclera.assetonboarding.model.AssetField;
import io.sclera.assetonboarding.repository.AssetDeviceMappingRepository;
import io.sclera.assetonboarding.repository.AssetFieldRepository;
import io.sclera.assetonboarding.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetOnboardingService {

    private final AssetRepository            assetRepository;
    private final AssetDeviceMappingRepository mappingRepository;
    private final AssetFieldRepository       fieldRepository;
    private final AssetServiceClient         assetServiceClient;

    public List<AssetDTO> getPaginated(String vdmsId, String importType,
                                       String search, int page, int size) {
        return assetRepository.getPaginated(vdmsId, importType, search, size, page * size)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<AssetDTO> getUnmapped(String vdmsId, int page, int size) {
        return assetRepository.getUnmapped(vdmsId, size, page * size)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public AssetDTO upsert(AssetDTO dto) {
        Asset a = assetRepository.findById(dto.getId())
                .orElseGet(() -> { Asset n = new Asset(); n.setId(
                        dto.getId() != null ? dto.getId() : UUID.randomUUID().toString()); return n; });
        applyDTO(dto, a);
        return toDTO(assetRepository.save(a));
    }

    @Transactional
    public void delete(String id) {
        if (!assetRepository.existsById(id))
            throw new NoSuchElementException("Asset not found: " + id);
        mappingRepository.findByAssetId(id)
                .forEach(m -> mappingRepository.deleteById(m.getId()));
        assetRepository.deleteById(id);
    }

    /**
     * Create asset-device mapping.
     * Verifies device exists in asset-service before persisting.
     */
    @Transactional
    public AssetDeviceMapping createMapping(MappingDTO dto, String vdmsId) {
        assetServiceClient.verifyDevice(dto.getDevice_id(), vdmsId);  // cross-service guard

        mappingRepository.findByDeviceIdAndAssetId(dto.getDevice_id(), dto.getAsset_id())
                .ifPresent(m -> { throw new IllegalStateException("Mapping already exists"); });

        AssetDeviceMapping m = new AssetDeviceMapping(
                UUID.randomUUID().toString(),
                dto.getDevice_id(),
                dto.getAsset_id(),
                dto.getMatchScore());
        AssetDeviceMapping saved = mappingRepository.save(m);

        assetRepository.setMatched(dto.getAsset_id(), true);
        assetServiceClient.patchAssetMatchStatus(dto.getDevice_id(), 1, vdmsId, null);
        return saved;
    }

    /**
     * Delete asset-device mapping.
     * Cascades matched-status reset on both sides.
     */
    @Transactional
    public void deleteMapping(String mappingId, String vdmsId) {
        AssetDeviceMapping m = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new NoSuchElementException("Mapping not found: " + mappingId));
        mappingRepository.deleteById(mappingId);
        assetRepository.setMatched(m.getAssetId(), false);
        assetServiceClient.patchAssetMatchStatus(m.getDeviceId(), 0, vdmsId, null);
    }

    public List<AssetDeviceMapping> getMappingsByDevice(String deviceId) {
        return mappingRepository.findByDeviceId(deviceId);
    }

    public List<AssetField> getAllFields() {
        return fieldRepository.getAllActive();
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private AssetDTO toDTO(Asset a) {
        AssetDTO d = new AssetDTO();
        d.setId(a.getId()); d.setDisplay_name(a.getDisplay_name());
        d.setDescription(a.getDescription()); d.setMac_address(a.getMac_address());
        d.setModel(a.getModel()); d.setVendor(a.getVendor()); d.setType(a.getType());
        d.setIp_address(a.getIp_address()); d.setNetwork_layer(a.getNetwork_layer());
        d.setSerial_number(a.getSerial_number()); d.setImport_type(a.getImport_type());
        d.setWarranty(a.getWarranty()); d.setIsMatched(a.getIsMatched());
        d.setSubsystem_parent_id(a.getSubsystem_parent_id());
        d.setSubsystem_count(a.getSubsystem_count());
        d.setOriginalKeys(a.getOriginalKeys()); d.setCustomFields(a.getCustomFields());
        d.setMatchedProductIds(a.getMatchedProductIds()); d.setVdms_id(a.getVdms_id());
        return d;
    }

    private void applyDTO(AssetDTO d, Asset a) {
        a.setDisplay_name(d.getDisplay_name()); a.setDescription(d.getDescription());
        a.setMac_address(d.getMac_address()); a.setModel(d.getModel());
        a.setVendor(d.getVendor()); a.setType(d.getType()); a.setIp_address(d.getIp_address());
        a.setNetwork_layer(d.getNetwork_layer()); a.setSerial_number(d.getSerial_number());
        a.setImport_type(d.getImport_type()); a.setWarranty(d.getWarranty());
        a.setIsMatched(d.getIsMatched()); a.setSubsystem_parent_id(d.getSubsystem_parent_id());
        a.setSubsystem_count(d.getSubsystem_count()); a.setOriginalKeys(d.getOriginalKeys());
        a.setCustomFields(d.getCustomFields()); a.setMatchedProductIds(d.getMatchedProductIds());
        a.setVdms_id(d.getVdms_id());
    }
}
