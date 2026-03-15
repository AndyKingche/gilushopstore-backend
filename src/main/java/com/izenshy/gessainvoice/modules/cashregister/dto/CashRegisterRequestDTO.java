package com.izenshy.gessainvoice.modules.cashregister.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CashRegisterRequestDTO implements Serializable {
    // FK NOT NULL
    private Long userId;
    private Long outletId;
    private Long enterpriseId;

    // NOT NULL con default
    private BigDecimal openingCash = BigDecimal.ZERO;
    private BigDecimal openingTransfer = BigDecimal.ZERO;
    private BigDecimal openingTotal = BigDecimal.ZERO;

    private BigDecimal totalSalesCash = BigDecimal.ZERO;
    private BigDecimal totalSalesTransfer = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal totalInvestments = BigDecimal.ZERO;

    // NOT NULL con default en BD
    private String status = "ABIERTA";

    // Nullable
    private String openingNotes;
}