/*
 * Author  : nagesh.nayak@sclera.com
 * Project : Sclera 2.0 AMP
 * © 2026 Sclera. All rights reserved.
 * This file is read-only. Unauthorized modification or distribution is prohibited.
 */
package io.sclera.asset.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private long    totalElements;
    private int     page;
    private int     size;
}
