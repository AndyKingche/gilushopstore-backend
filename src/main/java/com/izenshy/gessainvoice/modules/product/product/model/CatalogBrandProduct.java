package com.izenshy.gessainvoice.modules.product.product.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="catalog_brand_product")
@NamedQuery(name = "CatalogBrandProduct.findAll", query = "SELECT c FROM CatalogBrandProduct c")
@Data
public class CatalogBrandProduct implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_product_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private CatalogBrand brand;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductModel product;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}