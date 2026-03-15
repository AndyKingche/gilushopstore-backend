package com.izenshy.gessainvoice.modules.person.user.mapper;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.person.user.dto.UserDTO;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseModelToEnterpriseId")
    UserDTO modelToDTO(UserModel userModel);

    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseIdToEnterpriseModel")
    UserModel dtoToModel(UserDTO userDTO);

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
