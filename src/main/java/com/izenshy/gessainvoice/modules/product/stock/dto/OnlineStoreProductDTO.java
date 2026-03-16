package com.izenshy.gessainvoice.modules.product.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineStoreProductDTO {
    private String id;
    private String name;
    private String category;
    private String brand;
    private double price;
    private String description;
    private String image;
    private boolean inStock;
}