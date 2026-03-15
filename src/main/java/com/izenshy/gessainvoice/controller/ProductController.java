package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.product.product.dto.ListProductDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.ListProductDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.ProductDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.ProductDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.product.model.CategoryModel;
import com.izenshy.gessainvoice.modules.product.product.service.ProductService;
import com.izenshy.gessainvoice.modules.product.product.service.impl.CategoryServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/gessa/product")
@Tag(name = "Product", description = "Esta sección es dedicada a las operaciones relacionadas con los Productos")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Listar todos los productos (básico)
    @GetMapping("/list-product-dto")
    public ResponseEntity<ListProductDTO> getAll() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // Listar todos los productos (deluxe)
    @GetMapping("/list-product-deluxe")
    public ResponseEntity<ListProductDeluxeDTO> getAllDeluxe() {
        return ResponseEntity.ok(productService.getAllProductsDeluxe());
    }

    // Obtener producto por ID (básico)
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Crear producto (básico)
    @PostMapping("create")
    public ResponseEntity<ProductDTO> create(@RequestBody ProductDTO productDTO) {
        ProductDTO created = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Actualizar producto (básico)
    @PutMapping("/update/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    // Eliminar producto
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Crear producto (deluxe)
    @PostMapping("/create-deluxe")
    public ResponseEntity<ProductDeluxeDTO> createDeluxe(@RequestBody ProductDeluxeDTO productDeluxeDTO) {
        ProductDeluxeDTO created = productService.createProductDeluxe(productDeluxeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Actualizar producto (deluxe)
    @PutMapping("/update-deluxe/{id}")
    public ResponseEntity<ProductDeluxeDTO> updateDeluxe(@PathVariable Long id, @RequestBody ProductDeluxeDTO productDeluxeDTO) {
        return ResponseEntity.ok(productService.updateProductDeluxe(id, productDeluxeDTO));
    }

}
