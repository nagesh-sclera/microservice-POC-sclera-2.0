/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.assetonboarding.repository;

import io.sclera.assetonboarding.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {

    @Query(value =
        "SELECT * FROM asset " +
        "WHERE vdms_id = :vdmsId " +
        "  AND (:importType IS NULL OR import_type = :importType) " +
        "  AND (:search IS NULL " +
        "       OR display_name LIKE CONCAT('%',:search,'%') " +
        "       OR mac_address LIKE CONCAT('%',:search,'%') " +
        "       OR ip_address LIKE CONCAT('%',:search,'%')) " +
        "LIMIT :pageSize OFFSET :offset",
        nativeQuery = true)
    List<Asset> getPaginated(@Param("vdmsId")     String  vdmsId,
                             @Param("importType") String  importType,
                             @Param("search")     String  search,
                             @Param("pageSize")   int     pageSize,
                             @Param("offset")     int     offset);

    @Query(value =
        "SELECT a.* FROM asset a " +
        "LEFT JOIN asset_device_mapping m ON m.asset_id = a.id " +
        "WHERE a.vdms_id = :vdmsId AND m.id IS NULL " +
        "LIMIT :pageSize OFFSET :offset",
        nativeQuery = true)
    List<Asset> getUnmapped(@Param("vdmsId")   String vdmsId,
                            @Param("pageSize") int    pageSize,
                            @Param("offset")   int    offset);

    @Modifying
    @Query("UPDATE Asset a SET a.isMatched = :matched WHERE a.id = :id")
    int setMatched(@Param("id") String id, @Param("matched") Boolean matched);

    @Modifying
    @Query("UPDATE Asset a SET a.matchedProductIds = :products WHERE a.id = :id")
    int saveMatchedProducts(@Param("id") String id, @Param("products") String products);
}
