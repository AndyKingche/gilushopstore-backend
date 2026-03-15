package com.izenshy.gessainvoice.modules.person.user.model;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Entity
@Table( name = "users" )
@NamedQuery(name = "UserModel.findAll", query = "SELECT users FROM UserModel users")
@Data
public class UserModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID userUuid;

    @PrePersist
    public void prePersist() {
        if (userUuid == null) {
            userUuid = UUID.randomUUID();
        }
    }

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_firstname")
    private String userFirstname;

    @Column(name = "user_lastname")
    private String userLastname;

    @Column(name = "user_password")
    private String userPassword;

    @Column(name = "user_gender")
    private String userGender;

    @Column(name = "user_ci")
    private String userIdentification;

    @Column(name = "user_ruc")
    private String userRuc;

    @Column(name = "user_rol")
    private String userRol;

    @Column(name = "user_status")
    private Boolean userStatus;

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
