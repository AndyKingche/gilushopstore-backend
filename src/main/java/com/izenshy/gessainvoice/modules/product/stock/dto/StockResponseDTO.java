package com.izenshy.gessainvoice.modules.product.stock.dto;

import lombok.Data;

@Data
public class StockResponseDTO {
    private Long productId;
    private Long outletId;
    private String nameProduct;
    private float stockQuantity;
    private Boolean stockAvalible;
    private float unitPrice;
    private float pvpPrice;
    private int stockMax;
    private int stockMin;
    private Boolean applyTax;
    private Long ivaId;
    private Float taxValue;
    private Integer codeSri;
}
