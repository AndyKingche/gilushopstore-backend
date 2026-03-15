package com.izenshy.gessainvoice.modules.enterprises.certificate.dto;

import lombok.Data;

@Data
public class EnterpriseDTO {

    private Long id;
    private String enterpriseName;
    private String enterpriseOwnerName;
    private String enterpriseIdentification;
    private Boolean enterpriseStatus = true;
}
