package com.izenshy.gessainvoice.modules.person.client.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izenshy.gessainvoice.common.response.GessaApiResponse;
import com.izenshy.gessainvoice.modules.person.client.dto.ClientResponseWebService;
import com.izenshy.gessainvoice.modules.person.client.dto.ContribuyenteDTO;
import com.izenshy.gessainvoice.modules.person.client.dto.WebClientInfoResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@Service
public class WebClientService {
    private final RestTemplate restTemplate;


    public WebClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GessaApiResponse<ClientResponseWebService> obtenerInfoPorIdentificacion(String ci) throws JsonProcessingException {

        String url = String.format(
                "https://srienlinea.sri.gob.ec/movil-servicios/api/v1.0/deudas/porIdentificacion/%s/?tipoPersona=N&_=%s",
                ci, ci
        );

        try{
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String json = response.getBody();

            ObjectMapper mapper = new ObjectMapper();
            WebClientInfoResponseDTO dto = mapper.readValue(json, WebClientInfoResponseDTO.class);

            ClientResponseWebService clientFound = new ClientResponseWebService();
            clientFound.setNombreCompleto(dto.getContribuyente().getNombreComercial());
            clientFound.setIdentificacion(dto.getContribuyente().getIdentificacion());
            clientFound.setTipoIdentificacion(dto.getContribuyente().getTipoIdentificacion());

            return new GessaApiResponse<>(true, "Respuesta Existosa, cliente encontrado", clientFound);

        } catch (HttpClientErrorException ex) {
            return new GessaApiResponse<>(false, "Error insesperado, o la identificacion no es la correcta, HTTP STATUS : "+ex.getStatusCode(), null);

        }
        catch (Exception e) {
            return new GessaApiResponse<>(false, "Error insesperado, o la identificacion no es la correcta : "+e.getMessage(), null);
        }
    }

    public GessaApiResponse<ClientResponseWebService> obtenerInfoPorRuc(String ruc) throws JsonProcessingException {

        String url = "https://srienlinea.sri.gob.ec/sri-catastro-sujeto-servicio-internet/rest/ConsolidadoContribuyente/obtenerPorNumerosRuc?&ruc=" + ruc;

        try{
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String json = response.getBody();

            ObjectMapper mapper = new ObjectMapper();

            ContribuyenteDTO[] dtos = mapper.readValue(json, ContribuyenteDTO[].class);



            if (dtos.length > 0) {
                ClientResponseWebService clientFound = new ClientResponseWebService();
                clientFound.setNombreCompleto(dtos[0].getRazonSocial());
                clientFound.setIdentificacion(dtos[0].getNumeroRuc());
                clientFound.setTipoIdentificacion("R");
                return new GessaApiResponse<>(true, "Respuesta Exitosa, contribuyente encontrado", clientFound);
            } else {
                return new GessaApiResponse<>(false, "No se encontró información para el RUC proporcionado", null);
            }

        } catch (HttpClientErrorException ex) {
            return new GessaApiResponse<>(false, "Error inesperado, o el RUC no es el correcto, HTTP STATUS : "+ex.getStatusCode(), null);

        }
        catch (Exception e) {
            return new GessaApiResponse<>(false, "Error inesperado, o el RUC no es el correcto : "+e.getMessage(), null);
        }
    }



}
