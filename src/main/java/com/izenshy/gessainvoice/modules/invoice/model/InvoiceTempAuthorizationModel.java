package com.izenshy.gessainvoice.modules.invoice.model;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice_temp_authorization")
@NamedQuery(name = "InvoiceTempAuthorizationModel.findAll", query = "SELECT i FROM InvoiceTempAuthorizationModel i")
@Data
public class InvoiceTempAuthorizationModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temp_id")
    private Long id;

    @Column(name = "temp_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
    private UUID tempUuid;

    @PrePersist
    public void prePersist() {
        if (tempUuid == null) {
            tempUuid = UUID.randomUUID();
        }
    }

    @Column(name = "file_base64", columnDefinition = "TEXT")
    private String fileBase64;

    @Column(name = "access_code", length = 100)
    private String accessCode;

    @Column(name = "reception_status", length = 50)
    private String receptionStatus;

    @Column(name = "authorization_status", length = 50)
    private String authorizationStatus;

    @ManyToOne
    @JoinColumn(name = "enterprise_id", foreignKey = @ForeignKey(name = "fk_temp_auth_enterprise"))
    private EnterpriseModel enterpriseId;

    @ManyToOne
    @JoinColumn(name = "outlet_id", foreignKey = @ForeignKey(name = "fk_temp_auth_outlet"))
    private OutletModel outletId;

    @ManyToOne
    @JoinColumn(name = "invoice_id", foreignKey = @ForeignKey(name = "fk_temp_auth_invoice"))
    private InvoiceModel invoiceId;

    @Column(name = "date_created", updatable = false)
    @CreationTimestamp
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    @UpdateTimestamp
    private LocalDateTime dateUpdated;
}
