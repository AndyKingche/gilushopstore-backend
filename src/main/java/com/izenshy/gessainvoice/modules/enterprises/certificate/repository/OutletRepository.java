package com.izenshy.gessainvoice.modules.enterprises.certificate.repository;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutletRepository extends JpaRepository<OutletModel, Long> {

    List<OutletModel> findByEnterpriseId_Id(Long enterpriseId);
    Optional<OutletModel> findByOutletId(Long outletId);
}