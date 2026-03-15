package com.izenshy.gessainvoice.modules.person.client.dto;

import lombok.Data;
import java.io.Serializable;


@Data
public class ClientRequestDTO implements Serializable {

    public String clientFullName;
    public String clientAddress;
    public String clientEmail;
    public String clientCellphone;
    public String clientTypeIdentification;
    public String clientIdentification;
    public String clientGender;
    public Long enterpriseId;
    public Boolean clientStatus = true;

}
