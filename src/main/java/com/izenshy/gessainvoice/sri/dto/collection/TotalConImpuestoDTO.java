package com.izenshy.gessainvoice.sri.dto.collection;

import com.izenshy.gessainvoice.sri.dto.single.TotalImpuestoDTO;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
@XmlRootElement(name = "totalConImpuestos")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class TotalConImpuestoDTO implements Serializable {
    @XmlElement(name = "totalImpuesto")
    private List<TotalImpuestoDTO> totalImpuesto;
}
