package com.izenshy.gessainvoice.modules.invoice.mapper;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.person.client.model.ClientModel;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {InvoiceDetailMapper.class})
public interface InvoiceMapper {
    InvoiceMapper INSTANCE = Mappers.getMapper(InvoiceMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "invoiceUuid", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateUpdated", ignore = true)
    @Mapping(source = "userId", target = "userId", qualifiedByName = "mapUserIdToUserModel")
    @Mapping(source = "clientId", target = "clientId", qualifiedByName = "mapClientIdToClientModel")
    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseIdToEnterpriseModel")
    @Mapping(source = "details", target = "details") // <- mapear la lista de detalles usando InvoiceDetailMapper
    InvoiceModel requestDtoToModel(InvoiceRequestDTO dto);

    @Mapping(source = "userId", target = "userId", qualifiedByName = "mapUserModelToUserId")
    @Mapping(source = "clientId", target = "clientId", qualifiedByName = "mapClientModelToClientId")
    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseModelToEnterpriseId")
    InvoiceResponseDTO modelToResponseDto(InvoiceModel model);

    @Named("mapUserIdToUserModel")
    default UserModel mapUserIdToUserModel(Long id) {
        if (id == null) return null;
        UserModel user = new UserModel();
        user.setId(id);
        return user;
    }

    @Named("mapClientIdToClientModel")
    default ClientModel mapClientIdToClientModel(Long id) {
        if (id == null) return null;
        ClientModel client = new ClientModel();
        client.setId(id);
        return client;
    }

    @Named("mapEnterpriseIdToEnterpriseModel")
    default EnterpriseModel mapEnterpriseIdToEnterpriseModel(Long id) {
        if (id == null) return null;
        EnterpriseModel enterprise = new EnterpriseModel();
        enterprise.setId(id);
        return enterprise;
    }

    @Named("mapUserModelToUserId")
    default Long mapUserModelToUserId(UserModel user) {
        return user != null ? user.getId() : null;
    }

    @Named("mapClientModelToClientId")
    default Long mapClientModelToClientId(ClientModel client) {
        return client != null ? client.getId() : null;
    }

    @Named("mapEnterpriseModelToEnterpriseId")
    default Long mapEnterpriseModelToEnterpriseId(EnterpriseModel enterprise) {
        return enterprise != null ? enterprise.getId() : null;
    }
}