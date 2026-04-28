/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboarding.repository;

import io.sclera.assetonboarding.model.AssetDeviceMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetDeviceMappingRepository extends JpaRepository<AssetDeviceMapping, String> {

    List<AssetDeviceMapping> findByDeviceId(String deviceId);

    List<AssetDeviceMapping> findByAssetId(String assetId);

    Optional<AssetDeviceMapping> findByDeviceIdAndAssetId(String deviceId, String assetId);

//    void deleteByDevice_id(String deviceId);
}
