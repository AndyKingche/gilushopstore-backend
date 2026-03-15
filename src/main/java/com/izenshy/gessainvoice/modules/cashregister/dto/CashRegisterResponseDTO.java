package com.izenshy.gessainvoice.modules.cashregister.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CashRegisterResponseDTO implements Serializable {
    private Long id;
    private UUID cashRegisterUuid;
    private Long userId;
    private Long outletId;
    private Long enterpriseId;
    private LocalDateTime openingDate;
    private LocalDateTime closingDate;
    private BigDecimal openingCash;
    private BigDecimal openingTransfer;
    private BigDecimal openingTotal;
    private BigDecimal closingCash;
    private BigDecimal closingTransfer;
    private BigDecimal closingTotal;
    private BigDecimal totalSalesCash;
    private BigDecimal totalSalesTransfer;
    private BigDecimal totalExpenses;
    private BigDecimal totalInvestments;
    private BigDecimal cashDifference;
    private BigDecimal transferDifference;
    private String status;
    private String openingNotes;
    private String closingNotes;
}