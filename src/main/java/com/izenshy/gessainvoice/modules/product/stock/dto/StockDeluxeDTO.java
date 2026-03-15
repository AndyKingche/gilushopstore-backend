package com.izenshy.gessainvoice.modules.product.stock.dto;

import lombok.Data;

@Data
public class StockDeluxeDTO {
    private Long productId;
    private String productName;
    private String productCode;
    private String productDesc;
    private String categoryName;
    private String detailName;
    private Long outletId;
    private float stockQuantity;
    private Boolean stockAvalible;
    private float unitPrice;
    private float pvpPrice;
    private int stockMax;
    private int stockMin;
    private Boolean applyTax;
    private String taxCode;
}
