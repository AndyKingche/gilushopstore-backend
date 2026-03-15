package com.izenshy.gessainvoice.modules.product.reports.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "view_productos_mas_vendidos_dia")
@Data
public class BestSellingProductsDailyModel {
    @Id
    @Column(name = "dia")
    private LocalDate dia;

    @Column(name = "enterprise_id")
    private Long enterpriseId;

    @Column(name = "outlet_id")
    private Long outletId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "detail_name")
    private String detailName;

    @Column(name = "cantidad_vendida_dia")
    private BigDecimal cantidadVendidaDia;

    @Column(name = "total_vendido_dia_usd")
    private BigDecimal totalVendidoDiaUsd;
}