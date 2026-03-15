package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.product.product.dto.TaxDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.TaxResponse;
import com.izenshy.gessainvoice.modules.product.product.service.TaxService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/gessa/tax")
@Tag(name = "Tax", description = "Esta sección es dedicada a las operaciones relacionadas con el Impuesto Iva")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class TaxController {

    private final TaxService taxService;

    @Autowired
    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    // Crear un tax
    @PostMapping
    public ResponseEntity<TaxDTO> createTax(@RequestBody TaxDTO taxDTO) {
        TaxDTO created = taxService.createTax(taxDTO);
        return ResponseEntity.ok(created);
    }

    // Actualizar un tax
    @PutMapping("/{id}")
    public ResponseEntity<TaxDTO> updateTax(@PathVariable Long id, @RequestBody TaxDTO taxDTO) {
        TaxDTO updated = taxService.updateTax(id, taxDTO);
        return ResponseEntity.ok(updated);
    }

    // Eliminar un tax
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTax(@PathVariable Long id) {
        taxService.deleteTax(id);
        return ResponseEntity.noContent().build();
    }

    // Obtener un tax por ID
    @GetMapping("/{id}")
    public ResponseEntity<TaxResponse> getTaxById(@PathVariable Long id) {
        TaxResponse tax = taxService.getTaxById(id);
        return ResponseEntity.ok(tax);
    }

    // Obtener todos los taxes
    @GetMapping
    public ResponseEntity<List<TaxResponse>> getAllTaxes() {
        List<TaxResponse> taxes = taxService.getAllTaxes();
        return ResponseEntity.ok(taxes);
    }

    // Obtener tax por código
    @GetMapping("/by-code/{taxCode}")
    public ResponseEntity<TaxResponse> getTaxByCode(@PathVariable String taxCode) {
        Optional<TaxResponse> taxOpt = taxService.findByTaxCode(taxCode);
        return taxOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
