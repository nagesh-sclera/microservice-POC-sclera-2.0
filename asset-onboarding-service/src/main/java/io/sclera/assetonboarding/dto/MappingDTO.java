/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboarding.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class MappingDTO {
    @NotBlank private String device_id;
    @NotBlank private String asset_id;
    private Integer matchScore;
}
