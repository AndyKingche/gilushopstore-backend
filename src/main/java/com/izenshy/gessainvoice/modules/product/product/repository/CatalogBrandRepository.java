package com.izenshy.gessainvoice.modules.product.product.repository;

import com.izenshy.gessainvoice.modules.product.product.model.CatalogBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CatalogBrandRepository extends JpaRepository<CatalogBrand, Long> {
    Optional<CatalogBrand> findByBrandName(String brandName);
    boolean existsByBrandName(String brandName);
    boolean existsByBrandUuid(UUID uuid);
}