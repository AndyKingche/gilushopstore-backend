package com.izenshy.gessainvoice.modules.cashregister.repository;

import com.izenshy.gessainvoice.modules.cashregister.model.CashRegisterModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegisterModel, Long> {

    Optional<CashRegisterModel> findByCashRegisterUuid(UUID uuid);
    List<CashRegisterModel> findByOutletId_OutletId(Long outletId);
    List<CashRegisterModel> findByUserId_Id(Long userId);
    List<CashRegisterModel> findByStatus(String status);
    List<CashRegisterModel> findByEnterpriseId_Id(Long enterpriseId);
    @Query("""
        SELECT cr
            FROM CashRegisterModel cr
            WHERE cr.status = 'ABIERTA'
              AND cr.outletId.outletId = :outletId
              AND cr.enterpriseId.id = :enterpriseId
              AND cr.openingDate >= :startDate
              AND cr.openingDate < :endDate
    """)
    Optional<CashRegisterModel> findOpenCashRegisterToday(
            @Param("outletId") Long outletId,
            @Param("enterpriseId") Long enterpriseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}