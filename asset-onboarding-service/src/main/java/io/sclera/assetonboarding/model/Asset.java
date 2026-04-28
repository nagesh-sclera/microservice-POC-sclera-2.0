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
 * Flattened Asset entity – @ManyToOne Vdms replaced with vdms_id column.
 * assetDeviceMappings @OneToMany removed; queried via AssetDeviceMappingRepository.
 */
@Entity
@Table(name = "asset")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    public String id;

    @Column(name = "display_name", length = 128)
    public String display_name;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "mac_address", length = 32)
    public String mac_address;

    public String model;
    public String vendor;
    public String type;

    @Column(name = "ip_address", length = 64)
    public String ip_address;

    @Column(name = "network_layer")
    public Integer network_layer;

    @Column(name = "serial_number")
    public String serial_number;

    @Column(name = "import_type")
    public String import_type;   // corrigo | upload | manual

    public String warranty;

    @Column(name = "is_matched")
    public Boolean isMatched;

    @Column(name = "subsystem_parent_id")
    public String subsystem_parent_id;

    @Column(name = "subsystem_count")
    public Integer subsystem_count;

    @Column(name = "original_keys", columnDefinition = "LONGTEXT", nullable = false)
    public String originalKeys;

    @Column(name = "custom_fields", columnDefinition = "LONGTEXT")
    public String customFields;

    @Column(name = "matched_product_ids", columnDefinition = "TEXT")
    public String matchedProductIds;

    /** Was: @ManyToOne private Vdms vdms */
    @Column(name = "vdms_id", length = 64)
    public String vdms_id;
}
