package com.izenshy.gessainvoice.modules.product.product.mapper;

import com.izenshy.gessainvoice.modules.product.product.dto.ImageStockDTO;
import com.izenshy.gessainvoice.modules.product.product.model.ImageStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageStockMapper {
    ImageStockMapper INSTANCE = Mappers.getMapper(ImageStockMapper.class);

    @Mapping(source = "stock.id.productId", target = "stockProductId")
    @Mapping(source = "stock.id.outletId", target = "stockOutletId")
    ImageStockDTO modelToDTO(ImageStock imageStock);

    @Mapping(target = "stock", ignore = true)
    ImageStock dtoToModel(ImageStockDTO imageStockDTO);

    List<ImageStockDTO> modelsToDTOs(List<ImageStock> imageStocks);

    List<ImageStock> dtosToModels(List<ImageStockDTO> dtos);
}