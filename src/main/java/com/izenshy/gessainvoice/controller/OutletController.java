package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.service.OutletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/gessa/outlet")
@Tag(name = "Outlet", description = "Esta sección es dedicada a las operaciones relacionadas con los Outlets")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class OutletController {
    private final OutletService outletService;

    @Autowired
    public OutletController(OutletService outletService) {
        this.outletService = outletService;
    }

    // Obtener todos los outlets
    @GetMapping("/all")
    public ResponseEntity<List<OutletModel>> getAll() {
        return ResponseEntity.ok(outletService.findAll());
    }

    // Obtener outlet por ID
    @GetMapping("/{id}")
    public ResponseEntity<OutletModel> getById(@PathVariable Long id) {
        return outletService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Outlet not found with id " + id));
    }

    // Obtener outlets por enterprise ID
    @GetMapping("/by-enterprise/{enterpriseId}")
    public ResponseEntity<List<OutletModel>> getByEnterpriseId(@PathVariable Long enterpriseId) {
        List<OutletModel> outlets = outletService.findByEnterpriseId(enterpriseId);
        if (outlets.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(outlets);
    }

    // Crear nuevo outlet
    @PostMapping("/create")
    public ResponseEntity<OutletModel> create(@RequestBody OutletModel outlet) {
        OutletModel created = outletService.save(outlet);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Actualizar outlet existente
    @PutMapping("/update/{id}")
    public ResponseEntity<OutletModel> update(@PathVariable Long id, @RequestBody OutletModel outlet) {
        if (outletService.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Outlet not found with id " + id);
        }
        outlet.setOutletId(id); // Ensure ID is set
        OutletModel updated = outletService.update(outlet);
        return ResponseEntity.ok(updated);
    }

    // Eliminar outlet
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (outletService.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Outlet not found with id " + id);
        }
        outletService.delete(id);
        return ResponseEntity.noContent().build();
    }
}