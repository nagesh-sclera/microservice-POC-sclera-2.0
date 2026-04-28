/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboarding.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Flattened junction table.
 * Was: @ManyToOne Device device, @ManyToOne Asset asset
 * Now: plain String IDs – resolved cross-service when needed.
 */
@Entity
@Table(name = "asset_device_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetDeviceMapping {

    @Id
    private String id;

    /** Was: @ManyToOne(fetch = EAGER) private Device device */
    @Column(name = "device_id", nullable = false)
    private String deviceId;

    /** Was: @ManyToOne(fetch = EAGER) private Asset asset */
    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Column(name = "match_score")
    private Integer matchScore;
}
