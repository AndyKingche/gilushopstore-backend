package com.izenshy.gessainvoice.sri.invoice;

import com.izenshy.gessainvoice.sri.dto.collection.DetallesDTO;
import lombok.Data;

@Data
public class FacturaSRIDTO {

    public String rucEmpresa;
    public InfoFacturaDTO infoFactura;
    public DetallesDTO detalles;
}
