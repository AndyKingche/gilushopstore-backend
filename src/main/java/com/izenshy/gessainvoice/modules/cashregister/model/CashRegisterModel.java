package com.izenshy.gessainvoice.modules.cashregister.model;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cash_register")
@NamedQuery(name = "CashRegisterModel.findAll", query = "SELECT cr FROM CashRegisterModel cr")
@Data
public class CashRegisterModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cash_register_id")
    private Long id;

    @Column(name = "cash_register_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID cashRegisterUuid;

    @PrePersist
    public void prePersist() {
        if (cashRegisterUuid == null) {
            cashRegisterUuid = UUID.randomUUID();
        }
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel userId;

    @ManyToOne
    @JoinColumn(name = "outlet_id")
    private OutletModel outletId;

    @ManyToOne
    @JoinColumn(name = "enterprise_id")
    private EnterpriseModel enterpriseId;

    @Column(name = "opening_date")
    private LocalDateTime openingDate;

    @Column(name = "closing_date")
    private LocalDateTime closingDate;

    @Column(name = "opening_cash", precision = 12, scale = 2)
    private BigDecimal openingCash;

    @Column(name = "opening_transfer", precision = 12, scale = 2)
    private BigDecimal openingTransfer;

    @Column(name = "opening_total", precision = 12, scale = 2)
    private BigDecimal openingTotal;

    @Column(name = "closing_cash", precision = 12, scale = 2)
    private BigDecimal closingCash;

    @Column(name = "closing_transfer", precision = 12, scale = 2)
    private BigDecimal closingTransfer;

    @Column(name = "closing_total", precision = 12, scale = 2)
    private BigDecimal closingTotal;

    @Column(name = "total_sales_cash", precision = 12, scale = 2)
    private BigDecimal totalSalesCash = BigDecimal.ZERO;

    @Column(name = "total_sales_transfer", precision = 12, scale = 2)
    private BigDecimal totalSalesTransfer = BigDecimal.ZERO;

    @Column(name = "total_expenses", precision = 12, scale = 2)
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    @Column(name = "total_investments", precision = 12, scale = 2)
    private BigDecimal totalInvestments = BigDecimal.ZERO;

    @Column(name = "cash_difference", precision = 12, scale = 2)
    private BigDecimal cashDifference;

    @Column(name = "transfer_difference", precision = 12, scale = 2)
    private BigDecimal transferDifference;

    @Column(name = "status")
    private String status;

    @Column(name = "opening_notes")
    private String openingNotes;

    @Column(name = "closing_notes")
    private String closingNotes;

    @CreationTimestamp
    @Column(name = "date_created", updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;
}