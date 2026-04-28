/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.asset.service;

import io.sclera.asset.dto.DeviceDTO;
import io.sclera.asset.dto.OnboardStatusPatchDTO;
import io.sclera.asset.dto.PageResponse;
import io.sclera.asset.model.Device;
import io.sclera.asset.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceDTO getById(String id, String vdmsId) {
        Device d = deviceRepository.findByIdAndVdmsId(id, vdmsId)
                .orElseThrow(() -> new NoSuchElementException("Device not found: " + id));
        return toDTO(d);
    }

    public PageResponse<DeviceDTO> filter(String vdmsId, String dockerId, String search,
                                          Integer virtualType, Integer status,
                                          int page, int size) {
        int offset = page * size;
        List<Device> devices = deviceRepository.filterDevices(
                vdmsId, dockerId, search, virtualType, status, size, offset);
        long total = deviceRepository.count();
        return new PageResponse<>(devices.stream().map(this::toDTO).collect(Collectors.toList()),
                total, page, size);
    }

    @Transactional
    public DeviceDTO create(DeviceDTO dto, String username) {
        Device d = new Device();
        applyDTO(dto, d);
        d.setId(UUID.randomUUID().toString());
        d.setCreatedTimestamp(BigInteger.valueOf(System.currentTimeMillis()));
        d.setCreatedEmail(username);
        return toDTO(deviceRepository.save(d));
    }

    @Transactional
    public DeviceDTO update(String id, DeviceDTO dto, String username) {
        Device d = deviceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Device not found: " + id));
        applyDTO(dto, d);
        d.setUpdatedTimestamp(BigInteger.valueOf(System.currentTimeMillis()));
        d.setUpdatedEmail(username);
        return toDTO(deviceRepository.save(d));
    }

    @Transactional
    public void patchOnboardStatus(String id, OnboardStatusPatchDTO patch) {
        int rows = deviceRepository.updateOnboardStatus(id, patch.getOnboard_status());
        if (rows == 0) throw new NoSuchElementException("Device not found: " + id);
    }

    @Transactional
    public void delete(String id) {
        if (!deviceRepository.existsById(id))
            throw new NoSuchElementException("Device not found: " + id);
        deviceRepository.deleteById(id);
    }

    public Map<String, Long> onboardCountByDocker(String dockerId) {
        return Map.of(
            "pending",     deviceRepository.countByDockerIdAndOnboardStatus(dockerId, 0),
            "in_progress", deviceRepository.countByDockerIdAndOnboardStatus(dockerId, 1),
            "completed",   deviceRepository.countByDockerIdAndOnboardStatus(dockerId, 2)
        );
    }

    // ── mapping helpers ──────────────────────────────────────────────────
    private DeviceDTO toDTO(Device d) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(d.getId());
        dto.setDisplay_name(d.getDisplay_name());
        dto.setIp_address(d.getIp_address());
        dto.setMac_address(d.getMac_address());
        dto.setStatus(d.getStatus());
        dto.setType(d.getType());
        dto.setVendor(d.getVendor());
        dto.setModel(d.getModel());
        dto.setMonitor(d.getMonitor());
        dto.setVirtual_device_type(d.getVirtualDeviceType());
        dto.setOnboard_status(d.getOnboardStatus());
        dto.setAsset_group(d.getAssetGroup());
        dto.setCategory(d.getCategory());
        dto.setSub_category(d.getSubCategory());
        dto.setCost_value(d.getCostValue());
        dto.setCost_unit(d.getCostUnit());
        dto.setAssigned_user_email(d.getAssignedUserEmail());
        dto.setOperational_status(d.getOperationalStatus());
        dto.setAdc_json(d.getAdcJson());
        dto.setAsset_image_url(d.getAssetImageUrl());
        dto.setDocker_id(d.getDockerId());
        dto.setLocation_id(d.getLocationId());
        dto.setProduct_id(d.getProductId());
        dto.setVdms_id(d.getVdmsId());
        dto.setSerial_number(d.getSerialNumber());
        dto.setDescription(d.getDescription());
        dto.setCustom_fields(d.getCustomFields());
        dto.setNetwork_layer(d.getNetworkLayer());
        return dto;
    }

    private void applyDTO(DeviceDTO dto, Device d) {
        d.setDisplay_name(dto.getDisplay_name());
        d.setIp_address(dto.getIp_address());
        d.setMac_address(dto.getMac_address());
        d.setStatus(dto.getStatus());
        d.setType(dto.getType());
        d.setVendor(dto.getVendor());
        d.setModel(dto.getModel());
        d.setMonitor(dto.getMonitor());
        d.setVirtualDeviceType(dto.getVirtual_device_type());
        d.setOnboardStatus(dto.getOnboard_status());
        d.setAssetGroup(dto.getAsset_group());
        d.setCategory(dto.getCategory());
        d.setSubCategory(dto.getSub_category());
        d.setCostValue(dto.getCost_value());
        d.setCostUnit(dto.getCost_unit());
        d.setAssignedUserEmail(dto.getAssigned_user_email());
        d.setOperationalStatus(dto.getOperational_status());
        d.setAdcJson(dto.getAdc_json());
        d.setAssetImageUrl(dto.getAsset_image_url());
        d.setDockerId(dto.getDocker_id());
        d.setLocationId(dto.getLocation_id());
        d.setProductId(dto.getProduct_id());
        d.setVdmsId(dto.getVdms_id());
        d.setSerialNumber(dto.getSerial_number());
        d.setDescription(dto.getDescription());
        d.setCustomFields(dto.getCustom_fields());
        d.setNetworkLayer(dto.getNetwork_layer());
    }
}
