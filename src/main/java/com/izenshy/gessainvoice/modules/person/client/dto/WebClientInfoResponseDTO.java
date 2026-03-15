package com.izenshy.gessainvoice.modules.person.client.dto;

import lombok.Data;

@Data
public class WebClientInfoResponseDTO {
    private WebClientInfoDTO contribuyente;
    private Object deuda;
    private Object impugnacion;
    private Object remision;
}
