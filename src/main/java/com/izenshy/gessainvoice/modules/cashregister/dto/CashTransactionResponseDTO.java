package com.izenshy.gessainvoice.modules.cashregister.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CashTransactionResponseDTO implements Serializable {
    private Long id;
    private UUID transactionUuid;
    private Long cashRegisterId;
    private Long invoiceId;
    private String transactionType;
    private String paymentMethod;
    private BigDecimal amountCash;
    private BigDecimal amountTransfer;
    private BigDecimal totalAmount;
    private String description;
    private String referenceNumber;
    private String recipient;
    private Long userId;
    private LocalDateTime transactionDate;
}