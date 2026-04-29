package io.sclera.devicespecification.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.devicespecification.client.DeviceServiceClient;
import io.sclera.devicespecification.client.InventoryServiceClient;
import io.sclera.devicespecification.client.ManagedSoftwareServiceClient;
import io.sclera.devicespecification.client.UserServiceClient;
import io.sclera.devicespecification.dto.DeviceDTO;
import io.sclera.devicespecification.dto.DeviceSpecificationResponse;
import io.sclera.devicespecification.model.DeviceInstalledApps;
import io.sclera.devicespecification.model.DeviceNetworkSpecification;
import io.sclera.devicespecification.model.DeviceSpecification;
import io.sclera.devicespecification.repository.DeviceInstalledAppsRepository;
import io.sclera.devicespecification.repository.DeviceNetworkSpecificationRepository;
import io.sclera.devicespecification.repository.DeviceSpecificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Slf4j
@Service
public class DeviceSpecificationService {

    private final DeviceSpecificationRepository specRepo;
    private final DeviceNetworkSpecificationRepository networkRepo;
    private final DeviceInstalledAppsRepository installedAppsRepo;
    private final DeviceServiceClient deviceClient;
    private final InventoryServiceClient inventoryClient;
    private final UserServiceClient userClient;
    private final ManagedSoftwareServiceClient managedSoftwareClient;

    public DeviceSpecificationService(
            DeviceSpecificationRepository specRepo,
            DeviceNetworkSpecificationRepository networkRepo,
            DeviceInstalledAppsRepository installedAppsRepo,
            DeviceServiceClient deviceClient,
            InventoryServiceClient inventoryClient,
            UserServiceClient userClient,
            ManagedSoftwareServiceClient managedSoftwareClient) {
        this.specRepo              = specRepo;
        this.networkRepo           = networkRepo;
        this.installedAppsRepo     = installedAppsRepo;
        this.deviceClient          = deviceClient;
        this.inventoryClient       = inventoryClient;
        this.userClient            = userClient;
        this.managedSoftwareClient = managedSoftwareClient;
    }

    @Transactional
    public String saveFullSpec(JSONObject json) {
        if (json == null || !json.containsKey("systemInfo")) { log.warn("Invalid payload"); return null; }
        try {
            String serialNumber = safeStr(json, "id");
            String ipAddress    = safeStr(json, "deviceIp");
            String deviceName   = safeStr(json, "deviceName");
            String vdmsId       = safeStr(json, "vdmsId");
            String macAddress   = safeStr(json, "macAddress");
            String osType       = safeStr(json, "osType");
            String username     = safeStr(json, "username");
            String userUUID     = safeStr(json, "userUUID");
            String email        = safeStr(json, "email");
            String accountType  = safeStr(json, "accountType");
            JSONObject si       = json.getJSONObject("systemInfo");
            String model        = safeStr(si, "model");
            JSONObject biosJson = objOrEmpty(si, "bios");
            String manufacturer = biosJson.getString("manufacturer");

            // 1. Resolve deviceId via Device Service (WebClient GET)
            DeviceDTO device   = deviceClient.findBySerialNumber(serialNumber);
            String    deviceId = device != null ? device.getId() : null;

            // 2. Notify inventory for unknown device (fire-and-forget WebClient .subscribe())
            if (deviceId == null) {
                String masterEmail = userClient.getMasterUserEmail();
                Map<String, Object> p = new HashMap<>();
                p.put("source", "agent"); p.put("name", deviceName);
                p.put("mac_address", macAddress); p.put("ip_address", ipAddress);
                p.put("assignee", masterEmail); p.put("vdms_id", vdmsId); p.put("model", model);
                if (biosJson.containsKey("serialNumber"))
                    p.put("serial_number", biosJson.getString("serialNumber"));
                inventoryClient.notifyNewDevice(p);
            }

            // 3. Push device field changes + set online via Device Service (WebClient PATCH)
            if (device != null) {
                boolean changed = !Objects.equals(device.getIp_address(), ipAddress)
                        || !Objects.equals(device.getMac_address(), macAddress)
                        || !Objects.equals(device.getUser_data_model(), model)
                        || !Objects.equals(device.getUser_data_name(), deviceName)
                        || !Objects.equals(device.getUser_data_vendor(), manufacturer);
                if (changed)
                    deviceClient.updateDeviceInfo(deviceId, ipAddress, macAddress, model, deviceName, manufacturer);
                deviceClient.setDeviceOnline(deviceId);
            }

            // 4. Upsert DeviceSpecification (own DB)
            DeviceSpecification existing = specRepo.findById(serialNumber).orElse(null);
            specRepo.save(DeviceSpecification.builder()
                    .id(serialNumber).deviceId(deviceId)
                    .createdAt(existing != null ? existing.getCreatedAt() : System.currentTimeMillis())
                    .updatedAt(System.currentTimeMillis())
                    .username(username).email(email).accountType(accountType).userUUID(userUUID)
                    .deviceName(deviceName).model(model).osType(osType)
                    .locationInfo(objJson(si,"location")).osInfo(objJson(si,"osInfo"))
                    .cpuInfo(objJson(si,"cpuInfo")).diskDrives(arrJson(si,"diskDrives"))
                    .physicalDisks(arrJson(si,"physicalDisks")).bios(objJson(si,"bios"))
                    .ramInfo(arrJson(si,"ramInfo")).videoCards(arrJson(si,"videoCards"))
                    .soundDevices(arrJson(si,"soundDevices")).batteryInfo(objJson(si,"batteryInfo"))
                    .processes(arrJson(si,"processes")).systemUpdates(arrJson(si,"systemUpdates"))
                    .childDevices(objJson(si,"childDevices")).build());

            // 5. Upsert DeviceNetworkSpecification (own DB)
            networkRepo.save(DeviceNetworkSpecification.builder()
                    .id(serialNumber).deviceId(deviceId)
                    .networkInterfaces(arrJson(si,"networkInterfaces"))
                    .networkSettings(arrJson(si,"networkSettings"))
                    .networkPorts(arrJson(si,"networkPorts"))
                    .networkProcesses(arrJson(si,"networkProcesses")).build());

            // 6. Installed apps - de-duped locally; ManagedSoftware ID via WebClient POST
            JSONArray apps = si.getJSONArray("installedApps");
            if (apps != null && deviceId != null) {
                List<DeviceInstalledApps> batch = new ArrayList<>();
                long ts = System.currentTimeMillis();
                for (int i = 0; i < apps.size(); i++) {
                    JSONObject app = apps.getJSONObject(i);
                    String appName = app.getString("name");
                    if (installedAppsRepo.existsByDeviceIdAndName(deviceId, appName)) continue;
                    String msId = managedSoftwareClient.insertManagedSoftware(appName, app.getString("publisher"));
                    batch.add(DeviceInstalledApps.builder()
                            .id(UUID.randomUUID().toString()).createdAt(ts).name(appName)
                            .publisher(app.getString("publisher")).version(app.getString("version"))
                            .deviceId(deviceId).deviceSpecificationId(serialNumber).managedSoftwareId(msId).build());
                }
                installedAppsRepo.saveAll(batch);
                log.info("Saved {} installed apps for deviceId: {}", batch.size(), deviceId);
            }

            // 7. Child/virtual device sync via Device Service WebClient
            if (deviceId != null && !deviceId.isBlank())
                syncChildDevices(deviceId, vdmsId, username, specRepo.getChildDeviceByDeviceId(deviceId));

            return deviceId;
        } catch (Exception e) { log.error("saveFullSpec failed", e); return null; }
    }

    @Transactional
    public String applyDelta(JSONObject json) {
        if (json == null || !json.containsKey("systemInfo")) return null;
        String serialNumber = json.getString("id");
        String vdmsId       = json.getString("vdmsId");
        String eventType    = json.getString("eventType");
        JSONObject si       = json.getJSONObject("systemInfo");
        DeviceSpecification spec = specRepo.findById(serialNumber).orElse(null);
        if (spec == null) return null;
        String deviceId = spec.getDeviceId();
        if (deviceId != null) deviceClient.setDeviceOnline(deviceId);
        if (hasArr(si,"processes"))    spec.setProcesses(si.getJSONArray("processes").toJSONString());
        if (hasObj(si,"childDevices")) spec.setChildDevices(si.getJSONObject("childDevices").toJSONString());
        if (!"HighCPU".equalsIgnoreCase(eventType)) {
            DeviceNetworkSpecification ns = networkRepo.findById(serialNumber).orElse(null);
            if (ns != null) {
                if (hasArr(si,"networkInterfaces")) ns.setNetworkInterfaces(si.getJSONArray("networkInterfaces").toJSONString());
                if (hasArr(si,"networkSettings"))   ns.setNetworkSettings(si.getJSONArray("networkSettings").toJSONString());
                if (hasArr(si,"networkPorts"))      ns.setNetworkPorts(si.getJSONArray("networkPorts").toJSONString());
                if (hasArr(si,"networkProcesses"))  ns.setNetworkProcesses(si.getJSONArray("networkProcesses").toJSONString());
                networkRepo.save(ns);
            }
            if (hasObj(si,"batteryInfo")) spec.setBatteryInfo(si.getJSONObject("batteryInfo").toJSONString());
        }
        spec.setUpdatedAt(System.currentTimeMillis());
        specRepo.save(spec);
        if (deviceId != null && !deviceId.isBlank())
            syncChildDevices(deviceId, vdmsId, spec.getUsername(), spec.getChildDevices());
        return "success";
    }

    public DeviceSpecificationResponse getByDeviceId(String deviceId) {
        DeviceSpecification s = specRepo.findByDeviceId(deviceId);
        if (s == null) return null;
        return toResponse(s, networkRepo.findByDeviceId(deviceId));
    }

    public DeviceSpecificationResponse getById(String id) {
        return specRepo.findById(id).map(s -> toResponse(s, networkRepo.findById(id).orElse(null))).orElse(null);
    }

    @Transactional
    public void updateDeviceIdBySerialNumber(String sn, String deviceId) {
        specRepo.updateDeviceIdBySerialNumber(sn, deviceId);
        networkRepo.updateDeviceIdBySerialNumber(sn, deviceId);
        installedAppsRepo.updateDeviceIdBySerialNumber(sn, deviceId);
    }

    @Transactional
    public void deleteByDeviceId(String deviceId) {
        specRepo.deleteByDeviceId(deviceId);
        networkRepo.deleteByDeviceId(deviceId);
        installedAppsRepo.deleteByDeviceId(deviceId);
    }

    public List<String> getDistinctEmails()  { return specRepo.findDistinctEmail(); }
    public List<String> getDistinctOsTypes() { return specRepo.findDistinctOsType(); }

    public JSONArray getSystemUpdates(String deviceId) {
        DeviceSpecification s = specRepo.findByDeviceId(deviceId);
        return (s != null && s.getSystemUpdates() != null) ? JSON.parseArray(s.getSystemUpdates()) : new JSONArray();
    }

    private void syncChildDevices(String parentId, String vdmsId, String username, String raw) {
        if (raw == null) return;
        JSONObject children = parseObj(raw);
        Set<String> created = new HashSet<>();
        for (String cat : children.keySet()) {
            JSONArray arr = children.getJSONArray(cat);
            if (arr == null) continue;
            for (int i = 0; i < arr.size(); i++) {
                JSONObject d = arr.getJSONObject(i);
                String name = d.containsKey("displayName") ? d.getString("displayName") : d.getString("deviceName");
                if (name == null || name.isBlank()) continue;
                if (deviceClient.findByDisplayNameAndParentId(name, parentId).isPresent()) continue;
                String newId = deviceClient.addVirtualDevice(name, parentId, vdmsId, "agent_onboarded_assets", "it_asset");
                if (newId != null) { created.add(newId); deviceClient.incrementSubsystemCount(parentId); }
            }
        }
        if (!created.isEmpty()) deviceClient.onboardVirtualDevices(created, username);
    }

    private String safeStr(JSONObject o, String k) { return (o!=null&&o.containsKey(k))?o.getString(k):null; }
    private JSONObject objOrEmpty(JSONObject o, String k) { return (o.containsKey(k)&&o.getJSONObject(k)!=null)?o.getJSONObject(k):new JSONObject(); }
    private String objJson(JSONObject o, String k) { return (o.containsKey(k)&&o.getJSONObject(k)!=null)?o.getJSONObject(k).toJSONString():"{}"; }
    private String arrJson(JSONObject o, String k) { return (o.containsKey(k)&&o.getJSONArray(k)!=null)?o.getJSONArray(k).toJSONString():"[]"; }
    private boolean hasObj(JSONObject o, String k) { return o.containsKey(k)&&o.getJSONObject(k)!=null; }
    private boolean hasArr(JSONObject o, String k) { return o.containsKey(k)&&o.getJSONArray(k)!=null; }
    private JSONObject parseObj(String s) { try{return JSON.parseObject(s);}catch(Exception e){return new JSONObject();} }

    private DeviceSpecificationResponse toResponse(DeviceSpecification s, DeviceNetworkSpecification n) {
        return DeviceSpecificationResponse.builder()
                .id(s.getId()).deviceId(s.getDeviceId()).username(s.getUsername()).email(s.getEmail())
                .accountType(s.getAccountType()).userUUID(s.getUserUUID()).deviceName(s.getDeviceName())
                .model(s.getModel()).osType(s.getOsType()).locationInfo(s.getLocationInfo())
                .osInfo(s.getOsInfo()).cpuInfo(s.getCpuInfo()).diskDrives(s.getDiskDrives())
                .physicalDisks(s.getPhysicalDisks()).bios(s.getBios()).ramInfo(s.getRamInfo())
                .videoCards(s.getVideoCards()).soundDevices(s.getSoundDevices()).batteryInfo(s.getBatteryInfo())
                .processes(s.getProcesses()).systemUpdates(s.getSystemUpdates()).childDevices(s.getChildDevices())
                .networkInterfaces(n!=null?n.getNetworkInterfaces():null)
                .networkSettings(n!=null?n.getNetworkSettings():null)
                .networkPorts(n!=null?n.getNetworkPorts():null)
                .networkProcesses(n!=null?n.getNetworkProcesses():null)
                .createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt()).build();
    }
}
