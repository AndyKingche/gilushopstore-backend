package com.izenshy.gessainvoice.modules.product.product.mapper;

import com.izenshy.gessainvoice.modules.product.product.dto.CatalogBrandProductDTO;
import com.izenshy.gessainvoice.modules.product.product.model.CatalogBrandProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CatalogBrandProductMapper {
    CatalogBrandProductMapper INSTANCE = Mappers.getMapper(CatalogBrandProductMapper.class);

    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "product.id", target = "productId")
    CatalogBrandProductDTO modelToDTO(CatalogBrandProduct catalogBrandProduct);

    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "product", ignore = true)
    CatalogBrandProduct dtoToModel(CatalogBrandProductDTO catalogBrandProductDTO);

    List<CatalogBrandProductDTO> modelsToDTOs(List<CatalogBrandProduct> catalogBrandProducts);

    List<CatalogBrandProduct> dtosToModels(List<CatalogBrandProductDTO> dtos);
}