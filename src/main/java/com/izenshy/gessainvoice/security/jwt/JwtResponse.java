package com.izenshy.gessainvoice.security.jwt;

import lombok.Data;

import java.io.Serializable;

@Data
public class JwtResponse implements Serializable {
    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;
    private final String userRol;
    private final Long enterpriseId;
    private final String nombreCompleto;
    private final String genero;
    private final Long userId;

    public JwtResponse(String jwttoken, String userRol, Long enterpriseId, String nombreCompleto, String genero, Long userId) {
        this.jwttoken = jwttoken;
        this.userRol = userRol;
        this.enterpriseId = enterpriseId;
        this.nombreCompleto = nombreCompleto;
        this.genero = genero;
        this.userId = userId;
    }

}
