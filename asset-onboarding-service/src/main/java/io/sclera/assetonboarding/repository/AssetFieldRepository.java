/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboarding.repository;

import io.sclera.assetonboarding.model.AssetField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetFieldRepository extends JpaRepository<AssetField, String> {

    @Query("SELECT f FROM AssetField f WHERE f.isActive = true AND f.isDeleted = false ORDER BY f.showInSection")
    List<AssetField> getAllActive();
}
