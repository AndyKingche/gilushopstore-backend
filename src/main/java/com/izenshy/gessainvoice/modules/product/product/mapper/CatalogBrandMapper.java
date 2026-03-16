package com.izenshy.gessainvoice.modules.product.product.mapper;

import com.izenshy.gessainvoice.modules.product.product.dto.CatalogBrandDTO;
import com.izenshy.gessainvoice.modules.product.product.model.CatalogBrand;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CatalogBrandMapper {
    CatalogBrandMapper INSTANCE = Mappers.getMapper(CatalogBrandMapper.class);

    CatalogBrandDTO modelToDTO(CatalogBrand catalogBrand);

    CatalogBrand dtoToModel(CatalogBrandDTO catalogBrandDTO);

    List<CatalogBrandDTO> modelsToDTOs(List<CatalogBrand> catalogBrands);

    List<CatalogBrand> dtosToModels(List<CatalogBrandDTO> dtos);
}