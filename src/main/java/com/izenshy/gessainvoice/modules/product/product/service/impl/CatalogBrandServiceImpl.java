package com.izenshy.gessainvoice.modules.product.product.service.impl;

import com.izenshy.gessainvoice.common.exception.BadRequestException;
import com.izenshy.gessainvoice.common.exception.ResourceAlreadyExistsException;
import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.product.product.dto.CatalogBrandDTO;
import com.izenshy.gessainvoice.modules.product.product.mapper.CatalogBrandMapper;
import com.izenshy.gessainvoice.modules.product.product.model.CatalogBrand;
import com.izenshy.gessainvoice.modules.product.product.repository.CatalogBrandRepository;
import com.izenshy.gessainvoice.modules.product.product.service.CatalogBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CatalogBrandServiceImpl implements CatalogBrandService {
    private final CatalogBrandRepository catalogBrandRepository;
    private final CatalogBrandMapper catalogBrandMapper;

    @Autowired
    public CatalogBrandServiceImpl(CatalogBrandRepository catalogBrandRepository, CatalogBrandMapper catalogBrandMapper) {
        this.catalogBrandRepository = catalogBrandRepository;
        this.catalogBrandMapper = catalogBrandMapper;
    }

    @Override
    public CatalogBrandDTO createCatalogBrand(CatalogBrandDTO catalogBrandDTO) {
        if (catalogBrandDTO.getBrandName() == null || catalogBrandDTO.getBrandName().isEmpty()) {
            throw new BadRequestException("Brand name is required");
        }

        catalogBrandRepository.findByBrandName(catalogBrandDTO.getBrandName()).ifPresent(c -> {
            throw new ResourceAlreadyExistsException("Brand with name " + catalogBrandDTO.getBrandName() + " already exists");
        });

        CatalogBrand catalogBrand = catalogBrandMapper.dtoToModel(catalogBrandDTO);
        CatalogBrand saved = catalogBrandRepository.save(catalogBrand);
        return catalogBrandMapper.modelToDTO(saved);
    }

    @Override
    public CatalogBrandDTO updateCatalogBrand(Long id, CatalogBrandDTO catalogBrandDTO) {
        Optional<CatalogBrand> existing = catalogBrandRepository.findById(id);
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("CatalogBrand not found");
        }

        catalogBrandRepository.findByBrandName(catalogBrandDTO.getBrandName()).ifPresent(c -> {
            if (!c.getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Brand with name " + catalogBrandDTO.getBrandName() + " already exists");
            }
        });

        CatalogBrand catalogBrand = catalogBrandMapper.dtoToModel(catalogBrandDTO);
        catalogBrand.setId(id);
        CatalogBrand saved = catalogBrandRepository.save(catalogBrand);
        return catalogBrandMapper.modelToDTO(saved);
    }

    @Override
    public void deleteCatalogBrand(Long id) {
        if (!catalogBrandRepository.existsById(id)) {
            throw new ResourceNotFoundException("CatalogBrand not found");
        }
        catalogBrandRepository.deleteById(id);
    }

    @Override
    public CatalogBrandDTO getCatalogBrandById(Long id) {
        Optional<CatalogBrand> catalogBrand = catalogBrandRepository.findById(id);
        if (catalogBrand.isEmpty()) {
            throw new ResourceNotFoundException("CatalogBrand not found");
        }
        return catalogBrandMapper.modelToDTO(catalogBrand.get());
    }

    @Override
    public List<CatalogBrandDTO> getAllCatalogBrands() {
        List<CatalogBrand> catalogBrands = catalogBrandRepository.findAll();
        return catalogBrands.stream().map(catalogBrandMapper::modelToDTO).collect(Collectors.toList());
    }
}