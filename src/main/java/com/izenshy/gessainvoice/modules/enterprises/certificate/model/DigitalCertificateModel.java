package com.izenshy.gessainvoice.modules.enterprises.certificate.model;

import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table( name = "digital_certs" )
@NamedQuery(name = "DigitalCertificateModel.findAll", query = "SELECT digital_certs FROM DigitalCertificateModel digital_certs")
@Data
public class DigitalCertificateModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "digital_cert_id")
    private Long id;

    @Column(name = "digital_cert_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()",updatable = false, nullable = false)
    private UUID digCertUuid;

    @PrePersist
    public void prePersist() {
        if (digCertUuid == null) {
            digCertUuid = UUID.randomUUID();
        }
    }
    
    @Column( name = "digital_cert_name" )
    private String digCertName;

    @Column( name = "digital_certificate",  columnDefinition = "bytea")
    private byte[] digCertificate;

    @Column(name = "digital_cert_password")
    private String digCertPassword;

    @Column(name = "digital_cert_status")
    private Boolean digCertStatus;

    @Column(name = "digital_cert_expiration_date")
    private LocalDate digCertExpirationDate;

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
