package com.izenshy.gessainvoice.sri.dto.single;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
@XmlRootElement(name = "infoTributaria")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class InfoTributariaDTO implements Serializable {
    @XmlElement(name = "ambiente")
    private String ambiente;

    @XmlElement(name = "tipoEmision")
    private String tipoEmision = "1";

    @XmlElement(name = "razonSocial")
    private String razonSocial;

    @XmlElement(name = "nombreComercial")
    private String nombreComercial;

    @XmlElement(name = "ruc")
    private String ruc;

    @XmlElement(name = "claveAcceso")
    private String claveAcceso;

    @XmlElement(name = "codDoc")
    private String codDoc;

    @XmlElement(name = "estab")
    private String estab;

    @XmlElement(name = "ptoEmi")
    private String ptoEmi;

    @XmlElement(name = "secuencial")
    private String secuencial;

    @XmlElement(name = "dirMatriz")
    private String dirMatriz;
}
