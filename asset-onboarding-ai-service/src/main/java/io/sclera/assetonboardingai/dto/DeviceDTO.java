/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboardingai.dto;

import lombok.Data;

@Data
public class DeviceDTO {
    private String  id;
    private String  mac_address;
    private String  ip_address;
    private String  vendor;
    private String  model;
    private String  type;
    private String  serial_number;
    private Boolean ai_call;
}
