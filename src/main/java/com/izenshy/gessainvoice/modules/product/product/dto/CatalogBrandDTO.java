package com.izenshy.gessainvoice.modules.product.product.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CatalogBrandDTO {
    private Long id;
    private UUID brandUuid;
    private String brandName;
    private String brandDescription;
    private String brandLogoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}