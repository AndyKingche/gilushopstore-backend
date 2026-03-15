package com.izenshy.gessainvoice.modules.product.reports.repository;

import com.izenshy.gessainvoice.modules.product.reports.model.TotalSoldMonthlyModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TotalSoldMonthlyRepository extends JpaRepository<TotalSoldMonthlyModel, Long> {

    List<TotalSoldMonthlyModel> findByEnterpriseIdAndOutletId(Long enterpriseId, Long outletId);

    List<TotalSoldMonthlyModel> findByEnterpriseId(Long enterpriseId);
}