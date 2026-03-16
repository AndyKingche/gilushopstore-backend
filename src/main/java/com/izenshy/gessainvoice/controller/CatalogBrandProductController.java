package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.product.product.dto.CatalogBrandProductDTO;
import com.izenshy.gessainvoice.modules.product.product.service.CatalogBrandProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/gessa/catalog-brand-product")
@Tag(name = "CatalogBrandProduct", description = "Operations related to Catalog Brand Product associations")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.DELETE})
public class CatalogBrandProductController {
    private final CatalogBrandProductService catalogBrandProductService;

    @Autowired
    public CatalogBrandProductController(CatalogBrandProductService catalogBrandProductService) {
        this.catalogBrandProductService = catalogBrandProductService;
    }

    @PostMapping
    public ResponseEntity<CatalogBrandProductDTO> createCatalogBrandProduct(@RequestBody CatalogBrandProductDTO catalogBrandProductDTO) {
        CatalogBrandProductDTO created = catalogBrandProductService.createCatalogBrandProduct(catalogBrandProductDTO);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCatalogBrandProduct(@PathVariable Long id) {
        catalogBrandProductService.deleteCatalogBrandProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogBrandProductDTO> getCatalogBrandProductById(@PathVariable Long id) {
        CatalogBrandProductDTO catalogBrandProduct = catalogBrandProductService.getCatalogBrandProductById(id);
        return ResponseEntity.ok(catalogBrandProduct);
    }

    @GetMapping
    public ResponseEntity<List<CatalogBrandProductDTO>> getAllCatalogBrandProducts() {
        List<CatalogBrandProductDTO> catalogBrandProducts = catalogBrandProductService.getAllCatalogBrandProducts();
        return ResponseEntity.ok(catalogBrandProducts);
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<CatalogBrandProductDTO>> getByBrandId(@PathVariable Long brandId) {
        List<CatalogBrandProductDTO> catalogBrandProducts = catalogBrandProductService.getByBrandId(brandId);
        return ResponseEntity.ok(catalogBrandProducts);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<CatalogBrandProductDTO>> getByProductId(@PathVariable Long productId) {
        List<CatalogBrandProductDTO> catalogBrandProducts = catalogBrandProductService.getByProductId(productId);
        return ResponseEntity.ok(catalogBrandProducts);
    }
}