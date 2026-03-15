package com.izenshy.gessainvoice.modules.product.reports.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductsSoldMonthlyDTO {
    private Long enterpriseId;
    private Long outletId;
    private LocalDate mes;
    private Long productId;
    private String productName;
    private String categoryName;
    private String detailName;
    private BigDecimal cantidadVendidaMes;
    private BigDecimal totalVendidoMesUsd;
}