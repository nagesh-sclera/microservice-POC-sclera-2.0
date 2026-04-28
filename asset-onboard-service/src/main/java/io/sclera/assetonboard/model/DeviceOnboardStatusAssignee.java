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
 * Flattened – was @ManyToOne DeviceOnboardStatus; now holds device_onboard_status_id.
 */
@Entity
@Table(name = "device_onboard_status_assignee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceOnboardStatusAssignee {

    @Id
    private String id;

    private String type;   // technician | reviewer
    private String email;

    /** Was: @ManyToOne private DeviceOnboardStatus device_onboard_status */
    @Column(name = "device_onboard_status_id", nullable = false)
    private String device_onboard_status_id;
}
