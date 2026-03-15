package com.izenshy.gessainvoice.modules.product.reports.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "view_productos_en_minimo")
@Data
public class ProductsAtMinimumModel {
    @Id
    @Column(name = "outlet_id")
    private Long outletId;

    @Column(name = "outlet_name")
    private String outletName;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "detail_name")
    private String detailName;

    @Column(name = "stock_quantity")
    private BigDecimal stockQuantity;

    @Column(name = "stock_min")
    private Integer stockMin;
}