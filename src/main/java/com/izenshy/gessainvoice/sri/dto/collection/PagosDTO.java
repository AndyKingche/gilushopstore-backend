package com.izenshy.gessainvoice.sri.dto.collection;

import com.izenshy.gessainvoice.sri.dto.single.PagoDTO;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
@XmlRootElement(name = "pagos")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class PagosDTO implements Serializable {
    @XmlElement(name = "pago")
    private List<PagoDTO> pago;
}
