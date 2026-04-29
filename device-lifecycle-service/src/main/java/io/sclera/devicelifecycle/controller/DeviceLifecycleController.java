package io.sclera.devicelifecycle.controller;

import io.sclera.devicelifecycle.dto.DeviceLifecycleHistoryDTO;
import io.sclera.devicelifecycle.service.DeviceLifecycleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/device-lifecycle")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeviceLifecycleController {

    private final DeviceLifecycleService service;

    public DeviceLifecycleController(DeviceLifecycleService service) {
        this.service = service;
    }

    @PostMapping("/user/{username}/vdms/{vdmsId}/adddevicehistory")
    public ResponseEntity<Void> addHistory(@PathVariable String username,
                                           @PathVariable String vdmsId,
                                           @RequestBody DeviceLifecycleHistoryDTO dto,
                                           @RequestParam String retireStatus) {
        service.addHistory(username, vdmsId, dto, retireStatus);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{username}/vdms/{vdmsId}/device/{deviceId}/getdevicehistory")
    public ResponseEntity<Set<DeviceLifecycleHistoryDTO>> getHistory(
            @PathVariable String username,
            @PathVariable String vdmsId,
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        return ResponseEntity.ok(service.getHistory(deviceId, pageNo, pageSize));
    }

    @GetMapping("/device/{deviceId}/lifecycle/latest-status")
    public ResponseEntity<String> getLatestStatus(@PathVariable String deviceId) {
        String status = service.getLatestOperationalStatus(deviceId);
        return status != null ? ResponseEntity.ok(status) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/device/{deviceId}/lifecycle/history")
    public ResponseEntity<Void> deleteHistory(@PathVariable String deviceId) {
        service.deleteByDeviceId(deviceId);
        return ResponseEntity.noContent().build();
    }
}
