/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.asset.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class OnboardStatusPatchDTO {
    @NotNull
    private Integer onboard_status;   // 0=pending 1=in-progress 2=completed
}
