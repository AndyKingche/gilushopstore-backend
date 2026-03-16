package com.izenshy.gessainvoice.modules.product.product.service;

import com.izenshy.gessainvoice.modules.product.product.dto.CatalogBrandDTO;

import java.util.List;

public interface CatalogBrandService {
    CatalogBrandDTO createCatalogBrand(CatalogBrandDTO catalogBrandDTO);
    CatalogBrandDTO updateCatalogBrand(Long id, CatalogBrandDTO catalogBrandDTO);
    void deleteCatalogBrand(Long id);
    CatalogBrandDTO getCatalogBrandById(Long id);
    List<CatalogBrandDTO> getAllCatalogBrands();
}