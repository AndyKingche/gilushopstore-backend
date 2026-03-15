package com.izenshy.gessainvoice.modules.email.model;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_config")
@NamedQuery(name = "EmailConfigModel.findAll", query = "SELECT i FROM EmailConfigModel i")
@Data
public class EmailConfigModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_id")
    private Long id;

    @Column(name = "email_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
    private UUID emailUuid;

    @PrePersist
    public void prePersist() {
        if (emailUuid == null) {
            emailUuid = UUID.randomUUID();
        }
    }

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "smtp_host")
    private String smtpHost;

    @Column(name = "smtp_port")
    private String smtpPort;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "tlsEnabled")
    private boolean tlsEnabled;

    @Column(name = "sslEnabled")
    private boolean sslEnabled;

    @Column(name = "status_email")
    private boolean statusEmail;

    @Column(name = "subject_email")
    private String subjectEmail;

    @Column(name = "body_email")
    private String bodyEmail;

    @ManyToOne
    @JoinColumn(name = "enterprise_id")
    private EnterpriseModel enterpriseId;

    @CreationTimestamp
    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;
}
