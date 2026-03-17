package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.product.product.dto.CatalogBrandDTO;
import com.izenshy.gessainvoice.modules.product.product.service.CatalogBrandService;
import com.izenshy.gessainvoice.modules.product.stock.dto.OnlineStoreProductDTO;
import com.izenshy.gessainvoice.modules.product.stock.service.StockService;
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
    private final StockService stockService;

    @Autowired
    public CatalogBrandController(CatalogBrandService catalogBrandService, StockService stockService) {
        this.catalogBrandService = catalogBrandService;
        this.stockService = stockService;
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

    // Obtener productos de tienda online por marca
    @GetMapping("/online-store/{outletId}/brand/{brandId}")
    public ResponseEntity<List<OnlineStoreProductDTO>> getOnlineStoreProductsByBrand(
            @PathVariable Long outletId,
            @PathVariable Long brandId,
            @RequestParam(defaultValue = "4") int pageSize,
            @RequestParam(defaultValue = "0") int offset) {
        List<OnlineStoreProductDTO> products = stockService.getOnlineStoreProductsByBrand(outletId, brandId, pageSize, offset);
        return ResponseEntity.ok(products);
    }

    // Contar productos de tienda online por marca
    @GetMapping("/online-store/{outletId}/brand/{brandId}/count")
    public ResponseEntity<Long> getOnlineStoreProductsByBrandCount(@PathVariable Long outletId, @PathVariable Long brandId) {
        Long count = stockService.getOnlineStoreProductsByBrandCount(outletId, brandId);
        return ResponseEntity.ok(count);
    }
}