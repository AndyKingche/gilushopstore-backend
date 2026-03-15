package com.izenshy.gessainvoice.modules.cashregister.service.impl;

import com.izenshy.gessainvoice.modules.cashregister.dto.CashTransactionRequestDTO;
import com.izenshy.gessainvoice.modules.cashregister.dto.CashTransactionResponseDTO;
import com.izenshy.gessainvoice.modules.cashregister.mapper.CashTransactionMapper;
import com.izenshy.gessainvoice.modules.cashregister.model.CashRegisterModel;
import com.izenshy.gessainvoice.modules.cashregister.model.CashTransactionModel;
import com.izenshy.gessainvoice.modules.cashregister.repository.CashRegisterRepository;
import com.izenshy.gessainvoice.modules.cashregister.repository.CashTransactionRepository;
import com.izenshy.gessainvoice.modules.cashregister.service.CashTransactionService;
import com.izenshy.gessainvoice.modules.invoice.repository.InvoiceRepository;
import com.izenshy.gessainvoice.modules.person.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CashTransactionServiceImpl implements CashTransactionService {

    private final CashTransactionRepository cashTransactionRepository;
    private final CashTransactionMapper cashTransactionMapper;
    private final CashRegisterRepository cashRegisterRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    @Autowired
    public CashTransactionServiceImpl(CashTransactionRepository cashTransactionRepository,
                                      CashTransactionMapper cashTransactionMapper,
                                      CashRegisterRepository cashRegisterRepository,
                                      InvoiceRepository invoiceRepository,
                                      UserRepository userRepository) {
        this.cashTransactionRepository = cashTransactionRepository;
        this.cashTransactionMapper = cashTransactionMapper;
        this.cashRegisterRepository = cashRegisterRepository;
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CashTransactionModel createTransaction(CashTransactionRequestDTO requestDTO) {
        // Validate that cash register exists and is open
        if (requestDTO.getCashRegisterId() == null || !cashRegisterRepository.existsById(requestDTO.getCashRegisterId())) {
            throw new RuntimeException("Cash register does not exist");
        }

        // Validate that user exists
        if (requestDTO.getUserId() == null || !userRepository.existsById(requestDTO.getUserId())) {
            throw new RuntimeException("User does not exist");
        }

        // Validate invoice if provided
        if (requestDTO.getInvoiceId() != null && !invoiceRepository.existsById(requestDTO.getInvoiceId())) {
            throw new RuntimeException("Invoice does not exist");
        }

        CashTransactionModel transaction = cashTransactionMapper.dtoToModel(requestDTO);
        transaction.setTransactionDate(LocalDateTime.now());

        // Calculate total amount
        BigDecimal amountCash = requestDTO.getAmountCash() != null ? requestDTO.getAmountCash() : BigDecimal.ZERO;
        BigDecimal amountTransfer = requestDTO.getAmountTransfer() != null ? requestDTO.getAmountTransfer() : BigDecimal.ZERO;
        transaction.setTotalAmount(amountCash.add(amountTransfer));

        CashTransactionModel savedTransaction = cashTransactionRepository.save(transaction);

        // Update cash register totals
        updateCashRegisterTotals(requestDTO.getCashRegisterId());

        return savedTransaction;
    }

    @Override
    public CashTransactionModel getTransactionById(Long id) {
        return cashTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    @Override
    public CashTransactionResponseDTO getTransactionByUuid(UUID uuid) {
        return cashTransactionRepository.findByTransactionUuid(uuid)
                .map(cashTransactionMapper::modelToResponseDTO)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    @Override
    public List<CashTransactionModel> getAllTransactions() {
        return cashTransactionRepository.findAll();
    }

    @Override
    public List<CashTransactionResponseDTO> getTransactionsByCashRegister(Long cashRegisterId) {
        return cashTransactionRepository.findByCashRegisterId_Id(cashRegisterId)
                .stream()
                .map(cashTransactionMapper::modelToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashTransactionResponseDTO> getTransactionsByInvoice(Long invoiceId) {
        return cashTransactionRepository.findByInvoiceId_Id(invoiceId)
                .stream()
                .map(cashTransactionMapper::modelToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashTransactionResponseDTO> getTransactionsByType(String transactionType) {
        return cashTransactionRepository.findByTransactionType(transactionType)
                .stream()
                .map(cashTransactionMapper::modelToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashTransactionResponseDTO> getTransactionsByUser(Long userId) {
        return cashTransactionRepository.findByUserId_Id(userId)
                .stream()
                .map(cashTransactionMapper::modelToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTransaction(Long id) {
        CashTransactionModel transaction = cashTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        Long cashRegisterId = transaction.getCashRegisterId().getId();
        cashTransactionRepository.deleteById(id);

        // Update cash register totals after deletion
        updateCashRegisterTotals(cashRegisterId);
    }

    @Override
    public List<CashTransactionModel> findByCashRegisterId_IdAndReferenceNumber(Long cashRegisterId, String referenceNumber) {
        return cashTransactionRepository
                .findByCashRegisterId_IdAndReferenceNumber(
                        cashRegisterId,
                        referenceNumber
                );
    }

    @Override
    public CashTransactionModel closeTransaction(Long transactionId) {
        CashTransactionModel transaction = cashTransactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));


        transaction.setReferenceNumber("CERRADO");

        return cashTransactionRepository.save(transaction);
    }

    private void updateCashRegisterTotals(Long cashRegisterId) {
        CashRegisterModel cashRegister = cashRegisterRepository.findById(cashRegisterId)
                .orElseThrow(() -> new RuntimeException("Cash register not found"));

        // Calculate total sales cash
        BigDecimal totalSalesCash = cashTransactionRepository.findByCashRegisterId_Id(cashRegisterId)
                .stream()
                .filter(t -> "VENTA".equals(t.getTransactionType()))
                .map(CashTransactionModel::getAmountCash)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total sales transfer
        BigDecimal totalSalesTransfer = cashTransactionRepository.findByCashRegisterId_Id(cashRegisterId)
                .stream()
                .filter(t -> "VENTA".equals(t.getTransactionType()))
                .map(CashTransactionModel::getAmountTransfer)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total expenses
        BigDecimal totalExpenses = cashTransactionRepository.findByCashRegisterId_Id(cashRegisterId)
                .stream()
                .filter(t -> "GASTO".equals(t.getTransactionType()))
                .map(CashTransactionModel::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total investments
        BigDecimal totalInvestments = cashTransactionRepository.findByCashRegisterId_Id(cashRegisterId)
                .stream()
                .filter(t -> "INVERSION".equals(t.getTransactionType()))
                .map(CashTransactionModel::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cashRegister.setTotalSalesCash(totalSalesCash);
        cashRegister.setTotalSalesTransfer(totalSalesTransfer);
        cashRegister.setTotalExpenses(totalExpenses);
        cashRegister.setTotalInvestments(totalInvestments);

        cashRegisterRepository.save(cashRegister);
    }
}