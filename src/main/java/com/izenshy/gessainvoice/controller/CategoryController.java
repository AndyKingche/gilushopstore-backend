package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.product.product.model.CategoryModel;
import com.izenshy.gessainvoice.modules.product.product.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/gessa/category")
@Tag(name = "Category", description = "Esta sección es dedicada a las operaciones relacionadas con los Categoria Producto")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Obtener todas las categorías
    @GetMapping("/all")
    public ResponseEntity<List<CategoryModel>> getAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    // Obtener categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryModel> getById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id));
    }

    // Crear nueva categoría
    @PostMapping("/create")
    public ResponseEntity<CategoryModel> create(@RequestBody CategoryModel category) {
        CategoryModel created = categoryService.create(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Actualizar categoría existente
    @PutMapping("/update/{id}")
    public ResponseEntity<CategoryModel> update(@PathVariable Long id, @RequestBody CategoryModel category) {
        CategoryModel updated = categoryService.update(id, category);
        return ResponseEntity.ok(updated);
    }

    // Eliminar categoría
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Verificar si existe por nombre
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByName(@RequestParam String name) {
        return ResponseEntity.ok(categoryService.existsByName(name));
    }
}
