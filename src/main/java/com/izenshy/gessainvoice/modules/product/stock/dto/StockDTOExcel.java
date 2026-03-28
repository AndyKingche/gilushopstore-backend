package com.izenshy.gessainvoice.modules.product.stock.dto;

import lombok.Data;

@Data
public class StockDTOExcel {

    private String productName;
    private String productCode;
    private String categoryName;
    private String detailName;
    private double stockQuantity;
    private Boolean stockAvalible;
    private double unitPrice;
    private double pvpPrice;
    private int stockMax;
    private int stockMin;
    private Boolean applyTax;
    private String taxCode;
}
