package com.izenshy.gessainvoice.modules.invoice.model;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.person.client.model.ClientModel;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoice_header")
@NamedQuery(name = "InvoiceModel.findAll", query = "SELECT i FROM InvoiceModel i")
@Data
public class InvoiceModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long id;

    @Column(name = "invoice_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
    private UUID invoiceUuid;

    @PrePersist
    public void prePersist() {
        if (invoiceUuid == null) {
            invoiceUuid = UUID.randomUUID();
        }
    }

    @Column(name = "invoice_status")
    private String invoiceStatus;

    @Column(name = "invoice_tax", precision = 10, scale = 2)
    private BigDecimal invoiceTax;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "invoice_total", precision = 12, scale = 2)
    private BigDecimal invoiceTotal;

    @Column(name = "invoice_subtotal", precision = 12, scale = 2)
    private BigDecimal invoiceSubtotal;

    @Column(name = "invoice_discount", precision = 12, scale = 2)
    private BigDecimal invoiceDiscount;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "sequential")
    private String sequential;

    @Column(name = "remission_guide")
    private String remissionGuide;

    @Column(name = "access_key")
    private String accessKey;

    @Column(name = "issue_point")
    private String issuePoint;

    @Column(name = "establishment")
    private String establishment;

    @Column(name = "invoice_type")
    private String invoiceType;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_invoice_user"))
    private UserModel userId;

    @ManyToOne
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "fk_invoice_customer"))
    private ClientModel clientId;

    @ManyToOne
    @JoinColumn(name = "enterprise_id")
    private EnterpriseModel enterpriseId;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceDetailModel> details;

    @CreationTimestamp
    @Column(name = "date_created", updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;
}
