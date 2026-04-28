/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboardingai.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class AISuggestionRequestDTO {
    @NotBlank private String device_id;
    @NotBlank private String vdms_id;
}
