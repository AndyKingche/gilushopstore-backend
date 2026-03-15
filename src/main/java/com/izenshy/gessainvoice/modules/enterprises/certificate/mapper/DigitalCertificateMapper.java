package com.izenshy.gessainvoice.modules.enterprises.certificate.mapper;

import com.izenshy.gessainvoice.modules.enterprises.certificate.dto.DigitalCertificateDTO;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.DigitalCertificateModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DigitalCertificateMapper {
    DigitalCertificateMapper INSTANCE = Mappers.getMapper(DigitalCertificateMapper.class);

    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseModelToEnterpriseId")
    DigitalCertificateDTO modelToDTO(DigitalCertificateModel model);

    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseIdToEnterpriseModel")
    DigitalCertificateModel dtoToModel(DigitalCertificateDTO dto);

    // Enterprise mapping
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
