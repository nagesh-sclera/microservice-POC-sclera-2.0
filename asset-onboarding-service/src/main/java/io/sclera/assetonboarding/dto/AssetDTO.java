/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboarding.dto;

import lombok.Data;

@Data
public class AssetDTO {
    private String  id;
    private String  display_name;
    private String  description;
    private String  mac_address;
    private String  model;
    private String  vendor;
    private String  type;
    private String  ip_address;
    private Integer network_layer;
    private String  serial_number;
    private String  import_type;
    private String  warranty;
    private Boolean isMatched;
    private String  subsystem_parent_id;
    private Integer subsystem_count;
    private String  originalKeys;
    private String  customFields;
    private String  matchedProductIds;
    private String  vdms_id;
}
