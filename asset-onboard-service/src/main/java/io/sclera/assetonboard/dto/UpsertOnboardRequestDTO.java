/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpsertOnboardRequestDTO {
    private List<DeviceOnboardEntryDTO> devices;

    @Data
    public static class DeviceOnboardEntryDTO {
        private String  device_id;
        private Integer onboard_status;   // 0 | 1 | 2
        private String  assignee_email;
    }
}
