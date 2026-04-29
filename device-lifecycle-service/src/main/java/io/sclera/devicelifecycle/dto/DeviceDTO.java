package io.sclera.devicelifecycle.dto;

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
    private Integer status;
    private BigInteger last_seen_on;
    private String operational_status;
    private String assigned_user_email;
    private String inventory_tracking_id;
}
