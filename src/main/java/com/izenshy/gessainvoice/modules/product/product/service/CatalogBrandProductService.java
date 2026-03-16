package com.izenshy.gessainvoice.modules.product.product.service;

import com.izenshy.gessainvoice.modules.product.product.dto.CatalogBrandProductDTO;

import java.util.List;

public interface CatalogBrandProductService {
    CatalogBrandProductDTO createCatalogBrandProduct(CatalogBrandProductDTO catalogBrandProductDTO);
    void deleteCatalogBrandProduct(Long id);
    CatalogBrandProductDTO getCatalogBrandProductById(Long id);
    List<CatalogBrandProductDTO> getAllCatalogBrandProducts();
    List<CatalogBrandProductDTO> getByBrandId(Long brandId);
    List<CatalogBrandProductDTO> getByProductId(Long productId);
}