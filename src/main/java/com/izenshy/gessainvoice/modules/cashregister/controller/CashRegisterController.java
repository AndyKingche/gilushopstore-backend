package com.izenshy.gessainvoice.modules.cashregister.controller;

import com.izenshy.gessainvoice.modules.cashregister.dto.CashRegisterRequestDTO;
import com.izenshy.gessainvoice.modules.cashregister.dto.CashRegisterResponseDTO;
import com.izenshy.gessainvoice.modules.cashregister.model.CashRegisterModel;
import com.izenshy.gessainvoice.modules.cashregister.service.CashRegisterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/gessa/cash-register")
@Tag(name = "Cash Register", description = "Operations related to cash register management")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class CashRegisterController {

    private final CashRegisterService cashRegisterService;

    @Autowired
    public CashRegisterController(CashRegisterService cashRegisterService) {
        this.cashRegisterService = cashRegisterService;
    }

    @PostMapping("/open")
    public ResponseEntity<CashRegisterModel> openCashRegister(@RequestBody CashRegisterRequestDTO requestDTO) {
        CashRegisterModel cashRegister = cashRegisterService.openCashRegister(requestDTO);
        return ResponseEntity.ok(cashRegister);
    }

    @PutMapping("/close/{id}")
    public ResponseEntity<CashRegisterModel> closeCashRegister(@PathVariable Long id,
                                                               @RequestParam String closingNotes,
                                                               @RequestParam Double closingCash,
                                                               @RequestParam Double closingTransfer) {
        CashRegisterModel cashRegister = cashRegisterService.closeCashRegister(id, closingNotes, closingCash, closingTransfer);
        return ResponseEntity.ok(cashRegister);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CashRegisterModel> getCashRegisterById(@PathVariable Long id) {
        CashRegisterModel cashRegister = cashRegisterService.getCashRegisterById(id);
        return ResponseEntity.ok(cashRegister);
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<CashRegisterResponseDTO> getCashRegisterByUuid(@PathVariable String uuid) {
        UUID cashRegisterUuid = UUID.fromString(uuid);

        CashRegisterResponseDTO cashRegister = cashRegisterService.getCashRegisterByUuid(cashRegisterUuid);
        return ResponseEntity.ok(cashRegister);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CashRegisterModel>> getAllCashRegisters() {
        List<CashRegisterModel> cashRegisters = cashRegisterService.getAllCashRegisters();
        return ResponseEntity.ok(cashRegisters);
    }

    @GetMapping("/outlet/{outletId}")
    public ResponseEntity<List<CashRegisterResponseDTO>> getCashRegistersByOutlet(@PathVariable Long outletId) {
        List<CashRegisterResponseDTO> cashRegisters = cashRegisterService.getCashRegistersByOutlet(outletId);
        return ResponseEntity.ok(cashRegisters);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CashRegisterResponseDTO>> getCashRegistersByUser(@PathVariable Long userId) {
        List<CashRegisterResponseDTO> cashRegisters = cashRegisterService.getCashRegistersByUser(userId);
        return ResponseEntity.ok(cashRegisters);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CashRegisterResponseDTO>> getCashRegistersByStatus(@PathVariable String status) {
        List<CashRegisterResponseDTO> cashRegisters = cashRegisterService.getCashRegistersByStatus(status);
        return ResponseEntity.ok(cashRegisters);
    }

    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<List<CashRegisterResponseDTO>> getCashRegistersByEnterprise(@PathVariable Long enterpriseId) {
        List<CashRegisterResponseDTO> cashRegisters = cashRegisterService.getCashRegistersByEnterprise(enterpriseId);
        return ResponseEntity.ok(cashRegisters);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCashRegister(@PathVariable Long id) {
        cashRegisterService.deleteCashRegister(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene la caja ABIERTA del día de hoy
     */
    @GetMapping("/open/today")
    public ResponseEntity<CashRegisterModel> getOpenCashRegisterToday(
            @RequestParam Long outletId,
            @RequestParam Long enterpriseId
    ) {
        CashRegisterModel cashRegister =
                cashRegisterService.getOpenCashRegisterToday(outletId, enterpriseId);

        return ResponseEntity.ok(cashRegister);
    }

    /**
     * Valida si ya existe una caja ABIERTA hoy
     * Útil antes de abrir una nueva
     */
    @GetMapping("/open/today/exists")
    public ResponseEntity<Boolean> existsOpenCashRegisterToday(
            @RequestParam Long outletId,
            @RequestParam Long enterpriseId
    ) {
        boolean exists = cashRegisterService
                .findOpenCashRegisterToday(outletId, enterpriseId)
                .isPresent();

        return ResponseEntity.ok(exists);
    }
}