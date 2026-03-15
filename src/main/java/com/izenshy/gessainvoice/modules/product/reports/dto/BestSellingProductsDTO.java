package com.izenshy.gessainvoice.modules.product.reports.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BestSellingProductsDTO {
    private Long enterpriseId;
    private Long outletId;
    private Long productId;
    private String productName;
    private String categoryName;
    private String detailName;
    private BigDecimal totalCantidadVendida;
    private BigDecimal totalVendidoUsd;
}