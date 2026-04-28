/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboardingai.service;

import io.sclera.assetonboardingai.client.AssetOnboardingServiceClient;
import io.sclera.assetonboardingai.client.AssetServiceClient;
import io.sclera.assetonboardingai.dto.AssetDTO;
import io.sclera.assetonboardingai.dto.DeviceDTO;
import io.sclera.assetonboardingai.dto.MatchResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIMatchingService {

    private final AssetServiceClient assetServiceClient;
    private final AssetOnboardingServiceClient onboardingServiceClient;

    @Value("${ai.match-threshold:70}")
    private int matchThreshold;

    /**
     * Score-based matching for a single device against all unmapped assets.
     * Uses field similarity: MAC (40pts) + IP (20pts) + vendor (20pts) + model (10pts) + type (10pts).
     */
    public MatchResultDTO suggestMatches(String deviceId, String vdmsId) {
        DeviceDTO device = assetServiceClient.getDevice(deviceId, vdmsId);
        List<AssetDTO> candidates = onboardingServiceClient.getUnmappedAssets(vdmsId, 0, 200);

        List<MatchResultDTO.Candidate> scored = candidates.stream()
                .map(asset -> score(device, asset))
                .filter(c -> c.getScore() > 0)
                .sorted(Comparator.comparingInt(MatchResultDTO.Candidate::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return new MatchResultDTO(deviceId, scored);
    }

    /**
     * Batch AI matching: auto-creates mappings for high-confidence pairs.
     */
    public List<MatchResultDTO> batchMatch(String vdmsId) {
        List<DeviceDTO> devices = assetServiceClient.getDevicesWithAiEnabled(vdmsId, 0, 500);
        List<AssetDTO> assets = onboardingServiceClient.getUnmappedAssets(vdmsId, 0, 500);

        List<MatchResultDTO> results = new ArrayList<>();
        for (DeviceDTO device : devices) {
            List<MatchResultDTO.Candidate> candidates = assets.stream()
                    .map(a -> score(device, a))
                    .filter(c -> c.getScore() >= matchThreshold)
                    .sorted(Comparator.comparingInt(MatchResultDTO.Candidate::getScore).reversed())
                    .limit(3)
                    .collect(Collectors.toList());

            if (!candidates.isEmpty()) {
                MatchResultDTO.Candidate best = candidates.get(0);
                if (best.getScore() >= matchThreshold) {
                    try {
                        onboardingServiceClient.createMapping(
                                device.getId(), best.getAsset_id(), best.getScore(), vdmsId);
                        log.info("Auto-mapped device {} -> asset {} (score {})",
                                device.getId(), best.getAsset_id(), best.getScore());
                    } catch (Exception e) {
                        log.warn("Auto-mapping skipped: {}", e.getMessage());
                    }
                }
                results.add(new MatchResultDTO(device.getId(), candidates));
            }
        }
        return results;
    }

    // ── scoring logic ────────────────────────────────────────────────────
    private MatchResultDTO.Candidate score(DeviceDTO d, AssetDTO a) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (eq(d.getMac_address(), a.getMac_address())) {
            score += 40;
            reasons.add("MAC");
        }
        if (eq(d.getIp_address(), a.getIp_address())) {
            score += 20;
            reasons.add("IP");
        }
        if (eqIgnoreCase(d.getVendor(), a.getVendor())) {
            score += 20;
            reasons.add("Vendor");
        }
        if (eqIgnoreCase(d.getModel(), a.getModel())) {
            score += 10;
            reasons.add("Model");
        }
        if (eqIgnoreCase(d.getType(), a.getType())) {
            score += 10;
            reasons.add("Type");
        }

        return new MatchResultDTO.Candidate(
                a.getId(), a.getDisplay_name(), score,
                reasons.isEmpty() ? "No match" : String.join("+", reasons) + " match");
    }

    private boolean eq(String a, String b) {
        return a != null && a.equals(b);
    }

    private boolean eqIgnoreCase(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }
}
