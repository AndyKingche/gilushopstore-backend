package com.izenshy.gessainvoice.modules.product.product.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="categories")
@NamedQuery(name = "CategoryModel.findAll", query = "SELECT categories FROM CategoryModel categories")
@Data
public class CategoryModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID categoryUuid;

    @PrePersist
    public void prePersist() {
        if (categoryUuid == null) {
            categoryUuid = UUID.randomUUID();
        }
    }

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "category_description")
    private String categoryDesc;

    @CreationTimestamp
    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;
}
