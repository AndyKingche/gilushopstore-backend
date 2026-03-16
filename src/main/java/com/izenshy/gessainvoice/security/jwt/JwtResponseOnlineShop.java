package com.izenshy.gessainvoice.security.jwt;

import lombok.Data;

import java.io.Serializable;

@Data
public class JwtResponseOnlineShop implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;
    private final String nombreCompleto;

    public JwtResponseOnlineShop(String jwttoken, String nombreCompleto) {
        this.jwttoken = jwttoken;
        this.nombreCompleto = nombreCompleto;

    }
}
