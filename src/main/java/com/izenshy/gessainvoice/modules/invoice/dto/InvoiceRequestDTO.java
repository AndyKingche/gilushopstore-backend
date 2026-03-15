package com.izenshy.gessainvoice.modules.invoice.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceRequestDTO implements Serializable {

    private String invoiceStatus;
    private BigDecimal invoiceTax;
    private LocalDate invoiceDate;
    private BigDecimal invoiceTotal;
    private BigDecimal invoiceSubtotal;
    private BigDecimal invoiceDiscount;
    private String paymentType;
    private String sequential;
    private String remissionGuide;
    private String accessKey;
    private String issuePoint;
    private String establishment;
    private String invoiceType;
    private Long userId;
    private Long clientId;
    private Long enterpriseId;
    private List<InvoiceDetailRequestDTO> details;
}