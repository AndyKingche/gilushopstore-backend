package com.izenshy.gessainvoice.modules.product.product.model;

import com.izenshy.gessainvoice.modules.product.stock.model.StockModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="image_stock")
@NamedQuery(name = "ImageStock.findAll", query = "SELECT i FROM ImageStock i")
@Data
public class ImageStock implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_stock_id")
    private Long id;

    @Column(name = "image_stock_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID imageStockUuid;

    @PrePersist
    public void prePersist() {
        if (imageStockUuid == null) {
            imageStockUuid = UUID.randomUUID();
        }
    }

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "stock_product_id", referencedColumnName = "stock_product_id"),
        @JoinColumn(name = "stock_outlet_id", referencedColumnName = "stock_outlet_id")
    })
    private StockModel stock;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_order")
    private Integer imageOrder;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}