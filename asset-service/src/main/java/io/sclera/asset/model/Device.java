/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.asset.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Flattened Device entity – all ORM @ManyToOne / @OneToMany replaced with plain ID columns.
 * Relationships resolved at service level via inter-service WebClient calls.
 */
@Entity
@Table(name = "device")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    private String id;

    @Column(name = "display_name", length = 128)
    private String display_name;

    @Column(name = "ip_address", length = 64)
    private String ip_address;

    @Column(name = "mac_address", length = 32)
    private String mac_address;

    private Integer status;          // 0=offline  1=online
    private String type;
    private String vendor;
    private String model;
    private Integer monitor;

    @Column(name = "last_seen_on")
    private BigInteger lastSeenOn;

    @Column(name = "virtual_device_type")
    private Integer virtualDeviceType;   // 0=real 1=virtual-container 2=virtual-onboarded

    @Column(name = "onboard_status")
    private Integer onboardStatus;        // 0=pending 1=in-progress 2=completed

    @Column(name = "created_timestamp")
    private BigInteger createdTimestamp;

    @Column(name = "updated_timestamp")
    private BigInteger updatedTimestamp;

    @Column(name = "created_email")
    private String createdEmail;

    @Column(name = "updated_email")
    private String updatedEmail;

    @Column(name = "asset_group")
    private String assetGroup;

    private String category;
    private String subCategory;

    @Column(name = "location_status")
    private String locationStatus;

    @Column(name = "digital_twin_image_url")
    private String digitalTwinImageUrl;

    @Column(name = "asset_ocr_image_url")
    private String assetOcrImageUrl;

    @Column(name = "asset_image_url")
    private String assetImageUrl;

    @Column(name = "cost_value", precision = 18, scale = 2)
    private BigDecimal costValue;

    @Column(name = "cost_unit")
    private String costUnit;

    @Column(name = "assigned_user_email")
    private String assignedUserEmail;

    private Boolean aiCall;

    @Column(name = "is_dnd_enabled")
    private Boolean isDndEnabled;

    @Column(name = "operational_status")
    private String operationalStatus;

    @Column(name = "adc_json", columnDefinition = "TEXT")
    private String adcJson;

    // User-supplied overrides
    private String userDataName;
    private String userDataVendor;
    private String userDataModel;
    private String userDataType;

    // ── Floating references (previously ORM associations) ──────────────────
    /**
     * Was: @ManyToOne private Docker docker
     */
    @Column(name = "docker_id")
    private String dockerId;

    /**
     * Was: @ManyToOne private Location location
     */
    @Column(name = "location_id")
    private String locationId;

    /**
     * Was: @ManyToOne @JoinColumn(name="product_id") private Product_Details product_details
     */
    @Column(name = "product_id")
    private String productId;

    /**
     * Was: @ManyToOne private Phonebook global_vendor
     */
    @Column(name = "global_vendor_id")
    private String globalVendorId;

    /**
     * Was: @ManyToOne private Phonebook local_vendor
     */
    @Column(name = "local_vendor_id")
    private String localVendorId;

    /**
     * Tenant – was resolved via Docker→Vdms chain
     */
    @Column(name = "vdms_id")
    private String vdmsId;

    // Protocol status/count columns (kept intact – not relationships)
    @Column(name = "snmp_count")
    private Integer snmpCount;
    @Column(name = "snmp_status", length = 128)
    private String snmpStatus;
    @Column(name = "bacnet_count")
    private Integer bacnetCount;
    @Column(name = "bacnet_status", length = 128)
    private String bacnetStatus;
    @Column(name = "lorawan_count")
    private Integer lorawanCount;
    @Column(name = "lorawan_status", length = 128)
    private String lorawanStatus;
    @Column(name = "document_count")
    private Integer documentCount;
    @Column(name = "media_count")
    private Integer mediaCount;
    @Column(name = "checklist_template_count")
    private Integer checklistTemplateCount;
    @Column(name = "subsystem_count")
    private Integer subsystemCount;
    @Column(name = "subsystem_parent_id")
    private String subsystemParentId;
    @Column(name = "serial_number")
    private String serialNumber;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Column(name = "custom_fields", columnDefinition = "LONGTEXT")
    private String customFields;
    @Column(name = "network_layer")
    private Integer networkLayer;
    @Column(name = "asset_match_status")
    private Integer assetMatchStatus;
}
