package com.izenshy.gessainvoice.modules.invoice.mapper;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationAuxResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceTempAuthorizationModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface InvoiceTempAuthorizationMapper {
    InvoiceTempAuthorizationMapper INSTANCE = Mappers.getMapper(InvoiceTempAuthorizationMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tempUuid", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateUpdated", ignore = true)
    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseIdToEnterpriseModel")
    @Mapping(source = "outletId", target = "outletId", qualifiedByName = "mapOutletIdToOutletModel")
    @Mapping(source = "invoiceId", target = "invoiceId", qualifiedByName = "mapInvoiceIdToInvoiceModel")
    InvoiceTempAuthorizationModel requestDtoToModel(InvoiceTempAuthorizationRequestDTO dto);

    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseModelToEnterpriseId")
    @Mapping(source = "outletId", target = "outletId", qualifiedByName = "mapOutletModelToOutletId")
    @Mapping(source = "invoiceId", target = "invoiceId", qualifiedByName = "mapInvoiceModelToInvoiceId")
    InvoiceTempAuthorizationResponseDTO modelToResponseDto(InvoiceTempAuthorizationModel model);

    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseModelToEnterpriseId")
    @Mapping(source = "outletId", target = "outletId", qualifiedByName = "mapOutletModelToOutletId")
    @Mapping(source = "invoiceId", target = "invoiceId", qualifiedByName = "mapInvoiceModelToInvoiceId")
    InvoiceTempAuthorizationAuxResponseDTO modelToAuxResponseDto(InvoiceTempAuthorizationModel model);

    @Named("mapEnterpriseIdToEnterpriseModel")
    default EnterpriseModel mapEnterpriseIdToEnterpriseModel(Long id) {
        if (id == null) return null;
        EnterpriseModel enterprise = new EnterpriseModel();
        enterprise.setId(id);
        return enterprise;
    }

    @Named("mapOutletIdToOutletModel")
    default OutletModel mapOutletIdToOutletModel(Long id) {
        if (id == null) return null;
        OutletModel outlet = new OutletModel();
        outlet.setOutletId(id);
        return outlet;
    }

    @Named("mapInvoiceIdToInvoiceModel")
    default InvoiceModel mapInvoiceIdToInvoiceModel(Long id) {
        if (id == null) return null;
        InvoiceModel invoice = new InvoiceModel();
        invoice.setId(id);
        return invoice;
    }

    @Named("mapEnterpriseModelToEnterpriseId")
    default Long mapEnterpriseModelToEnterpriseId(EnterpriseModel model) {
        if (model == null) return null;
        return model.getId();
    }

    @Named("mapOutletModelToOutletId")
    default Long mapOutletModelToOutletId(OutletModel model) {
        if (model == null) return null;
        return model.getOutletId();
    }

    @Named("mapInvoiceModelToInvoiceId")
    default Long mapInvoiceModelToInvoiceId(InvoiceModel model) {
        if (model == null) return null;
        return model.getId();
    }
}
