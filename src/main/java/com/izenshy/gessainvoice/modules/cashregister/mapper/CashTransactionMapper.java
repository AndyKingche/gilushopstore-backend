package com.izenshy.gessainvoice.modules.cashregister.mapper;

import com.izenshy.gessainvoice.modules.cashregister.dto.CashTransactionRequestDTO;
import com.izenshy.gessainvoice.modules.cashregister.dto.CashTransactionResponseDTO;
import com.izenshy.gessainvoice.modules.cashregister.model.CashRegisterModel;
import com.izenshy.gessainvoice.modules.cashregister.model.CashTransactionModel;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CashTransactionMapper {
    CashTransactionMapper INSTANCE = Mappers.getMapper(CashTransactionMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionUuid", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "transactionDate", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateUpdated", ignore = true)
    @Mapping(source = "cashRegisterId", target = "cashRegisterId", qualifiedByName = "mapCashRegisterIdToCashRegisterModel")
    @Mapping(source = "invoiceId", target = "invoiceId", qualifiedByName = "mapInvoiceIdToInvoiceModel")
    @Mapping(source = "userId", target = "userId", qualifiedByName = "mapUserIdToUserModel")
    CashTransactionModel dtoToModel(CashTransactionRequestDTO dto);

    @Mapping(source = "cashRegisterId", target = "cashRegisterId", qualifiedByName = "mapCashRegisterModelToCashRegisterId")
    @Mapping(source = "invoiceId", target = "invoiceId", qualifiedByName = "mapInvoiceModelToInvoiceId")
    @Mapping(source = "userId", target = "userId", qualifiedByName = "mapUserModelToUserId")
    CashTransactionResponseDTO modelToResponseDTO(CashTransactionModel model);

    @Named("mapCashRegisterIdToCashRegisterModel")
    default CashRegisterModel mapCashRegisterIdToCashRegisterModel(Long id) {
        if (id == null) return null;
        CashRegisterModel cashRegister = new CashRegisterModel();
        cashRegister.setId(id);
        return cashRegister;
    }

    @Named("mapInvoiceIdToInvoiceModel")
    default InvoiceModel mapInvoiceIdToInvoiceModel(Long id) {
        if (id == null) return null;
        InvoiceModel invoice = new InvoiceModel();
        invoice.setId(id);
        return invoice;
    }

    @Named("mapUserIdToUserModel")
    default UserModel mapUserIdToUserModel(Long id) {
        if (id == null) return null;
        UserModel user = new UserModel();
        user.setId(id);
        return user;
    }

    @Named("mapCashRegisterModelToCashRegisterId")
    default Long mapCashRegisterModelToCashRegisterId(CashRegisterModel cashRegister) {
        return cashRegister != null ? cashRegister.getId() : null;
    }

    @Named("mapInvoiceModelToInvoiceId")
    default Long mapInvoiceModelToInvoiceId(InvoiceModel invoice) {
        return invoice != null ? invoice.getId() : null;
    }

    @Named("mapUserModelToUserId")
    default Long mapUserModelToUserId(UserModel user) {
        return user != null ? user.getId() : null;
    }
}