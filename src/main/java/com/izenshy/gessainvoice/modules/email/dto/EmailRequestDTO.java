package com.izenshy.gessainvoice.modules.email.dto;

import lombok.Data;

@Data
public class EmailRequestDTO {
    private String to;
    private String subject;
    private String body;
    private String pdfBase64;
}