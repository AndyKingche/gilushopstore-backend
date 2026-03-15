package com.izenshy.gessainvoice.sri.dto.collection;

import com.izenshy.gessainvoice.sri.dto.single.ImpuestoDTO;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
@XmlRootElement(name = "impuestos")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class ImpuestosDTO implements Serializable {
    @XmlElement(name = "impuesto")
    private ImpuestoDTO impuesto;
}
