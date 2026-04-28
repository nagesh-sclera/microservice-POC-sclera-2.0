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
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@Table(name = "asset_field")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetField {

    @Id
    private String id;

    private String  name;
    private String  type;            // text | select | date | number

    @Column(name = "tool_tip")
    private String  toolTip;

    @Column(name = "default_value")
    private String  defaultValue;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive = true;

    @ColumnDefault("0")
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(columnDefinition = "TEXT")
    private String  options;         // JSON array for select fields

    @Column(name = "show_in_section")
    private Integer showInSection;

    @Column(name = "created_at")
    private BigInteger createdAt;
}
