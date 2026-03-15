package com.izenshy.gessainvoice.modules.enterprises.certificate.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table( name = "enterprises" )
@NamedQuery(name = "EnterpriseModel.findAll", query = "SELECT enterprises FROM EnterpriseModel enterprises")
@Data
public class EnterpriseModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enterprise_id")
    private Long id;

    @Column(name = "enterprise_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID enterpriseUuid;

    @PrePersist
    public void prePersist() {
        if (enterpriseUuid == null) {
            enterpriseUuid = UUID.randomUUID();
        }
    }

    @Column(name = "enterprise_name")
    private String enterpriseName;

    @Column(name = "enterprise_owner_name")
    private String enterpriseOwnerName;

    @Column(name = "enterprise_owner_identification")
    private String enterpriseIdentification;

    @Column(name = "enterprise_status")
    private Boolean enterpriseStatus;

    @CreationTimestamp
    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;
}
