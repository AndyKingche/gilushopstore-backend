package com.izenshy.gessainvoice.modules.product.product.repository;

import com.izenshy.gessainvoice.modules.product.product.model.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel, Long> {
    Optional<ProductModel> findByProductCode(String productCode);
    ProductModel getByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
}
