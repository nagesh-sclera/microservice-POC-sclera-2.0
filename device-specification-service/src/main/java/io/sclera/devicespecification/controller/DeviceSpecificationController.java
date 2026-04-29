package io.sclera.devicespecification.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.sclera.devicespecification.dto.DeviceSpecificationResponse;
import io.sclera.devicespecification.service.DeviceSpecificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device-specification")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeviceSpecificationController {

    private final DeviceSpecificationService service;

    public DeviceSpecificationController(DeviceSpecificationService service) {
        this.service = service;
    }

    @PostMapping("/devicespecification")
    public ResponseEntity<String> receiveFullSpec(@RequestBody ObjectNode body) {
        String deviceId = service.saveFullSpec(JSONObject.parseObject(body.toString()));
        return deviceId != null ? ResponseEntity.ok(deviceId) : ResponseEntity.ok().build();
    }

    @PostMapping("/deltadevicespecs")
    public ResponseEntity<String> receiveDelta(@RequestBody ObjectNode body) {
        String result = service.applyDelta(JSONObject.parseObject(body.toString()));
        return result != null ? ResponseEntity.ok(result)
                              : ResponseEntity.badRequest().body("Invalid input or device not found");
    }

    @GetMapping("/devicespecification/{deviceId}")
    public ResponseEntity<DeviceSpecificationResponse> getByDeviceId(@PathVariable String deviceId) {
        DeviceSpecificationResponse r = service.getByDeviceId(deviceId);
        return ResponseEntity.ok(r != null ? r : DeviceSpecificationResponse.builder().build());
    }

    @GetMapping("/systemupdates/{deviceId}")
    public ResponseEntity<JSONArray> getSystemUpdates(@PathVariable String deviceId) {
        return ResponseEntity.ok(service.getSystemUpdates(deviceId));
    }

    @PutMapping("/devicespecification/{sn}/device/{deviceId}")
    public ResponseEntity<Void> updateDeviceId(@PathVariable String sn, @PathVariable String deviceId) {
        service.updateDeviceIdBySerialNumber(sn, deviceId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/devicespecification/device/{deviceId}")
    public ResponseEntity<Void> deleteByDeviceId(@PathVariable String deviceId) {
        service.deleteByDeviceId(deviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/devicespecification/emails")
    public ResponseEntity<List<String>> getEmails() {
        return ResponseEntity.ok(service.getDistinctEmails());
    }

    @GetMapping("/devicespecification/os-types")
    public ResponseEntity<List<String>> getOsTypes() {
        return ResponseEntity.ok(service.getDistinctOsTypes());
    }
}
