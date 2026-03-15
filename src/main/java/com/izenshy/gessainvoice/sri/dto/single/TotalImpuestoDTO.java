package com.izenshy.gessainvoice.sri.dto.single;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
@XmlRootElement(name = "totalImpuesto")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class TotalImpuestoDTO implements Serializable {
    @XmlElement(name = "codigo")
    private String codigo;

    @XmlElement(name = "codigoPorcentaje")
    private String codigoPorcentaje;

    @XmlElement(name = "baseImponible")
    private BigDecimal baseImponible;

    @XmlElement(name = "valor")
    private BigDecimal valor;
}
