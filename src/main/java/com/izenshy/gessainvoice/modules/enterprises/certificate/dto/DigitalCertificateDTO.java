package com.izenshy.gessainvoice.modules.enterprises.certificate.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class DigitalCertificateDTO {
    public Long id;
    public String digCertName;
    public String digCertPassword;
    public LocalDate digCertExpirationDate;
    public Boolean digCertStatus = true;
    public Long enterpriseId;
}
