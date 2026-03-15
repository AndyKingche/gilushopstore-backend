package com.izenshy.gessainvoice.modules.invoice.mapper;

import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceDetailModel;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.product.stock.model.StockModel;
import com.izenshy.gessainvoice.modules.product.stock.model.StockPKModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface InvoiceDetailMapper {
    InvoiceDetailMapper INSTANCE = Mappers.getMapper(InvoiceDetailMapper.class);

    // === REQUEST DTO → MODEL ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detailUuid", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateUpdated", ignore = true)
    @Mapping(target = "invoice", source = "invoiceId", qualifiedByName = "mapInvoiceIdToInvoiceModel")
    @Mapping(target = "stock", source = ".", qualifiedByName = "mapStockIdsToStockModel")
    InvoiceDetailModel requestDtoToModel(InvoiceDetailRequestDTO dto);

    // === MODEL → RESPONSE DTO ===
    @Mapping(source = "invoice", target = "invoiceId", qualifiedByName = "mapInvoiceModelToInvoiceId")
    @Mapping(source = "stock", target = "stockProductId", qualifiedByName = "mapStockModelToProductId")
    @Mapping(source = "stock", target = "stockOutletId", qualifiedByName = "mapStockModelToOutletId")
    InvoiceDetailResponseDTO modelToResponseDto(InvoiceDetailModel model);

    // === MAPEO DE FACTURA ===
    @Named("mapInvoiceIdToInvoiceModel")
    default InvoiceModel mapInvoiceIdToInvoiceModel(Long invoiceId) {
        if (invoiceId == null) return null;
        InvoiceModel invoice = new InvoiceModel();
        invoice.setId(invoiceId);
        return invoice;
    }

    @Named("mapInvoiceModelToInvoiceId")
    default Long mapInvoiceModelToInvoiceId(InvoiceModel invoice) {
        return (invoice != null) ? invoice.getId() : null;
    }

    // === MAPEO DE STOCK ===
    @Named("mapStockIdsToStockModel")
    default StockModel mapStockIdsToStockModel(InvoiceDetailRequestDTO dto) {
        if (dto == null || dto.getStockProductId() == null || dto.getStockOutletId() == null) return null;
        StockModel stock = new StockModel();
        StockPKModel pk = new StockPKModel();
        pk.setProductId(dto.getStockProductId());
        pk.setOutletId(dto.getStockOutletId());
        stock.setId(pk);
        return stock;
    }

    @Named("mapStockModelToProductId")
    default Long mapStockModelToProductId(StockModel stock) {
        return (stock != null && stock.getId() != null) ? stock.getId().getProductId() : null;
    }

    @Named("mapStockModelToOutletId")
    default Long mapStockModelToOutletId(StockModel stock) {
        return (stock != null && stock.getId() != null) ? stock.getId().getOutletId() : null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detailUuid", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "dateUpdated", ignore = true)
    @Mapping(target = "invoice", ignore = true) // 🔒 no tocar relación
    @Mapping(target = "stock", source = ".", qualifiedByName = "mapStockIdsToStockModel")
    void updateModelFromDto(
            InvoiceDetailRequestDTO dto,
            @MappingTarget InvoiceDetailModel model
    );
}