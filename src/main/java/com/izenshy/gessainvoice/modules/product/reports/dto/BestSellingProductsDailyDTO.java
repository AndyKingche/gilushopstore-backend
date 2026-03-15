package com.izenshy.gessainvoice.modules.product.reports.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BestSellingProductsDailyDTO {
    private LocalDate dia;
    private Long enterpriseId;
    private Long outletId;
    private Long productId;
    private String productName;
    private String categoryName;
    private String detailName;
    private BigDecimal cantidadVendidaDia;
    private BigDecimal totalVendidoDiaUsd;
}