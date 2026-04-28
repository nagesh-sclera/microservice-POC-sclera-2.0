/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboard.dto;

import lombok.Data;

/** Subset of asset-service DeviceDTO needed by this service. */
@Data
public class DeviceResponseDTO {
    private String  id;
    private String  vdms_id;
    private String  docker_id;
    private Integer onboard_status;
    private Integer virtual_device_type;
}
