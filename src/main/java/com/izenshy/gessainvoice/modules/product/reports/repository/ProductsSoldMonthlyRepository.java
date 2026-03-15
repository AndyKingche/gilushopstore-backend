package com.izenshy.gessainvoice.modules.product.reports.repository;

import com.izenshy.gessainvoice.modules.product.reports.model.ProductsSoldMonthlyModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsSoldMonthlyRepository extends JpaRepository<ProductsSoldMonthlyModel, Long> {

    List<ProductsSoldMonthlyModel> findByEnterpriseIdAndOutletId(Long enterpriseId, Long outletId);

    List<ProductsSoldMonthlyModel> findByEnterpriseId(Long enterpriseId);
}