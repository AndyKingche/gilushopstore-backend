package com.izenshy.gessainvoice.modules.product.reports.repository;

import com.izenshy.gessainvoice.modules.product.reports.model.BestSellingProductsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BestSellingProductsRepository extends JpaRepository<BestSellingProductsModel, Long> {

    List<BestSellingProductsModel> findByEnterpriseIdAndOutletId(Long enterpriseId, Long outletId);

    List<BestSellingProductsModel> findByEnterpriseId(Long enterpriseId);
}