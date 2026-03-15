package com.izenshy.gessainvoice.modules.invoice.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PdfFormaPagoDTO implements Serializable {
    private String formaPago;
    private String valorpago;
}
