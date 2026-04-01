package com.izenshy.gessainvoice.modules.cashregister.service.impl;

import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.cashregister.dto.CashRegisterRequestDTO;
import com.izenshy.gessainvoice.modules.cashregister.dto.CashRegisterResponseDTO;
import com.izenshy.gessainvoice.modules.cashregister.mapper.CashRegisterMapper;
import com.izenshy.gessainvoice.modules.cashregister.model.CashRegisterModel;
import com.izenshy.gessainvoice.modules.cashregister.repository.CashRegisterRepository;
import com.izenshy.gessainvoice.modules.cashregister.service.CashRegisterService;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.EnterpriseRepository;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.OutletRepository;
import com.izenshy.gessainvoice.modules.person.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CashRegisterServiceImpl implements CashRegisterService {

    private final CashRegisterRepository cashRegisterRepository;
    private final CashRegisterMapper cashRegisterMapper;
    private final UserRepository userRepository;
    private final OutletRepository outletRepository;
    private final EnterpriseRepository enterpriseRepository;

    @Autowired
    public CashRegisterServiceImpl(CashRegisterRepository cashRegisterRepository,
            CashRegisterMapper cashRegisterMapper,
            UserRepository userRepository,
            OutletRepository outletRepository,
            EnterpriseRepository enterpriseRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.cashRegisterMapper = cashRegisterMapper;
        this.userRepository = userRepository;
        this.outletRepository = outletRepository;
        this.enterpriseRepository = enterpriseRepository;
    }

    @Override
    public CashRegisterModel openCashRegister(CashRegisterRequestDTO requestDTO) {
        // Validate that user, outlet, and enterprise exist
        if (requestDTO.getUserId() == null || !userRepository.existsById(requestDTO.getUserId())) {
            throw new ResourceNotFoundException("User does not exist");
        }
        if (requestDTO.getOutletId() == null || !outletRepository.existsById(requestDTO.getOutletId())) {
            throw new ResourceNotFoundException("Outlet does not exist");
        }
        if (requestDTO.getEnterpriseId() == null || !enterpriseRepository.existsById(requestDTO.getEnterpriseId())) {
            throw new ResourceNotFoundException("Enterprise does not exist");
        }

        // Check if there's already an open cash register for this outlet
        List<CashRegisterModel> openRegisters = cashRegisterRepository.findByOutletId_OutletId(requestDTO.getOutletId())
                .stream()
                .filter(cr -> "ABIERTA".equals(cr.getStatus()) || "POR_CERRAR".equals(cr.getStatus()))
                .collect(Collectors.toList());

        if (!openRegisters.isEmpty()) {
            throw new ResourceNotFoundException("There is already an open cash register for this outlet");
        }

        CashRegisterModel cashRegister = cashRegisterMapper.dtoToModel(requestDTO);
        cashRegister.setOpeningDate(LocalDateTime.now());
        cashRegister.setStatus("ABIERTA");

        // Calculate opening total
        BigDecimal openingCash = requestDTO.getOpeningCash() != null ? requestDTO.getOpeningCash() : BigDecimal.ZERO;
        BigDecimal openingTransfer = requestDTO.getOpeningTransfer() != null ? requestDTO.getOpeningTransfer()
                : BigDecimal.ZERO;
        cashRegister.setOpeningTotal(openingCash.add(openingTransfer));

        return cashRegisterRepository.save(cashRegister);
    }

    @Override
    public CashRegisterModel closeCashRegister(Long id, String closingNotes, Double closingCash,
            Double closingTransfer) {
        CashRegisterModel cashRegister = cashRegisterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cash register not found"));

        if (!"ABIERTA".equals(cashRegister.getStatus()) && !"POR_CERRAR".equals(cashRegister.getStatus())) {
            throw new ResourceNotFoundException("Cash register is not open");
        }

        cashRegister.setClosingDate(LocalDateTime.now());
        cashRegister.setStatus("CERRADA");
        cashRegister.setClosingNotes(closingNotes);
        cashRegister.setClosingCash(BigDecimal.valueOf(closingCash != null ? closingCash : 0.0));
        cashRegister.setClosingTransfer(BigDecimal.valueOf(closingTransfer != null ? closingTransfer : 0.0));
        cashRegister.setClosingTotal(cashRegister.getClosingCash().add(cashRegister.getClosingTransfer()));

        // Calculate differences
        // BigDecimal expectedCash =
        // cashRegister.getOpeningCash().add(cashRegister.getTotalSalesCash()).subtract(
        // cashRegister.getTotalExpenses() != null ? cashRegister.getTotalExpenses() :
        // BigDecimal.ZERO);
        // BigDecimal expectedTransfer =
        // cashRegister.getOpeningTransfer().add(cashRegister.getTotalSalesTransfer()).subtract(
        // cashRegister.getTotalInvestments() != null ?
        // cashRegister.getTotalInvestments() : BigDecimal.ZERO);

        // cashRegister.setCashDifference(cashRegister.getClosingCash().subtract(expectedCash));
        // cashRegister.setTransferDifference(cashRegister.getClosingTransfer().subtract(expectedTransfer));

        // return cashRegisterRepository.save(cashRegister);
        // Calculate differences
        BigDecimal totalExpenses = cashRegister.getTotalExpenses() != null ? cashRegister.getTotalExpenses()
                : BigDecimal.ZERO;
        BigDecimal totalInvestments = cashRegister.getTotalInvestments() != null ? cashRegister.getTotalInvestments()
                : BigDecimal.ZERO;

        BigDecimal expectedCash = cashRegister.getOpeningCash()
                .add(cashRegister.getTotalSalesCash())
                .add(totalInvestments)
                .subtract(totalExpenses);

        BigDecimal expectedTransfer = cashRegister.getOpeningTransfer()
                .add(cashRegister.getTotalSalesTransfer());

        cashRegister.setCashDifference(cashRegister.getClosingCash().subtract(expectedCash));
        cashRegister.setTransferDifference(cashRegister.getClosingTransfer().subtract(expectedTransfer));

        return cashRegisterRepository.save(cashRegister);
    }

    @Override
    public CashRegisterModel getCashRegisterById(Long id) {
        return cashRegisterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cash register not found"));
    }

    @Override
    public CashRegisterResponseDTO getCashRegisterByUuid(UUID uuid) {

        return cashRegisterRepository.findByCashRegisterUuid(uuid)
                .map(cashRegisterMapper::modelToResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Cash register not found"));
    }

    @Override
    public List<CashRegisterModel> getAllCashRegisters() {
        return cashRegisterRepository.findAll();
    }

    @Override
    public List<CashRegisterResponseDTO> getCashRegistersByOutlet(Long outletId) {
        return cashRegisterRepository.findByOutletId_OutletId(outletId)
                .stream()
                .map(cashRegisterMapper::modelToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashRegisterResponseDTO> getCashRegistersByUser(Long userId) {
        return cashRegisterRepository.findByUserId_Id(userId)
                .stream()
                .map(cashRegisterMapper::modelToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashRegisterResponseDTO> getCashRegistersByStatus(String status) {
        return cashRegisterRepository.findByStatus(status)
                .stream()
                .map(cashRegisterMapper::modelToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashRegisterResponseDTO> getCashRegistersByEnterprise(Long enterpriseId) {
        return cashRegisterRepository.findByEnterpriseId_Id(enterpriseId)
                .stream()
                .map(cashRegisterMapper::modelToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCashRegister(Long id) {
        if (!cashRegisterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cash register not found");
        }
        cashRegisterRepository.deleteById(id);
    }

    @Override
    public CashRegisterModel getOpenCashRegisterToday(Long outletId, Long enterpriseId) {

        LocalDate today = LocalDate.now();

        LocalDateTime startDate = today.atStartOfDay();
        LocalDateTime endDate = today.plusDays(1).atStartOfDay();

        return cashRegisterRepository
                .findOpenCashRegisterToday(outletId, enterpriseId, startDate, endDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una caja ABIERTA para hoy en este punto de venta"));
    }

    @Override
    public Optional<CashRegisterModel> findOpenCashRegisterToday(Long outletId, Long enterpriseId) {
        LocalDate today = LocalDate.now();

        LocalDateTime startDate = today.atStartOfDay();
        LocalDateTime endDate = today.plusDays(1).atStartOfDay();

        return cashRegisterRepository
                .findOpenCashRegisterToday(outletId, enterpriseId, startDate, endDate);

    }

    @Override
    public void validateNoOpenCashRegisterToday(Long outletId, Long enterpriseId) {
        LocalDate today = LocalDate.now();

        LocalDateTime startDate = today.atStartOfDay();
        LocalDateTime endDate = today.plusDays(1).atStartOfDay();

        boolean exists = cashRegisterRepository
                .findOpenCashRegisterToday(outletId, enterpriseId, startDate, endDate)
                .isPresent();

        if (exists) {
            throw new ResourceNotFoundException(
                    "Ya existe una caja ABIERTA para hoy en este punto de venta");
        }
    }
}