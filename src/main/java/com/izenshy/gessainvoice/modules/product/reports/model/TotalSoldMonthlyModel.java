package com.izenshy.gessainvoice.modules.product.reports.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "view_total_vendido_mes")
@Data
public class TotalSoldMonthlyModel {
    @Id
    @Column(name = "mes")
    private LocalDate mes;

    @Column(name = "enterprise_id")
    private Long enterpriseId;

    @Column(name = "outlet_id")
    private Long outletId;

    @Column(name = "total_vendido_mes")
    private BigDecimal totalVendidoMes;
}