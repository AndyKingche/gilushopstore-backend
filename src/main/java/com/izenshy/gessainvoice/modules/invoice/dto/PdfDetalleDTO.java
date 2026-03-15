package com.izenshy.gessainvoice.modules.invoice.dto;

import com.izenshy.gessainvoice.sri.dto.collection.ImpuestosDTO;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
@Data
public class PdfDetalleDTO implements Serializable {
        private String codPrincipal;
        private String cantidad;
        private String precioUnit;
        private String descripcion;
        private String subsidio;
        private String precioTotal;
        private String descuento;
        private String preciosinSubsidio;
}
