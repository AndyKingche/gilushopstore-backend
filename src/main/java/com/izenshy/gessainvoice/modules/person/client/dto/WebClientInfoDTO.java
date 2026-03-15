package com.izenshy.gessainvoice.modules.person.client.dto;

import lombok.Data;

@Data
public class WebClientInfoDTO {
    private String identificacion;
    private String denominacion;
    private String tipo;
    private String clase;
    private String tipoIdentificacion;
    private String resolucion;
    private String nombreComercial;
    private String direccionMatriz;
    private Long fechaInformacion;
    private String mensaje;
    private String estado;
}
