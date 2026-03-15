package com.izenshy.gessainvoice.modules.person.client.model;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clients")
@NamedQuery(name = "ClientModel.findAll", query = "SELECT clients FROM ClientModel clients")
@Data
public class ClientModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Long id;

    @Column(name = "client_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID clientUuid;

    @PrePersist
    public void prePersist() {
        if (clientUuid == null) {
            clientUuid = UUID.randomUUID();
        }
    }

    @Column(name = "client_fullname")
    private String clientFullName;

    @Column(name = "client_address")
    private String clientAddress;

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "client_cellphone")
    private String clientCellphone;

    @Column(name = "client_typeid")
    private String clientTypeIdentification;

    @Column(name = "client_ci")
    private String clientIdentification;

    @Column(name = "client_ruc")
    private String clientRuc;

    @Column(name = "client_gender")
    private String clientGender;

    @Column(name = "client_status")
    private Boolean clientStatus;

    @ManyToOne
    @JoinColumn(name = "enterprise_id")
    public EnterpriseModel enterpriseId;

    @CreationTimestamp
    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;
}
