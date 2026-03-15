package com.izenshy.gessainvoice.modules.product.product.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String productName;
    private String productCode;
    private String productDesc;
    private Long categoryId;
    private Long detailId;
}
