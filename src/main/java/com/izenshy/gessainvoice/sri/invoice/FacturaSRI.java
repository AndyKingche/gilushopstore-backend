package com.izenshy.gessainvoice.sri.invoice;

import com.izenshy.gessainvoice.sri.dto.collection.DetallesDTO;
import com.izenshy.gessainvoice.sri.dto.single.InfoTributariaDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.*;
import lombok.Data;


import java.io.Serializable;
@XmlRootElement(name = "factura")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Schema(name = "FacturaSRI", description = "Representa la estructura de una factura para el SRI")
public class FacturaSRI implements Serializable {
    @XmlAttribute(name = "id")
    private String id = "comprobante";

    @XmlAttribute(name = "version")
    private String version = "2.0.0";

    @XmlElement(name = "infoTributaria")
    private InfoTributariaDTO infoTributaria;

    @XmlElement(name = "infoFactura")
    private InfoFacturaDTO infoFactura;

    @XmlElement(name = "detalles")
    private DetallesDTO detalles;
}
