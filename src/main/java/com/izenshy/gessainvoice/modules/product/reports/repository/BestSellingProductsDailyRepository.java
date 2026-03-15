package com.izenshy.gessainvoice.modules.product.reports.repository;

import com.izenshy.gessainvoice.modules.product.reports.model.BestSellingProductsDailyModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BestSellingProductsDailyRepository extends JpaRepository<BestSellingProductsDailyModel, Long> {

    List<BestSellingProductsDailyModel> findByEnterpriseIdAndOutletId(Long enterpriseId, Long outletId);

    List<BestSellingProductsDailyModel> findByEnterpriseId(Long enterpriseId);
}