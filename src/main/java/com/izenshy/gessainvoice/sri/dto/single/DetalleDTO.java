package com.izenshy.gessainvoice.sri.dto.single;

import com.izenshy.gessainvoice.sri.dto.collection.ImpuestosDTO;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;

@XmlRootElement(name = "detalle")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class DetalleDTO implements Serializable {
    @XmlElement(name = "codigoPrincipal")
    private String codigoPrincipal;

    @XmlElement(name = "descripcion")
    private String descripcion;

    @XmlElement(name = "cantidad")
    private BigDecimal cantidad;

    @XmlElement(name = "precioUnitario")
    private BigDecimal precioUnitario;

    @XmlElement(name = "descuento")
    private BigDecimal descuento;

    @XmlElement(name = "precioTotalSinImpuesto")
    private BigDecimal precioTotalSinImpuesto;

    @XmlElement(name = "impuestos")
    private ImpuestosDTO impuestos;
}
