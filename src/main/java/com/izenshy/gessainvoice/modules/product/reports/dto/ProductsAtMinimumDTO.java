package com.izenshy.gessainvoice.modules.product.reports.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductsAtMinimumDTO {
    private Long outletId;
    private String outletName;
    private Long productId;
    private String productName;
    private String categoryName;
    private String detailName;
    private BigDecimal stockQuantity;
    private Integer stockMin;
}