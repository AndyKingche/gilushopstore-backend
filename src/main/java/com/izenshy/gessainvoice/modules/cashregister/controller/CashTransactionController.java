package com.izenshy.gessainvoice.modules.cashregister.controller;

import com.izenshy.gessainvoice.modules.cashregister.dto.CashTransactionRequestDTO;
import com.izenshy.gessainvoice.modules.cashregister.dto.CashTransactionResponseDTO;
import com.izenshy.gessainvoice.modules.cashregister.model.CashTransactionModel;
import com.izenshy.gessainvoice.modules.cashregister.service.CashTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/gessa/cash-transaction")
@Tag(name = "Cash Transaction", description = "Operations related to cash register transactions")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class CashTransactionController {

    private final CashTransactionService cashTransactionService;

    @Autowired
    public CashTransactionController(CashTransactionService cashTransactionService) {
        this.cashTransactionService = cashTransactionService;
    }

    @PostMapping("/create")
    public ResponseEntity<CashTransactionModel> createTransaction(@RequestBody CashTransactionRequestDTO requestDTO) {
        CashTransactionModel transaction = cashTransactionService.createTransaction(requestDTO);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CashTransactionModel> getTransactionById(@PathVariable Long id) {
        CashTransactionModel transaction = cashTransactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<CashTransactionResponseDTO> getTransactionByUuid(@PathVariable String uuid) {
        UUID cashTransactionUuid = UUID.fromString(uuid);

        CashTransactionResponseDTO transaction = cashTransactionService.getTransactionByUuid(cashTransactionUuid);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CashTransactionModel>> getAllTransactions() {
        List<CashTransactionModel> transactions = cashTransactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/cash-register/{cashRegisterId}")
    public ResponseEntity<List<CashTransactionResponseDTO>> getTransactionsByCashRegister(@PathVariable Long cashRegisterId) {
        List<CashTransactionResponseDTO> transactions = cashTransactionService.getTransactionsByCashRegister(cashRegisterId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/cash-register/{cashRegisterId}/reference/{referenceNumber}")
    public ResponseEntity<List<CashTransactionModel>> getByCashRegisterAndReference(
            @PathVariable Long cashRegisterId,
            @PathVariable String referenceNumber
    ) {
        List<CashTransactionModel> transactions =
                cashTransactionService.findByCashRegisterId_IdAndReferenceNumber(
                        cashRegisterId,
                        referenceNumber
                );

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<CashTransactionResponseDTO>> getTransactionsByInvoice(@PathVariable Long invoiceId) {
        List<CashTransactionResponseDTO> transactions = cashTransactionService.getTransactionsByInvoice(invoiceId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/type/{transactionType}")
    public ResponseEntity<List<CashTransactionResponseDTO>> getTransactionsByType(@PathVariable String transactionType) {
        List<CashTransactionResponseDTO> transactions = cashTransactionService.getTransactionsByType(transactionType);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CashTransactionResponseDTO>> getTransactionsByUser(@PathVariable Long userId) {
        List<CashTransactionResponseDTO> transactions = cashTransactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(transactions);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        cashTransactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/close/{id}")
    public ResponseEntity<CashTransactionModel> closeTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(cashTransactionService.closeTransaction(id));
    }
}