package com.izenshy.gessainvoice.modules.product.reports.repository;

import com.izenshy.gessainvoice.modules.product.reports.model.ProductsAtMinimumModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsAtMinimumRepository extends JpaRepository<ProductsAtMinimumModel, Long> {

    List<ProductsAtMinimumModel> findByOutletId(Long outletId);

    List<ProductsAtMinimumModel> findAll();
}