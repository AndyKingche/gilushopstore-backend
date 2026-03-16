package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.product.product.dto.CatalogBrandDTO;
import com.izenshy.gessainvoice.modules.product.product.service.CatalogBrandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/gessa/catalog-brand")
@Tag(name = "CatalogBrand", description = "Operations related to Catalog Brand")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class CatalogBrandController {
    private final CatalogBrandService catalogBrandService;

    @Autowired
    public CatalogBrandController(CatalogBrandService catalogBrandService) {
        this.catalogBrandService = catalogBrandService;
    }

    @PostMapping
    public ResponseEntity<CatalogBrandDTO> createCatalogBrand(@RequestBody CatalogBrandDTO catalogBrandDTO) {
        CatalogBrandDTO created = catalogBrandService.createCatalogBrand(catalogBrandDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogBrandDTO> updateCatalogBrand(@PathVariable Long id, @RequestBody CatalogBrandDTO catalogBrandDTO) {
        CatalogBrandDTO updated = catalogBrandService.updateCatalogBrand(id, catalogBrandDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCatalogBrand(@PathVariable Long id) {
        catalogBrandService.deleteCatalogBrand(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogBrandDTO> getCatalogBrandById(@PathVariable Long id) {
        CatalogBrandDTO catalogBrand = catalogBrandService.getCatalogBrandById(id);
        return ResponseEntity.ok(catalogBrand);
    }

    @GetMapping
    public ResponseEntity<List<CatalogBrandDTO>> getAllCatalogBrands() {
        List<CatalogBrandDTO> catalogBrands = catalogBrandService.getAllCatalogBrands();
        return ResponseEntity.ok(catalogBrands);
    }
}