package com.izenshy.gessainvoice.modules.enterprises.emitter.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmitterDTO{

    public Long id;
    public String emitterRazonSocial;
    public String emitterNombreComercial;
    public String emitterAmbiente;
    public String emitterRuc;
    public String emitterDirMatriz;
    public String emitterPtoEmision;
    public String emitterCodEstb;
    public Boolean emitterStatus;
    public Long enterpriseId;
}
