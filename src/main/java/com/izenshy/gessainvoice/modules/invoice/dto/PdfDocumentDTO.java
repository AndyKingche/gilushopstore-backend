package com.izenshy.gessainvoice.modules.invoice.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PdfDocumentDTO implements Serializable {
    private String nombrePrincipal;
    private String nombreEmpresa;
    private String direccionMatriz;
    private String direccionSucursal;
    private String contabilidad;
    private String ruc;
    private String nfactura;
    private String nautorizacion;
    private String fechaautorizacion;
    private String ambiente;
    private String emision;
    private String nombreApellidos;
    private String identificacion;
    private String fecha;
    private String direccion;
    private List<PdfDetalleDTO> datosFactura;
    private List<PdfSubDetallesDTO> detalleDTOS;
    private List<PdfFormaPagoDTO> formaPagoDTOS;

}
