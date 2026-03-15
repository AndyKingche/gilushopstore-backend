package com.izenshy.gessainvoice.modules.invoice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class InvoiceTempAuthorizationAuxResponseDTO {
    private Long id;

    private String accessCode;

    private String receptionStatus;

    private String authorizationStatus;

    private Long enterpriseId;

    private Long outletId;

    private Long invoiceId;
}
