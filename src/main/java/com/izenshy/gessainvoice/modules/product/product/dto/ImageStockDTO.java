package com.izenshy.gessainvoice.modules.product.product.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ImageStockDTO {
    private Long id;
    private UUID imageStockUuid;
    private Long stockProductId;
    private Long stockOutletId;
    private String imageUrl;
    private Integer imageOrder;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}