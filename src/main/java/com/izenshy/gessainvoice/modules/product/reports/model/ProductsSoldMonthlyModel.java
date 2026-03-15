package com.izenshy.gessainvoice.modules.product.reports.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "view_productos_vendidos_mes")
@Data
public class ProductsSoldMonthlyModel {
    @Id
    @Column(name = "mes")
    private LocalDate mes;

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

    @Column(name = "cantidad_vendida_mes")
    private BigDecimal cantidadVendidaMes;

    @Column(name = "total_vendido_mes_usd")
    private BigDecimal totalVendidoMesUsd;
}