package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.service.EnterpriseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/gessa/enterprise")
@Tag(name = "Enterprise", description = "Esta sección es dedicada a las operaciones relacionadas con la Empresa")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    @Autowired
    public EnterpriseController(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    @GetMapping("/identification/{id}")
    public ResponseEntity<EnterpriseModel> getById(@PathVariable Long id) {
        return enterpriseService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Enterprise not found with id " + id));
    }

    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<EnterpriseModel> getByRuc(@PathVariable String ruc) {
        return enterpriseService.getEnterpriseByRuc(ruc)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Enterprise not found with ruc " + ruc));
    }
}
