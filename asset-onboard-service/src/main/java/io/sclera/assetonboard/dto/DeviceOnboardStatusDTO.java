/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboard.dto;

import lombok.Data;

@Data
public class DeviceOnboardStatusDTO {
    private String  assignee_email;
    private Integer image_status;
    private Integer geolocation_status;
    private Integer tag_status;
    private Integer field_status;
}
