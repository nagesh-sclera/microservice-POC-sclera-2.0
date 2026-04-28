/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboardingai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class MatchResultDTO {
    private String          device_id;
    private List<Candidate> candidates;

    @Data
    @AllArgsConstructor
    public static class Candidate {
        private String  asset_id;
        private String  asset_display_name;
        private Integer score;          // 0-100
        private String  match_reason;   // e.g. "MAC+Vendor match"
    }
}
