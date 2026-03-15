package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.product.product.model.DetailModel;
import com.izenshy.gessainvoice.modules.product.product.service.DetailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/gessa/detail-product")
@Tag(name = "Detail", description = "Esta sección es dedicada a las operaciones relacionadas con los detalles del Producto")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class DetailController {
    private final DetailService detailService;

    @Autowired
    public DetailController(DetailService detailService) {
        this.detailService = detailService;
    }

    // Obtener todos los detalles
    @GetMapping("/all")
    public ResponseEntity<List<DetailModel>> getAll() {
        return ResponseEntity.ok(detailService.findAll());
    }

    // Obtener detalle por ID
    @GetMapping("/{id}")
    public ResponseEntity<DetailModel> getById(@PathVariable Long id) {
        return detailService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Detail not found with id " + id));
    }

    // Crear nuevo detalle
    @PostMapping("/create")
    public ResponseEntity<DetailModel> create(@RequestBody DetailModel detail) {
        DetailModel created = detailService.create(detail);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Actualizar detalle existente
    @PutMapping("/update/{id}")
    public ResponseEntity<DetailModel> update(@PathVariable Long id, @RequestBody DetailModel detail) {
        if (detailService.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Detail not found with id " + id);
        }
        DetailModel updated = detailService.update(id, detail);
        return ResponseEntity.ok(updated);
    }

    // Eliminar detalle
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (detailService.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Detail not found with id " + id);
        }
        detailService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
