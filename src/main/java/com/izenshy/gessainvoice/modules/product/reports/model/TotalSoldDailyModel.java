package com.izenshy.gessainvoice.modules.product.reports.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "view_total_vendido_dia")
@Data
public class TotalSoldDailyModel {
    @Id
    @Column(name = "dia")
    private LocalDate dia;

    @Column(name = "enterprise_id")
    private Long enterpriseId;

    @Column(name = "outlet_id")
    private Long outletId;

    @Column(name = "total_vendido_dia")
    private BigDecimal totalVendidoDia;
}