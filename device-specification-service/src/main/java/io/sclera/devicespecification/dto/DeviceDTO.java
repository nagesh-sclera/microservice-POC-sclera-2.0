package io.sclera.devicespecification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceDTO {
    private String id;
    private String user_data_name;
    private String user_data_model;
    private String user_data_vendor;
    private String ip_address;
    private String mac_address;
    private String serial_number;
    private String vdms_id;
    private String docker_name;
    private String asset_group;
    private Integer virtual_device_type;
    private Integer status;
    private BigInteger last_seen_on;
    private BigInteger created_timestamp;
    private String subsystem_parent_id;
    private String assigned_user_email;
    private Integer asset_match_status;
    private String warranty;
    private Integer monitor;
    private String inventory_tracking_id;
    private String operational_status;
}
