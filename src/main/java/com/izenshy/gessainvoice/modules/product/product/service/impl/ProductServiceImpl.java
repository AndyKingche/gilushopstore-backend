package com.izenshy.gessainvoice.modules.product.product.service.impl;

import com.izenshy.gessainvoice.common.exception.BadRequestException;
import com.izenshy.gessainvoice.common.exception.ResourceAlreadyExistsException;
import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.product.product.dto.ListProductDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.ListProductDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.ProductDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.ProductDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.product.mapper.ProductMapper;
import com.izenshy.gessainvoice.modules.product.product.model.CategoryModel;
import com.izenshy.gessainvoice.modules.product.product.model.DetailModel;
import com.izenshy.gessainvoice.modules.product.product.model.ProductModel;
import com.izenshy.gessainvoice.modules.product.product.repository.CategoryRepository;
import com.izenshy.gessainvoice.modules.product.product.repository.DetailRepository;
import com.izenshy.gessainvoice.modules.product.product.repository.ProductRepository;
import com.izenshy.gessainvoice.modules.product.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DetailRepository detailRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
            DetailRepository detailRepository, @Lazy ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.detailRepository = detailRepository;
        this.productMapper = productMapper;
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {

        if (productDTO.getProductCode() == null || productDTO.getProductCode().isEmpty()) {
            throw new BadRequestException("Product code is required");
        }

        productRepository.findByProductCode(productDTO.getProductCode()).ifPresent(p -> {
            throw new ResourceAlreadyExistsException("Product with code " + productDTO.getProductCode() + " already exists");
        });

        ProductModel product = productMapper.dtoToModel(productDTO);
        ProductModel saved = productRepository.save(product);
        return productMapper.modelToDTO(saved);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        ProductModel existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!existing.getProductCode().equals(productDTO.getProductCode())
                && productRepository.existsByProductCode(productDTO.getProductCode())) {
            throw new ResourceNotFoundException("Product code " + productDTO.getProductCode() + " already exists");
        }

        existing.setProductName(productDTO.getProductName());
        existing.setProductCode(productDTO.getProductCode());
        existing.setProductDesc(productDTO.getProductDesc());

        if (productDTO.getCategoryId() != null) {
            CategoryModel category = new CategoryModel();
            category.setId(productDTO.getCategoryId());
            existing.setCategoryId(category);
        }
        if (productDTO.getDetailId() != null) {
            DetailModel detail = new DetailModel();
            detail.setId(productDTO.getDetailId());
            existing.setDetailId(detail);
        }

        ProductModel updated = productRepository.save(existing);
        return productMapper.modelToDTO(updated);
    }

    @Override
    public ProductDeluxeDTO createProductDeluxe(ProductDeluxeDTO productDeluxeDTO) {
        if (productDeluxeDTO.getProductCode() == null || productDeluxeDTO.getProductCode().isEmpty()) {
            throw new BadRequestException("Product code is required");
        }

        productRepository.findByProductCode(productDeluxeDTO.getProductCode()).ifPresent(p -> {
            throw new ResourceAlreadyExistsException("Product with code " + productDeluxeDTO.getProductCode() + " already exists");
        });

        ProductModel product = productMapper.deluxeDTOToModel(productDeluxeDTO);

        // 🔎 Buscar o crear Category
        if (productDeluxeDTO.getCategoryName() != null) {
            CategoryModel category = categoryRepository.findByCategoryNameIgnoreCase(productDeluxeDTO.getCategoryName())
                    .orElseGet(() -> {
                        CategoryModel newCategory = new CategoryModel();
                        newCategory.setCategoryName(productDeluxeDTO.getCategoryName());
                        return categoryRepository.save(newCategory); // 💾 guardamos la nueva
                    });
            product.setCategoryId(category);
        }

        // 🔎 Buscar o crear Detail
        if (productDeluxeDTO.getDetailName() != null) {
            DetailModel detail = detailRepository.findByDetailNameIgnoreCase(productDeluxeDTO.getDetailName())
                    .orElseGet(() -> {
                        DetailModel newDetail = new DetailModel();
                        newDetail.setDetailName(productDeluxeDTO.getDetailName());
                        return detailRepository.save(newDetail); // 💾 guardamos el nuevo
                    });
            product.setDetailId(detail);
        }

        ProductModel saved = productRepository.save(product);
        return mapToDeluxeDTO(saved);
    }

    @Override
    @Transactional
    public ProductDeluxeDTO updateProductDeluxe(Long id, ProductDeluxeDTO productDeluxeDTO) {

        // 1. Buscar producto existente
        ProductModel existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // 2. Validar código único (solo si cambia)
        if (!existing.getProductCode().equals(productDeluxeDTO.getProductCode())
                && productRepository.existsByProductCode(productDeluxeDTO.getProductCode())) {
            throw new ResourceAlreadyExistsException("Product code " + productDeluxeDTO.getProductCode() + " already exists");
        }

        // 3. Actualizar datos básicos
        existing.setProductName(productDeluxeDTO.getProductName());
        existing.setProductCode(productDeluxeDTO.getProductCode());
        existing.setProductDesc(productDeluxeDTO.getProductDesc());

        // 4. Manejo de Categoría (find or create)
        if (productDeluxeDTO.getCategoryName() != null && !productDeluxeDTO.getCategoryName().isEmpty()) {
            CategoryModel category = categoryRepository
                    .findByCategoryNameIgnoreCase(productDeluxeDTO.getCategoryName().trim().toUpperCase())
                    .orElseGet(() -> {
                        CategoryModel newCategory = new CategoryModel();
                        newCategory.setCategoryName(productDeluxeDTO.getCategoryName().trim().toUpperCase());
                        return categoryRepository.save(newCategory);
                    });

            existing.setCategoryId(category);
        }

        // 5. Manejo de Detalle (find or create)
        if (productDeluxeDTO.getDetailName() != null && !productDeluxeDTO.getDetailName().isEmpty()) {
            DetailModel detail = detailRepository
                    .findByDetailNameIgnoreCase(productDeluxeDTO.getDetailName().trim().toUpperCase())
                    .orElseGet(() -> {
                        DetailModel newDetail = new DetailModel();
                        newDetail.setDetailName(productDeluxeDTO.getDetailName().trim().toUpperCase());
                        return detailRepository.save(newDetail);
                    });

            existing.setDetailId(detail);
        }

        // 6. Guardar producto actualizado
        ProductModel updated = productRepository.save(existing);

        // 7. Mapear a DTO y retornar
        return mapToDeluxeDTO(updated);
    }

    private ProductDeluxeDTO mapToDeluxeDTO(ProductModel product) {
        ProductDeluxeDTO deluxe = new ProductDeluxeDTO();
        deluxe.setId(product.getId());
        deluxe.setProductName(product.getProductName());
        deluxe.setProductCode(product.getProductCode());
        deluxe.setProductDesc(product.getProductDesc());

        if (product.getCategoryId() != null) {
            deluxe.setCategoryName(product.getCategoryId().getCategoryName());
        }
        if (product.getDetailId() != null) {
            deluxe.setDetailName(product.getDetailId().getDetailName());
        }
        return deluxe;
    }

    @Override
    public ProductDTO getProductById(Long id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.modelToDTO(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public ListProductDTO getAllProducts() {
        List<ProductDTO> list = productRepository.findAll()
                .stream()
                .map(productMapper::modelToDTO)
                .toList();
        ListProductDTO result = new ListProductDTO();
        result.setListProduct(list);
        return result;
    }

    @Override
    public ListProductDeluxeDTO getAllProductsDeluxe() {
        List<ProductDeluxeDTO> deluxeList = productRepository.findAll()
                .stream()
                .map(productMapper::modelToDeluxeDTO)
                .toList();
        ListProductDeluxeDTO response = new ListProductDeluxeDTO();
        response.setListProduct(deluxeList);
        return response;
    }
}