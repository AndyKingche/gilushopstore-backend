package com.izenshy.gessainvoice.modules.product.stock.dto;

import lombok.Data;

@Data
public class StockDTO {
    private Long productId;
    private Long outletId;
    private float stockQuantity;
    private Boolean stockAvalible;
    private double unitPrice;
    private double pvpPrice;
    private int stockMax;
    private int stockMin;
    private Boolean applyTax;
    private Long ivaId;
}
