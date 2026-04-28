/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.asset.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DeviceDTO {
    private String  id;
    private String  display_name;
    private String  ip_address;
    private String  mac_address;
    private Integer status;
    private String  type;
    private String  vendor;
    private String  model;
    private Integer monitor;
    private Integer virtual_device_type;
    private Integer onboard_status;
    private String  asset_group;
    private String  category;
    private String  sub_category;
    private BigDecimal cost_value;
    private String  cost_unit;
    private String  assigned_user_email;
    private String  operational_status;
    private String  adc_json;
    private String  asset_image_url;
    private String  docker_id;
    private String  location_id;
    private String  product_id;
    private String  vdms_id;
    private String  serial_number;
    private String  description;
    private String  custom_fields;
    private Integer network_layer;
}
