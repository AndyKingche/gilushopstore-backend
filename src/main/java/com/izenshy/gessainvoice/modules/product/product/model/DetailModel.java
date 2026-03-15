package com.izenshy.gessainvoice.modules.product.product.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="details")
@NamedQuery(name = "DetailModel.findAll", query = "SELECT details FROM DetailModel details")
@Data
public class DetailModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long id;

    @Column(name = "detail_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID detailUuid;

    @PrePersist
    public void prePersist() {
        if (detailUuid == null) {
            detailUuid = UUID.randomUUID();
        }
    }

    @Column(name = "detail_name")
    private String detailName;

    @Column(name = "detail_description")
    private String detailDesc;

    @CreationTimestamp
    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;
}
