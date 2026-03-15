package com.izenshy.gessainvoice.modules.invoice.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class InvoiceDetailRequestDTO implements Serializable {

    private Long id;
    private UUID detailUuid;
    private Integer quantity;
    private String description;
    private BigDecimal totalValue;
    private BigDecimal totalValueWithoutTax;
    private BigDecimal unitValue;
    private BigDecimal unitValueWithoutTax;
    private BigDecimal productTax;
    private Long stockProductId;
    private Long stockOutletId;
    private Long invoiceId;
}