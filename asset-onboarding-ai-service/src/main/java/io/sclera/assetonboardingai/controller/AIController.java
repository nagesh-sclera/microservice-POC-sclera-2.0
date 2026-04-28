/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboardingai.controller;

import io.sclera.assetonboardingai.dto.AISuggestionRequestDTO;
import io.sclera.assetonboardingai.dto.MatchResultDTO;
import io.sclera.assetonboardingai.service.AIMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class AIController {

    private final AIMatchingService aiMatchingService;

    /**
     * POST /api/ai/suggest
     * Returns ranked candidates for a single device.
     */
    @PostMapping("/suggest")
    public ResponseEntity<MatchResultDTO> suggest(
            @Valid @RequestBody AISuggestionRequestDTO request) {
        return ResponseEntity.ok(
                aiMatchingService.suggestMatches(request.getDevice_id(), request.getVdms_id()));
    }

    /**
     * POST /api/ai/batch-match?vdmsId=
     * Auto-matches all AI-enabled devices in a tenant; persists high-confidence mappings.
     */
    @PostMapping("/batch-match")
    public ResponseEntity<List<MatchResultDTO>> batchMatch(@RequestParam String vdmsId) {
        return ResponseEntity.ok(aiMatchingService.batchMatch(vdmsId));
    }
}
