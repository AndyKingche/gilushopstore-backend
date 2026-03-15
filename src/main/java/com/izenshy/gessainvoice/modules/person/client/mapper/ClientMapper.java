package com.izenshy.gessainvoice.modules.person.client.mapper;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;

import com.izenshy.gessainvoice.modules.person.client.dto.ClientRequestDTO;
import com.izenshy.gessainvoice.modules.person.client.dto.ClientResponseDTO;
import com.izenshy.gessainvoice.modules.person.client.model.ClientModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseModelToEnterpriseId")
    ClientRequestDTO modelToDTO(ClientModel clientModel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clientUuid", ignore = true)
    @Mapping(target = "clientRuc", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateUpdated", ignore = true)
    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseIdToEnterpriseModel")
    ClientModel dtoToModel(ClientRequestDTO userDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clientUuid", ignore = true)
    @Mapping(target = "clientRuc", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateUpdated", ignore = true)
    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseIdToEnterpriseModel")
    ClientModel dtoToModel(ClientResponseDTO clientResponseDTO);

    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseModelToEnterpriseId")
    ClientResponseDTO modelToResponseDTO(ClientModel clientModel);

    @Named("mapEnterpriseModelToEnterpriseId")
    default Long mapEnterpriseModelToEnterpriseId(EnterpriseModel enterprise) {
        return enterprise != null ? enterprise.getId() : null;
    }

    @Named("mapEnterpriseIdToEnterpriseModel")
    default EnterpriseModel mapEnterpriseIdToEnterpriseModel(Long id) {
        if (id == null) return null;
        EnterpriseModel enterprise = new EnterpriseModel();
        enterprise.setId(id);
        return enterprise;
    }
}
