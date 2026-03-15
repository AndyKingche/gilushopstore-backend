package com.izenshy.gessainvoice.modules.product.product.mapper;

import com.izenshy.gessainvoice.modules.product.product.dto.ProductDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.ProductDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.product.model.CategoryModel;
import com.izenshy.gessainvoice.modules.product.product.model.DetailModel;
import com.izenshy.gessainvoice.modules.product.product.model.ProductModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    // --- ProductDTO ---
    @Mapping(source = "categoryId", target = "categoryId", qualifiedByName = "mapCategoryModelToId")
    @Mapping(source = "detailId", target = "detailId", qualifiedByName = "mapDetailModelToId")
    ProductDTO modelToDTO(ProductModel productModel);

    @Mapping(source = "categoryId", target = "categoryId", qualifiedByName = "mapIdToCategoryModel")
    @Mapping(source = "detailId", target = "detailId", qualifiedByName = "mapIdToDetailModel")
    ProductModel dtoToModel(ProductDTO productDTO);

    List<ProductDTO> modelsToDTOs(List<ProductModel> productModels);

    List<ProductModel> dtosToModels(List<ProductDTO> dtos);

    // --- ProductDeluxeDTO ---
    @Mapping(source = "categoryId.categoryName", target = "categoryName")
    @Mapping(source = "detailId.detailName", target = "detailName")
    ProductDeluxeDTO modelToDeluxeDTO(ProductModel productModel);

    @Mapping(source = "categoryName", target = "categoryId", qualifiedByName = "mapCategoryNameToModel")
    @Mapping(source = "detailName", target = "detailId", qualifiedByName = "mapDetailNameToModel")
    ProductModel deluxeDTOToModel(ProductDeluxeDTO productDeluxeDTO);

    List<ProductDeluxeDTO> modelsToDeluxeDTOs(List<ProductModel> productModels);

    List<ProductModel> deluxeDTOsToModels(List<ProductDeluxeDTO> deluxeDTOs);

    // --- Mapping Helpers ---
    @Named("mapCategoryModelToId")
    default Long mapCategoryModelToId(CategoryModel category) {
        return category != null ? category.getId() : null;
    }

    @Named("mapDetailModelToId")
    default Long mapDetailModelToId(DetailModel detail) {
        return detail != null ? detail.getId() : null;
    }

    @Named("mapIdToCategoryModel")
    default CategoryModel mapIdToCategoryModel(Long id) {
        if (id == null) return null;
        CategoryModel category = new CategoryModel();
        category.setId(id);
        return category;
    }

    @Named("mapIdToDetailModel")
    default DetailModel mapIdToDetailModel(Long id) {
        if (id == null) return null;
        DetailModel detail = new DetailModel();
        detail.setId(id);
        return detail;
    }

    @Named("mapCategoryNameToModel")
    default CategoryModel mapCategoryNameToModel(String name) {
        if (name == null) return null;
        CategoryModel category = new CategoryModel();
        category.setCategoryName(name);
        return category;
    }

    @Named("mapDetailNameToModel")
    default DetailModel mapDetailNameToModel(String name) {
        if (name == null) return null;
        DetailModel detail = new DetailModel();
        detail.setDetailName(name);
        return detail;
    }
}
