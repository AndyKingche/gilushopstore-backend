package com.izenshy.gessainvoice.modules.product.product.repository;

import com.izenshy.gessainvoice.modules.product.product.model.ImageStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImageStockRepository extends JpaRepository<ImageStock, Long> {
    List<ImageStock> findByStock_Id_ProductIdAndStock_Id_OutletId(Long productId, Long outletId);
    boolean existsByImageStockUuid(UUID uuid);
}