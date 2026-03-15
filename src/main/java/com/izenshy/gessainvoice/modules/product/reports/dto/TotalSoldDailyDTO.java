package com.izenshy.gessainvoice.modules.product.reports.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TotalSoldDailyDTO {
    private Long enterpriseId;
    private Long outletId;
    private LocalDate dia;
    private BigDecimal totalVendidoDia;
}