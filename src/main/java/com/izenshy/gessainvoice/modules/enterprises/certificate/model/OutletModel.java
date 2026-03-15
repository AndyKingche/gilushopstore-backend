package com.izenshy.gessainvoice.modules.enterprises.certificate.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table( name = "outlets" )
@NamedQuery(name = "OutletModel.findAll", query = "SELECT outlets FROM OutletModel outlets")
@Data
public class  OutletModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="outlet_id")
    private Long outletId;

    @Column(name = "outlet_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID outletUuid;

    @PrePersist
    public void prePersist() {
        if (outletUuid == null) {
            outletUuid = UUID.randomUUID();
        }
    }

    @Column(name="outlet_city")
    private String outletCity;

    @Column(name="outlet_address")
    private String outletAddress;

    @Column(name="outlet_name")
    private String outletName;

    @Column(name="outlet_telf")
    private String outletTelf;

    @Column(name="outlet_status")
    private Boolean outletStatus;

    @ManyToOne
    @JoinColumn(name = "enterprise_id")
    public EnterpriseModel enterpriseId;

    @CreationTimestamp
    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @CreationTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

}
