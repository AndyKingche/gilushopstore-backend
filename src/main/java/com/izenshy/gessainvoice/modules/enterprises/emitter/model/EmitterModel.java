package com.izenshy.gessainvoice.modules.enterprises.emitter.model;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table( name = "emitters" )
@NamedQuery(name = "EmitterModel.findAll", query = "SELECT emitters FROM EmitterModel emitters")
@Data
public class EmitterModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emitter_id")
    private Long id;

    @Column(name = "emitter_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID emitterUuid;

    @PrePersist
    public void prePersist() {
        if (emitterUuid == null) {
            emitterUuid = UUID.randomUUID();
        }
    }

    @Column(name = "ambiente")
    private String emitterAmbiente;

    @Column(name = "razon_social")
    private String emitterRazonSocial;

    @Column(name = "nombre_comercial")
    private String emitterNombreComercial;

    @Column(name = "ruc")
    private String emitterRuc;

    @Column(name = "dir_matriz")
    private String emitterDirMatriz;

    @Column(name = "cod_estab")
    private String emitterCodEstb;

    @Column(name = "pto_emision")
    private String emitterPtoEmision;

    @Column(name = "emitter_status")
    private Boolean emitterStatus;

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
