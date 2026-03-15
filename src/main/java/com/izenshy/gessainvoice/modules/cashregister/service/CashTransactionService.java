package com.izenshy.gessainvoice.modules.cashregister.service;

import com.izenshy.gessainvoice.modules.cashregister.dto.CashTransactionRequestDTO;
import com.izenshy.gessainvoice.modules.cashregister.dto.CashTransactionResponseDTO;
import com.izenshy.gessainvoice.modules.cashregister.model.CashTransactionModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashTransactionService {

    CashTransactionModel createTransaction(CashTransactionRequestDTO requestDTO);
    CashTransactionModel getTransactionById(Long id);
    CashTransactionResponseDTO getTransactionByUuid(UUID uuid);
    List<CashTransactionModel> getAllTransactions();
    List<CashTransactionResponseDTO> getTransactionsByCashRegister(Long cashRegisterId);
    List<CashTransactionResponseDTO> getTransactionsByInvoice(Long invoiceId);
    List<CashTransactionResponseDTO> getTransactionsByType(String transactionType);
    List<CashTransactionResponseDTO> getTransactionsByUser(Long userId);
    void deleteTransaction(Long id);
    List<CashTransactionModel> findByCashRegisterId_IdAndReferenceNumber(
            Long cashRegisterId,
            String referenceNumber
    );
    CashTransactionModel closeTransaction(Long transactionId);
}