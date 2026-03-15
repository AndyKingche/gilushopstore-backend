package com.izenshy.gessainvoice.sri.dto.collection;

import com.izenshy.gessainvoice.sri.dto.single.DetalleDTO;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
@XmlRootElement(name = "detalles")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class DetallesDTO implements Serializable {
    @XmlElement(name = "detalle")
    private List<DetalleDTO> detalle;
}
