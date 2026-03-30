package com.izenshy.gessainvoice.modules.invoice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class InvoiceHeaderDTO {
    private Long id;
    private UUID invoiceUuid;
    private String enterpriseName;
    private String establishmentAddress;
    private String rucEnterprise;
    private String establishment;
    private String remissionGuide;
    private String sequential;
    private String accessKey;
    private String fechaAutorizacion;
    private String clientFullName;
    private String clientRuc;
    private LocalDate invoiceDate;
    private String clientAddress;
    private BigDecimal invoiceSubtotal;
    private BigDecimal invoiceDiscount;
    private BigDecimal invoiceTotal;

    //private String invoiceStatus;
    // private BigDecimal invoiceTax;
    // private String paymentType;
    // private String issuePoint;
    // private String invoiceType;
    // private String userName;
    private List<InvoiceHeaderDetailDTO> detalles;
    // private LocalDateTime dateUpdated;
}
