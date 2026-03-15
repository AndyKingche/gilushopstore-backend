package com.izenshy.gessainvoice.modules.invoice.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PdfSubDetallesDTO implements Serializable {
    private String subtotal15porciento;
    private String noobjiva;
    private String noextiva;
    private String sinimpuesto;
    private String valortotal;
    private String propina;
    private String irbpnr;
    private String iva15;
    private String ice;
    private String totaldesc;
    private String valorsinsubsidio;
}
