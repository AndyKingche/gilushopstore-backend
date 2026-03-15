package com.izenshy.gessainvoice.modules.cashregister.service;

import com.izenshy.gessainvoice.modules.cashregister.dto.CashRegisterRequestDTO;
import com.izenshy.gessainvoice.modules.cashregister.dto.CashRegisterResponseDTO;
import com.izenshy.gessainvoice.modules.cashregister.model.CashRegisterModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashRegisterService {

    CashRegisterModel openCashRegister(CashRegisterRequestDTO requestDTO);
    CashRegisterModel closeCashRegister(Long id, String closingNotes, Double closingCash, Double closingTransfer);
    CashRegisterModel getCashRegisterById(Long id);
    CashRegisterResponseDTO getCashRegisterByUuid(UUID uuid);
    List<CashRegisterModel> getAllCashRegisters();
    List<CashRegisterResponseDTO> getCashRegistersByOutlet(Long outletId);
    List<CashRegisterResponseDTO> getCashRegistersByUser(Long userId);
    List<CashRegisterResponseDTO> getCashRegistersByStatus(String status);
    List<CashRegisterResponseDTO> getCashRegistersByEnterprise(Long enterpriseId);
    void deleteCashRegister(Long id);
    /**
     * Obtiene la caja ABIERTA del día de hoy.
     * Lanza excepción si no existe.
     */
    CashRegisterModel getOpenCashRegisterToday(
            Long outletId,
            Long enterpriseId
    );

    /**
     * Retorna Optional de la caja ABIERTA del día de hoy.
     */
    Optional<CashRegisterModel> findOpenCashRegisterToday(
            Long outletId,
            Long enterpriseId
    );

    /**
     * Valida que NO exista una caja ABIERTA hoy.
     * Útil antes de abrir una nueva.
     */
    void validateNoOpenCashRegisterToday(
            Long outletId,
            Long enterpriseId
    );
}