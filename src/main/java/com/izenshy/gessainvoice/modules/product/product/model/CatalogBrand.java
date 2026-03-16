package com.izenshy.gessainvoice.modules.product.product.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="catalog_brand")
@NamedQuery(name = "CatalogBrand.findAll", query = "SELECT c FROM CatalogBrand c")
@Data
public class CatalogBrand implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Long id;

    @Column(name = "brand_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID brandUuid;

    @PrePersist
    public void prePersist() {
        if (brandUuid == null) {
            brandUuid = UUID.randomUUID();
        }
    }

    @Column(name = "brand_name", unique = true)
    private String brandName;

    @Column(name = "brand_description")
    private String brandDescription;

    @Column(name = "brand_logo_url")
    private String brandLogoUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}