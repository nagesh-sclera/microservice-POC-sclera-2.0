/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Flattened – was @OneToOne with Device; now holds device_id as a plain column.
 */
@Entity
@Table(name = "device_onboard_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceOnboardStatus {

    @Id
    private String id;

    @Column(name = "assignee_email")
    private String assignee_email;

    @Column(name = "image_status")
    private Integer image_status = 0;         // 0=pending 1=in-progress 2=completed

    @Column(name = "geolocation_status")
    private Integer geolocation_status = 0;

    @Column(name = "tag_status")
    private Integer tag_status = 0;

    @Column(name = "field_status")
    private Integer field_status = 0;

    /** Was: @OneToOne private Device device */
    @Column(name = "device_id", nullable = false)
    private String device_id;

    @Column(name = "is_removed")
    private Boolean is_removed = false;

    @Column(name = "sync_timestamp")
    private Long sync_timestamp;
}
