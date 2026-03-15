package com.izenshy.gessainvoice.modules.invoice.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class InvoiceTempAuthorizationRequestDTO implements Serializable {

    private String fileBase64;

    private String accessCode;

    private String receptionStatus;

    private String authorizationStatus;

    private Long enterpriseId;

    private Long outletId;

    private Long invoiceId;
}
