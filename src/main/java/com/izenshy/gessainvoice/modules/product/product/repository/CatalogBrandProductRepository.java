package com.izenshy.gessainvoice.modules.product.product.repository;

import com.izenshy.gessainvoice.modules.product.product.model.CatalogBrandProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogBrandProductRepository extends JpaRepository<CatalogBrandProduct, Long> {
    Optional<CatalogBrandProduct> findByBrand_IdAndProduct_Id(Long brandId, Long productId);
    List<CatalogBrandProduct> findByBrand_Id(Long brandId);
    List<CatalogBrandProduct> findByProduct_Id(Long productId);
    boolean existsByBrand_IdAndProduct_Id(Long brandId, Long productId);
}