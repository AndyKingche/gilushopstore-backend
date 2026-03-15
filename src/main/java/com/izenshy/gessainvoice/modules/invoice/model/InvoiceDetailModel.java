package com.izenshy.gessainvoice.modules.invoice.model;

import com.izenshy.gessainvoice.modules.product.stock.model.StockModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice_detail")
@NamedQuery(name = "InvoiceDetailModel.findAll", query = "SELECT d FROM InvoiceDetailModel d")
@Data
public class InvoiceDetailModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long id;

    @Column(name = "detail_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
    private UUID detailUuid;

    @PrePersist
    public void prePersist() {
        if (detailUuid == null) {
            detailUuid = UUID.randomUUID();
        }
    }

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "total_value", precision = 12, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "total_value_without_tax", precision = 12, scale = 2)
    private BigDecimal totalValueWithoutTax;

    @Column(name = "unit_value", precision = 12, scale = 2)
    private BigDecimal unitValue;

    @Column(name = "unit_value_without_tax", precision = 12, scale = 2)
    private BigDecimal unitValueWithoutTax;

    @Column(name = "product_tax", precision = 10, scale = 2)
    private BigDecimal productTax;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "stock_product_id", referencedColumnName = "stock_product_id"),
            @JoinColumn(name = "stock_outlet_id", referencedColumnName = "stock_outlet_id")
    })
    private StockModel stock;

    @ManyToOne
    @JoinColumn(name = "invoice_id", foreignKey = @ForeignKey(name = "fk_detail_invoice"))
    private InvoiceModel invoice;

    @CreationTimestamp
    @Column(name = "date_created", updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;
}
