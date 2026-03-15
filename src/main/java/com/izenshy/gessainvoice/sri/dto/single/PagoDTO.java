package com.izenshy.gessainvoice.sri.dto.single;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
@XmlRootElement(name = "pago")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class PagoDTO implements Serializable {
    @XmlElement(name = "formaPago")
    private String formaPago;

    @XmlElement(name = "total")
    private BigDecimal total;

    @XmlElement(name = "plazo")
    private String plazo;

    @XmlElement(name = "unidadTiempo")
    private String unidadTiempo;
}
