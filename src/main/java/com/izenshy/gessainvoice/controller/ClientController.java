package com.izenshy.gessainvoice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.izenshy.gessainvoice.common.response.GessaApiResponse;
import com.izenshy.gessainvoice.modules.person.client.dto.*;
import com.izenshy.gessainvoice.modules.person.client.model.ClientModel;
import com.izenshy.gessainvoice.modules.person.client.service.ClientService;
import com.izenshy.gessainvoice.modules.person.client.service.impl.WebClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("api/v1/gessa/client")
@Tag(name = "Client", description = "Esta sección es dedicada a las operaciones relacionadas con los clientes")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class ClientController {
    private WebClientService webClientService;
    private ClientService clientService;

    @Autowired
    public ClientController(WebClientService webClientService, ClientService clientService){
        this.webClientService = webClientService;
        this.clientService = clientService;
    }


    @GetMapping("/find-web-client/{ci}")
    public GessaApiResponse<ClientResponseWebService> getWebClient(@PathVariable String ci) throws JsonProcessingException {
        return webClientService.obtenerInfoPorIdentificacion(ci);
    }

    @GetMapping("/find-web-client-sri/{ci}")
    public GessaApiResponse<ClientResponseWebService> getWebClientSRI(@PathVariable String ci) throws JsonProcessingException {
        return webClientService.obtenerInfoPorRuc(ci);
    }

    @PostMapping("create-client")
    public ClientModel saveClient(@RequestBody ClientRequestCreateDTO clientRequestDTO ){

        return clientService.saveClientDTO(clientRequestDTO);

    }

    @PutMapping("/update-client/{id}")
    public ResponseEntity<ClientModel> updateClient(@PathVariable Long id, @RequestBody ClientRequestDTO clientRequestDTO) {
        ClientModel updatedClient = clientService.updateClientDTO(id, clientRequestDTO);
        return ResponseEntity.ok(updatedClient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientModel> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<java.util.List<ClientModel>> getAllClients() {
        List<ClientModel> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @DeleteMapping("/delete-client/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get-all/enterprise/{enterpriseId}")
    public ResponseEntity<List<ClientResponseDTO>> getAllClientByEnterprise(@PathVariable Long enterpriseId) throws JsonProcessingException {
        List<ClientResponseDTO> getAllClient = clientService.getAllByEnterprise(enterpriseId);
        return ResponseEntity.ok(getAllClient);
    }

    @GetMapping("/get-by-ruc/{ruc}")
    public ResponseEntity<ClientResponseDTO> getAllClientByRuc(@PathVariable String ruc) throws JsonProcessingException {
        ClientResponseDTO getAllClient = clientService.getClientByRuc(ruc);
        return ResponseEntity.ok(getAllClient);
    }
    @GetMapping("/get-by-ci/{ci}")
    public ResponseEntity<ClientResponseDTO> getAllClientByCI(@PathVariable String ci) throws JsonProcessingException {
        ClientResponseDTO getAllClient = clientService.getClientByIdentificacion(ci);
        return ResponseEntity.ok(getAllClient);
    }


}
