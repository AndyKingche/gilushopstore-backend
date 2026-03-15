package com.izenshy.gessainvoice.modules.product.reports.repository;

import com.izenshy.gessainvoice.modules.product.reports.model.TotalSoldDailyModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TotalSoldDailyRepository extends JpaRepository<TotalSoldDailyModel, Long> {

    List<TotalSoldDailyModel> findByEnterpriseIdAndOutletId(Long enterpriseId, Long outletId);

    List<TotalSoldDailyModel> findByEnterpriseId(Long enterpriseId);
}