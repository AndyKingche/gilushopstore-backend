package com.izenshy.gessainvoice.modules.cashregister.mapper;

import com.izenshy.gessainvoice.modules.cashregister.dto.CashRegisterRequestDTO;
import com.izenshy.gessainvoice.modules.cashregister.dto.CashRegisterResponseDTO;
import com.izenshy.gessainvoice.modules.cashregister.model.CashRegisterModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CashRegisterMapper {
    CashRegisterMapper INSTANCE = Mappers.getMapper(CashRegisterMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cashRegisterUuid", ignore = true)
    @Mapping(target = "openingDate", ignore = true)
    @Mapping(target = "closingDate", ignore = true)
    @Mapping(target = "openingTotal", ignore = true)
    @Mapping(target = "closingCash", ignore = true)
    @Mapping(target = "closingTransfer", ignore = true)
    @Mapping(target = "closingTotal", ignore = true)
    @Mapping(target = "totalSalesCash", ignore = true)
    @Mapping(target = "totalSalesTransfer", ignore = true)
    @Mapping(target = "totalExpenses", ignore = true)
    @Mapping(target = "totalInvestments", ignore = true)
    @Mapping(target = "cashDifference", ignore = true)
    @Mapping(target = "transferDifference", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "closingNotes", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateUpdated", ignore = true)
    @Mapping(source = "userId", target = "userId", qualifiedByName = "mapUserIdToUserModel")
    @Mapping(source = "outletId", target = "outletId", qualifiedByName = "mapOutletIdToOutletModel")
    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseIdToEnterpriseModel")
    CashRegisterModel dtoToModel(CashRegisterRequestDTO dto);

    @Mapping(source = "userId", target = "userId", qualifiedByName = "mapUserModelToUserId")
    @Mapping(source = "outletId", target = "outletId", qualifiedByName = "mapOutletModelToOutletId")
    @Mapping(source = "enterpriseId", target = "enterpriseId", qualifiedByName = "mapEnterpriseModelToEnterpriseId")
    CashRegisterResponseDTO modelToResponseDTO(CashRegisterModel model);

    @Named("mapUserIdToUserModel")
    default UserModel mapUserIdToUserModel(Long id) {
        if (id == null) return null;
        UserModel user = new UserModel();
        user.setId(id);
        return user;
    }

    @Named("mapOutletIdToOutletModel")
    default OutletModel mapOutletIdToOutletModel(Long id) {
        if (id == null) return null;
        OutletModel outlet = new OutletModel();
        outlet.setOutletId(id);
        return outlet;
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

    @Named("mapOutletModelToOutletId")
    default Long mapOutletModelToOutletId(OutletModel outlet) {
        return outlet != null ? outlet.getOutletId() : null;
    }

    @Named("mapEnterpriseModelToEnterpriseId")
    default Long mapEnterpriseModelToEnterpriseId(EnterpriseModel enterprise) {
        return enterprise != null ? enterprise.getId() : null;
    }
}