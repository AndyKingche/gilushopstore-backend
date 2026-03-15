package com.izenshy.gessainvoice.modules.product.product.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="products")
@NamedQuery(name = "ProductModel.findAll", query = "SELECT products FROM ProductModel products")
@Data
public class ProductModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "product_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID productUuid;

    @PrePersist
    public void prePersist() {
        if (productUuid == null) {
            productUuid = UUID.randomUUID();
        }
    }

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_code", unique = true)
    private String productCode;

    @Column(name = "product_description")
    private String productDesc;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "category_id")
    private CategoryModel categoryId;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "detail_id")
    private DetailModel detailId;

    @CreationTimestamp
    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

}
