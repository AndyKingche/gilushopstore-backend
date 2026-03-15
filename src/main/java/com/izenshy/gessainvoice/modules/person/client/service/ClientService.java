package com.izenshy.gessainvoice.modules.person.client.service;

import com.izenshy.gessainvoice.modules.person.client.dto.ClientRequestCreateDTO;
import com.izenshy.gessainvoice.modules.person.client.dto.ClientRequestDTO;
import com.izenshy.gessainvoice.modules.person.client.dto.ClientResponseDTO;
import com.izenshy.gessainvoice.modules.person.client.model.ClientModel;

import java.util.List;
import java.util.Optional;

public interface ClientService {

    ClientModel saveClient(ClientModel clientModel);
    ClientModel saveClientDTO(ClientRequestCreateDTO clientRequestDTO);
    ClientModel updateClientDTO(Long id, ClientRequestDTO clientRequestDTO);
    ClientModel getClientById(Long clientId);
    ClientResponseDTO getClientByRuc(String ruc);
    ClientResponseDTO getClientByIdentificacion(String identificacion);
    List<ClientModel> getAllClients();
    void deleteClient(Long id);
    //List<ClientRequestDTO> getAllByEnterprise(Long enterpriseId);
    List<ClientResponseDTO> getAllByEnterprise(Long enterpriseId);
}
