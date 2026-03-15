package com.izenshy.gessainvoice.modules.product.reports.repository;

import com.izenshy.gessainvoice.modules.product.reports.model.BestSellingProductsMonthlyModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BestSellingProductsMonthlyRepository extends JpaRepository<BestSellingProductsMonthlyModel, Long> {

    List<BestSellingProductsMonthlyModel> findByEnterpriseIdAndOutletId(Long enterpriseId, Long outletId);

    List<BestSellingProductsMonthlyModel> findByEnterpriseId(Long enterpriseId);
}