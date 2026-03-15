package com.izenshy.gessainvoice.modules.cashregister.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CashTransactionRequestDTO implements Serializable {
    private Long cashRegisterId;
    private Long invoiceId;
    private String transactionType;
    private String paymentMethod;
    private BigDecimal amountCash;
    private BigDecimal amountTransfer;
    private String description;
    private String referenceNumber;
    private String recipient;
    private Long userId;
}