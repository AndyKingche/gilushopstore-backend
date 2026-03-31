package com.izenshy.gessainvoice.modules.person.client.service.impl;

import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.EnterpriseRepository;
import com.izenshy.gessainvoice.modules.person.client.dto.ClientRequestCreateDTO;
import com.izenshy.gessainvoice.modules.person.client.dto.ClientRequestDTO;
import com.izenshy.gessainvoice.modules.person.client.dto.ClientResponseDTO;
import com.izenshy.gessainvoice.modules.person.client.mapper.ClientMapper;
import com.izenshy.gessainvoice.modules.person.client.model.ClientModel;
import com.izenshy.gessainvoice.modules.person.client.repository.ClientRepository;
import com.izenshy.gessainvoice.modules.person.client.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final ClientMapper clientMapper;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, EnterpriseRepository enterpriseRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.clientMapper = clientMapper;
    }


    @Override
    public ClientModel saveClient(ClientModel clientModel) {
        return clientRepository.findByClientIdentification(clientModel.getClientIdentification()).map(
                clientFound -> {

                    clientFound.setClientFullName(clientModel.getClientFullName());
                    clientFound.setClientAddress(clientModel.getClientAddress());
                    clientFound.setClientEmail(clientModel.getClientEmail());
                    clientFound.setClientCellphone(clientModel.getClientCellphone());
                    clientFound.setClientTypeIdentification(clientModel.getClientTypeIdentification());
                    clientFound.setClientRuc(clientModel.getClientRuc());
                    clientFound.setClientIdentification(clientModel.getClientIdentification());
                    clientFound.setClientGender(clientModel.getClientGender());
                    clientFound.setClientStatus(true);

                    return clientRepository.save(clientFound);
                }
        ).orElseGet(() -> clientRepository.save(clientModel));

    }

    @Override
    public ClientModel saveClientDTO(ClientRequestCreateDTO clientRequestDTO) {
        // Validate that enterprise exists
        if (clientRequestDTO.getEnterpriseId() == null || !enterpriseRepository.existsById(clientRequestDTO.getEnterpriseId())) {
            throw new ResourceNotFoundException("Enterprise does not exist");
        }

        // Create enterprise reference
        EnterpriseModel enterpriseRef = new EnterpriseModel();
        enterpriseRef.setId(clientRequestDTO.getEnterpriseId());

        return clientRepository.findByClientIdentification(clientRequestDTO.getClientIdentification()).
                map(clientFound -> {

                    clientFound.setClientFullName(clientRequestDTO.getClientFullName());
                    clientFound.setClientAddress(clientRequestDTO.getClientAddress());
                    clientFound.setClientEmail(clientRequestDTO.getClientEmail());
                    clientFound.setClientCellphone(clientRequestDTO.getClientCellphone());
                    clientFound.setClientTypeIdentification(clientRequestDTO.getClientTypeIdentification());
                    clientFound.setClientRuc(clientRequestDTO.getClientRUC());
                    clientFound.setClientIdentification(clientRequestDTO.getClientIdentification());
                    clientFound.setClientGender(clientRequestDTO.getClientGender());
                    clientFound.setClientStatus(true);
                    clientFound.setEnterpriseId(enterpriseRef);

                    return clientRepository.save(clientFound);
                }).orElseGet(() -> {

                    ClientModel newClient = new ClientModel();
                    newClient.setClientFullName(clientRequestDTO.getClientFullName());
                    newClient.setClientAddress(clientRequestDTO.getClientAddress());
                    newClient.setClientEmail(clientRequestDTO.getClientEmail());
                    newClient.setClientCellphone(clientRequestDTO.getClientCellphone());
                    newClient.setClientTypeIdentification(clientRequestDTO.getClientTypeIdentification());
                    newClient.setClientRuc(clientRequestDTO.getClientIdentification());
                    newClient.setClientIdentification(clientRequestDTO.getClientIdentification());
                    newClient.setClientGender(clientRequestDTO.getClientGender());
                    newClient.setClientStatus(true);
                    newClient.setEnterpriseId(enterpriseRef);

                    return clientRepository.save(newClient);
                });

    }

    @Override
    public ClientModel getClientById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(()-> new ResourceNotFoundException("Cliente no encontrado con Id: " + clientId));
    }

    @Override
    public ClientModel updateClientDTO(Long id, ClientRequestDTO clientRequestDTO) {
        // Validate that enterprise exists
        if (clientRequestDTO.getEnterpriseId() == null || !enterpriseRepository.existsById(clientRequestDTO.getEnterpriseId())) {
            throw new ResourceNotFoundException("Enterprise does not exist");
        }

        // Create enterprise reference
        EnterpriseModel enterpriseRef = new EnterpriseModel();
        enterpriseRef.setId(clientRequestDTO.getEnterpriseId());

        return clientRepository.findById(id).map(clientFound -> {
            clientFound.setClientFullName(clientRequestDTO.getClientFullName());
            clientFound.setClientAddress(clientRequestDTO.getClientAddress());
            clientFound.setClientEmail(clientRequestDTO.getClientEmail());
            clientFound.setClientCellphone(clientRequestDTO.getClientCellphone());
            clientFound.setClientTypeIdentification(clientRequestDTO.getClientTypeIdentification());
            clientFound.setClientRuc(clientRequestDTO.getClientIdentification());
            clientFound.setClientIdentification(clientRequestDTO.getClientIdentification());
            clientFound.setClientGender(clientRequestDTO.getClientGender());
            clientFound.setClientStatus(clientRequestDTO.getClientStatus());
            clientFound.setEnterpriseId(enterpriseRef);

            return clientRepository.save(clientFound);
        }).orElseThrow(() -> new ResourceNotFoundException("Client not found with id " + id));
    }

    @Override
    public List<ClientModel> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    public void deleteClient(Long id) {
        Optional<ClientModel> clientOpt = clientRepository.findById(id);
        if (clientOpt.isPresent()) {
            ClientModel client = clientOpt.get();
            client.setClientStatus(false);
            clientRepository.save(client);
        } else {
            throw new ResourceNotFoundException("Client not found with id " + id);
        }
    }

    @Override
    public ClientResponseDTO getClientByRuc(String ruc) {
        return clientRepository.findByClientRuc(ruc)
                .map(clientMapper::modelToResponseDTO)
                .orElseThrow(()-> new ResourceNotFoundException("Cliente no encontrado con RUC: "));
    }

    @Override
    public ClientResponseDTO getClientByIdentificacion(String identificacion) {
        return clientRepository.findByClientIdentification(identificacion)
                .map(clientMapper::modelToResponseDTO)
                .orElseThrow(()-> new ResourceNotFoundException("Cliente no encontrado"));
    }

    @Override
    public List<ClientResponseDTO> getAllByEnterprise(Long enterpriseId) {
        List<ClientModel> clients = clientRepository.findByEnterpriseId_Id(enterpriseId);
        return clients.stream().map(clientMapper::modelToResponseDTO).collect(Collectors.toList());
    }
}
