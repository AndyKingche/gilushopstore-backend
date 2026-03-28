package com.izenshy.gessainvoice.modules.product.stock.dto;

import lombok.Data;

@Data
public class StockResponseDTO {
    private Long productId;
    private Long outletId;
    private String nameProduct;
    private double stockQuantity;
    private Boolean stockAvalible;
    private double unitPrice;
    private double pvpPrice;
    private int stockMax;
    private int stockMin;
    private Boolean applyTax;
    private Long ivaId;
    private Float taxValue;
    private Integer codeSri;
}
