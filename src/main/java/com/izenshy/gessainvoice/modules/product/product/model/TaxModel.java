package com.izenshy.gessainvoice.modules.product.product.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="taxes")
@NamedQuery(name = "TaxModel.findAll", query = "SELECT taxes FROM TaxModel taxes")
@Data
public class TaxModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tax_id")
    private Long id;

    @Column(name = "tax_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID taxUuid;

    @PrePersist
    public void prePersist() {
        if (taxUuid == null) {
            taxUuid = UUID.randomUUID();
        }
    }
    @Column(name = "tax_code", unique = true)
    private String taxCode;

    @Column(name = "tax_percentage")
    private String taxPercentage;

    @Column(name = "code_sri")
    private Integer codeSri;

    @Column(name = "tax_value")
    private Float taxValue;

    @CreationTimestamp
    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;
}
