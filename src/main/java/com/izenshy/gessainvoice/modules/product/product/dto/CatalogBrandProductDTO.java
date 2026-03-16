package com.izenshy.gessainvoice.modules.product.product.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CatalogBrandProductDTO {
    private Long id;
    private Long brandId;
    private Long productId;
    private LocalDateTime createdAt;
}