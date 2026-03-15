package com.izenshy.gessainvoice.modules.invoice.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class InvoiceTempAuthorizationResponseDTO implements Serializable {

    private Long id;

    private UUID tempUuid;

    private String fileBase64;

    private String accessCode;

    private String receptionStatus;

    private String authorizationStatus;

    private Long enterpriseId;

    private Long outletId;

    private Long invoiceId;

    private LocalDateTime dateCreated;

    private LocalDateTime dateUpdated;
}
