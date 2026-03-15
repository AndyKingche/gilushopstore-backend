package com.izenshy.gessainvoice.modules.person.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class ContribuyenteDTO {
    private String numeroRuc;
    private String razonSocial;
    private String estadoContribuyenteRuc;
    private String actividadEconomicaPrincipal;
    private String tipoContribuyente;
    private String regimen;
    private String categoria;
    private String obligadoLlevarContabilidad;
    private String agenteRetencion;
    private String contribuyenteEspecial;
    private InformacionFechasContribuyenteDTO informacionFechasContribuyente;
    private List<RepresentanteLegalDTO> representantesLegales = null;
    private String motivoCancelacionSuspension;
    private String contribuyenteFantasma;
    private String transaccionesInexistente;

    @Data
    public static class InformacionFechasContribuyenteDTO {
        private String fechaInicioActividades;
        private String fechaCese;
        private String fechaReinicioActividades;
        private String fechaActualizacion;
    }

    @Data
    public static class RepresentanteLegalDTO {
        private String nombre;
        private String identificacion;
        private String cargo;
    }
}
