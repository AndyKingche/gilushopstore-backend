package com.izenshy.gessainvoice.modules.person.user.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDTO {
    private Long id;
    private String userName;
    private String userFirstname;
    private String userLastname;
    private String userPassword;
    private String userGender;
    private String userIdentification;
    private String userRuc;
    private String userRol;
    private Boolean userStatus;
    private Long enterpriseId;
}
