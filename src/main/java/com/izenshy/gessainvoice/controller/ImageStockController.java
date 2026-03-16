package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.product.product.dto.ImageStockDTO;
import com.izenshy.gessainvoice.modules.product.product.service.ImageStockService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/gessa/image-stock")
@Tag(name = "ImageStock", description = "Operations related to Image Stock")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class ImageStockController {
    private final ImageStockService imageStockService;

    @Autowired
    public ImageStockController(ImageStockService imageStockService) {
        this.imageStockService = imageStockService;
    }

    @PostMapping
    public ResponseEntity<ImageStockDTO> createImageStock(@RequestBody ImageStockDTO imageStockDTO) {
        ImageStockDTO created = imageStockService.createImageStock(imageStockDTO);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImageStockDTO> updateImageStock(@PathVariable Long id, @RequestBody ImageStockDTO imageStockDTO) {
        ImageStockDTO updated = imageStockService.updateImageStock(id, imageStockDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImageStock(@PathVariable Long id) {
        imageStockService.deleteImageStock(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageStockDTO> getImageStockById(@PathVariable Long id) {
        ImageStockDTO imageStock = imageStockService.getImageStockById(id);
        return ResponseEntity.ok(imageStock);
    }

    @GetMapping
    public ResponseEntity<List<ImageStockDTO>> getAllImageStocks() {
        List<ImageStockDTO> imageStocks = imageStockService.getAllImageStocks();
        return ResponseEntity.ok(imageStocks);
    }

    @GetMapping("/stock/{productId}/{outletId}")
    public ResponseEntity<List<ImageStockDTO>> getImageStocksByStock(@PathVariable Long productId, @PathVariable Long outletId) {
        List<ImageStockDTO> imageStocks = imageStockService.getImageStocksByStock(productId, outletId);
        return ResponseEntity.ok(imageStocks);
    }
}