package com.izenshy.gessainvoice.modules.cashregister.repository;

import com.izenshy.gessainvoice.modules.cashregister.model.CashTransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CashTransactionRepository extends JpaRepository<CashTransactionModel, Long> {

    Optional<CashTransactionModel> findByTransactionUuid(UUID uuid);
    List<CashTransactionModel> findByCashRegisterId_Id(Long cashRegisterId);
    List<CashTransactionModel> findByCashRegisterId_IdAndReferenceNumber(
            Long cashRegisterId,
            String referenceNumber
    );
    List<CashTransactionModel> findByInvoiceId_Id(Long invoiceId);
    List<CashTransactionModel> findByTransactionType(String transactionType);
    List<CashTransactionModel> findByUserId_Id(Long userId);
}