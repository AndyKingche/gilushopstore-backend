package com.izenshy.gessainvoice.modules.invoice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class InvoiceHeaderDetailDTO {
    private Long id;
    private String descripcion;
    private Integer cantidad;
    private BigDecimal totalValue;
    private BigDecimal precioUnitario;
    private BigDecimal precioTotalSinImpuestoalueWithoutTax;
    // private BigDecimal unitValueWithoutTax;

    // private BigDecimal productTax;
    // private Long stockProductId;
    // private Long stockOutletId;
    // private Long invoiceId;
}
