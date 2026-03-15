package com.izenshy.gessainvoice.modules.product.stock.repository;

import com.izenshy.gessainvoice.modules.product.stock.dto.ListStockDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.stock.model.StockModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<StockModel, Long> {

    List<StockModel> findByIdProductId(Long productId);
    List<StockModel> findByIdOutletId(Long outletId);
    Optional<StockModel> findByIdProductIdAndIdOutletId(Long productId, Long outletId);
    Optional<StockModel> findByProductId_ProductCodeAndOutletId_OutletId(String productCode, Long outletId);

    @Query("SELECT s FROM StockModel s JOIN s.productId p LEFT JOIN p.categoryId c LEFT JOIN p.detailId d WHERE s.id.outletId = :outletId AND (LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.productDesc) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.detailName) LIKE LOWER(CONCAT('%', :query, '%')) )")
    List<StockModel> searchByQueryAndOutlet(String query, Long outletId);

}
