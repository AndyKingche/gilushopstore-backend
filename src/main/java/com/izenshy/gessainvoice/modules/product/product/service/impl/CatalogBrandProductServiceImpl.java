package com.izenshy.gessainvoice.modules.product.product.service.impl;

import com.izenshy.gessainvoice.common.exception.ResourceAlreadyExistsException;
import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.product.product.dto.CatalogBrandProductDTO;
import com.izenshy.gessainvoice.modules.product.product.mapper.CatalogBrandProductMapper;
import com.izenshy.gessainvoice.modules.product.product.model.CatalogBrand;
import com.izenshy.gessainvoice.modules.product.product.model.CatalogBrandProduct;
import com.izenshy.gessainvoice.modules.product.product.model.ProductModel;
import com.izenshy.gessainvoice.modules.product.product.repository.CatalogBrandProductRepository;
import com.izenshy.gessainvoice.modules.product.product.repository.CatalogBrandRepository;
import com.izenshy.gessainvoice.modules.product.product.repository.ProductRepository;
import com.izenshy.gessainvoice.modules.product.product.service.CatalogBrandProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CatalogBrandProductServiceImpl implements CatalogBrandProductService {
    private final CatalogBrandProductRepository catalogBrandProductRepository;
    private final CatalogBrandRepository catalogBrandRepository;
    private final ProductRepository productRepository;
    private final CatalogBrandProductMapper catalogBrandProductMapper;

    @Autowired
    public CatalogBrandProductServiceImpl(CatalogBrandProductRepository catalogBrandProductRepository,
                                          CatalogBrandRepository catalogBrandRepository,
                                          ProductRepository productRepository,
                                          CatalogBrandProductMapper catalogBrandProductMapper) {
        this.catalogBrandProductRepository = catalogBrandProductRepository;
        this.catalogBrandRepository = catalogBrandRepository;
        this.productRepository = productRepository;
        this.catalogBrandProductMapper = catalogBrandProductMapper;
    }

    @Override
    public CatalogBrandProductDTO createCatalogBrandProduct(CatalogBrandProductDTO catalogBrandProductDTO) {
        Optional<CatalogBrand> brand = catalogBrandRepository.findById(catalogBrandProductDTO.getBrandId());
        if (brand.isEmpty()) {
            throw new ResourceNotFoundException("Brand not found");
        }
        Optional<ProductModel> product = productRepository.findById(catalogBrandProductDTO.getProductId());
        if (product.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        if (catalogBrandProductRepository.existsByBrand_IdAndProduct_Id(catalogBrandProductDTO.getBrandId(), catalogBrandProductDTO.getProductId())) {
            throw new ResourceAlreadyExistsException("Association already exists");
        }

        CatalogBrandProduct catalogBrandProduct = catalogBrandProductMapper.dtoToModel(catalogBrandProductDTO);
        catalogBrandProduct.setBrand(brand.get());
        catalogBrandProduct.setProduct(product.get());
        CatalogBrandProduct saved = catalogBrandProductRepository.save(catalogBrandProduct);
        return catalogBrandProductMapper.modelToDTO(saved);
    }

    @Override
    public void deleteCatalogBrandProduct(Long id) {
        if (!catalogBrandProductRepository.existsById(id)) {
            throw new ResourceNotFoundException("CatalogBrandProduct not found");
        }
        catalogBrandProductRepository.deleteById(id);
    }

    @Override
    public CatalogBrandProductDTO getCatalogBrandProductById(Long id) {
        Optional<CatalogBrandProduct> catalogBrandProduct = catalogBrandProductRepository.findById(id);
        if (catalogBrandProduct.isEmpty()) {
            throw new ResourceNotFoundException("CatalogBrandProduct not found");
        }
        return catalogBrandProductMapper.modelToDTO(catalogBrandProduct.get());
    }

    @Override
    public List<CatalogBrandProductDTO> getAllCatalogBrandProducts() {
        List<CatalogBrandProduct> catalogBrandProducts = catalogBrandProductRepository.findAll();
        return catalogBrandProducts.stream().map(catalogBrandProductMapper::modelToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CatalogBrandProductDTO> getByBrandId(Long brandId) {
        List<CatalogBrandProduct> catalogBrandProducts = catalogBrandProductRepository.findByBrand_Id(brandId);
        return catalogBrandProducts.stream().map(catalogBrandProductMapper::modelToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CatalogBrandProductDTO> getByProductId(Long productId) {
        List<CatalogBrandProduct> catalogBrandProducts = catalogBrandProductRepository.findByProduct_Id(productId);
        return catalogBrandProducts.stream().map(catalogBrandProductMapper::modelToDTO).collect(Collectors.toList());
    }
}