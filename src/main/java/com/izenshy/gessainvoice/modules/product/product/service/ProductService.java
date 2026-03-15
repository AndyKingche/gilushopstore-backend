package com.izenshy.gessainvoice.modules.product.product.service;

import com.izenshy.gessainvoice.modules.product.product.dto.ListProductDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.ListProductDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.ProductDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.ProductDeluxeDTO;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    void deleteProduct(Long id);
    ProductDTO getProductById(Long id);
    ListProductDTO getAllProducts();
    ListProductDeluxeDTO getAllProductsDeluxe();
    ProductDeluxeDTO createProductDeluxe(ProductDeluxeDTO productDeluxeDTO);
    ProductDeluxeDTO updateProductDeluxe(Long id, ProductDeluxeDTO productDeluxeDTO);
}
