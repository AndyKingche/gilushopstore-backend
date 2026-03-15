package com.izenshy.gessainvoice.modules.person.client.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClientResponseWebService implements Serializable {
    public String nombreCompleto;
    public String identificacion;
    public String tipoIdentificacion;
}
