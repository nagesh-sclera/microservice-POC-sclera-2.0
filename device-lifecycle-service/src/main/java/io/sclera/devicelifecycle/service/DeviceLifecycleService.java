package io.sclera.devicelifecycle.service;

import com.fasterxml.uuid.Generators;
import io.sclera.devicelifecycle.client.DeviceServiceClient;
import io.sclera.devicelifecycle.client.InventoryServiceClient;
import io.sclera.devicelifecycle.dto.DeviceDTO;
import io.sclera.devicelifecycle.dto.DeviceLifecycleHistoryDTO;
import io.sclera.devicelifecycle.repository.DeviceLifecycleHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

@Slf4j
@Service
public class DeviceLifecycleService {

    private final DeviceLifecycleHistoryRepository repository;
    private final DeviceServiceClient deviceClient;
    private final InventoryServiceClient inventoryClient;

    public DeviceLifecycleService(DeviceLifecycleHistoryRepository repository,
                                  DeviceServiceClient deviceClient,
                                  InventoryServiceClient inventoryClient) {
        this.repository      = repository;
        this.deviceClient    = deviceClient;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public void addHistory(String username, String vdmsId,
                           DeviceLifecycleHistoryDTO dto, String retireStatus) {
        if (dto.getId() == null) dto.setId(Generators.timeBasedGenerator().generate().toString());

        // Determine usage status based on prior assignment count
        int assignmentCount = 0;
        Integer latestCount = repository.getLatestAssignmentCount(dto.getDeviceId());
        if (latestCount != null) assignmentCount = latestCount;

        dto.setUsageStatus((dto.getAssignedUserId() == null && assignmentCount == 0)
                        || (assignmentCount == 0 && dto.getAssignedUserId() != null) ? "new" : "used");

        if (dto.getAssignedUserId() != null) {
            assignmentCount += 1;
            dto.setId(Generators.timeBasedGenerator().generate().toString());
            dto.setCreatedTimestamp(BigInteger.valueOf(System.currentTimeMillis()));
            dto.setAssignedTimestamp(BigInteger.valueOf(System.currentTimeMillis()));
            dto.setAssignmentCount(assignmentCount);
        } else {
            dto.setId(Generators.timeBasedGenerator().generate().toString());
            dto.setCreatedTimestamp(BigInteger.valueOf(System.currentTimeMillis()));
            dto.setAssignedTimestamp(null);
            dto.setAssignmentCount(assignmentCount);
        }

        // Sync operational status back to Device Service (WebClient PATCH) if changed
        String latestStatus = repository.getLatestOperationalStatus(dto.getDeviceId());
        if (dto.getOperationalStatus() != null
                && !dto.getOperationalStatus().equalsIgnoreCase(latestStatus)) {
            deviceClient.updateOperationalStatus(dto.getDeviceId(), dto.getOperationalStatus());
        }

        // Clear assigned user email on retire / unassign (WebClient PATCH)
        if ("true".equalsIgnoreCase(retireStatus) || "false".equalsIgnoreCase(retireStatus)) {
            deviceClient.updateAssignedUserEmail(dto.getDeviceId(), null);
        }

        // Full retire flow: archive device + retire in inventory (WebClient POST x2)
        if ("true".equalsIgnoreCase(retireStatus)) {
            DeviceDTO device = deviceClient.getDeviceById(dto.getDeviceId());
            if (device != null && device.getInventory_tracking_id() != null) {
                deviceClient.archiveDevices(username, vdmsId,
                        Collections.singleton(dto.getDeviceId()));
                inventoryClient.retireInventoryDevice(vdmsId, dto.getDeviceId(),
                        username, dto.getDescription(), device.getInventory_tracking_id());
            }
        }

        // Persist history record to own DB
        repository.insertHistory(
                dto.getId(), dto.getOperationalStatus(), dto.getUsageStatus(),
                dto.getAssignedUserId(), dto.getAssignmentCount(),
                dto.getCreatedTimestamp(), dto.getAssignedTimestamp(),
                dto.getDeviceId(), dto.getDescription(), dto.getAssignedByUserId());

        log.info("DeviceLifecycleHistory added for deviceId: {}", dto.getDeviceId());
    }

    public Set<DeviceLifecycleHistoryDTO> getHistory(String deviceId, Integer pageNo, Integer pageSize) {
        if (pageNo  == null || pageNo  < 1) pageNo  = 1;
        if (pageSize == null || pageSize < 1) pageSize = 5;
        Integer offset = pageSize * (pageNo - 1);
        Set<DeviceLifecycleHistoryDTO> result = repository.getDeviceLifeCycleHistory(deviceId, pageSize, offset);
        return result != null ? result : Collections.emptySet();
    }

    public String getLatestOperationalStatus(String deviceId) {
        return repository.getLatestOperationalStatus(deviceId);
    }

    @Transactional
    public void deleteByDeviceId(String deviceId) {
        repository.deleteByDeviceId(deviceId);
        log.info("Deleted lifecycle history for deviceId: {}", deviceId);
    }
}
