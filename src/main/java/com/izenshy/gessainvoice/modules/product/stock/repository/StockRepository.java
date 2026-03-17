package com.izenshy.gessainvoice.modules.product.stock.repository;

import com.izenshy.gessainvoice.modules.product.stock.dto.ListStockDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.stock.dto.OnlineStoreProductDTO;
import com.izenshy.gessainvoice.modules.product.stock.model.StockModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "SELECT " +
            "p.product_id::text AS id, " +
            "p.product_name AS name, " +
            "c.category_name AS category, " +
            "cb.brand_name AS brand, " +
            "s.pvp_price AS price, " +
            "p.product_description AS description, " +
            "img.image_url AS image, " +
            "CASE WHEN s.stock_quantity > 0 AND s.stock_avalible THEN true ELSE false END AS inStock " +
            "FROM public.stock s " +
            "INNER JOIN public.products p ON p.product_id = s.stock_product_id " +
            "LEFT JOIN public.categories c ON p.category_id = c.category_id " +
            "LEFT JOIN public.catalog_brand_product cbp ON cbp.product_id = p.product_id " +
            "LEFT JOIN public.catalog_brand cb ON cb.brand_id = cbp.brand_id " +
            "LEFT JOIN public.image_stock img ON img.stock_product_id = s.stock_product_id AND img.stock_outlet_id = s.stock_outlet_id AND img.is_primary = true " +
            "WHERE s.stock_outlet_id = :outletId " +
            "AND s.stock_avalible = true " +
            "AND UPPER(c.category_name) != 'ROPA' " +
            "AND UPPER(p.product_name) != 'CAMISETA NEON' " +
            "AND UPPER(c.category_name) != 'SIN DEFINICION' " +
            "ORDER BY p.product_name ASC " +
            "LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<Object[]> findOnlineStoreProductsByOutletId(@Param("outletId") Long outletId, @Param("pageSize") int pageSize, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) " +
            "FROM public.stock s " +
            "INNER JOIN public.products p ON p.product_id = s.stock_product_id " +
            "LEFT JOIN public.categories c ON p.category_id = c.category_id " +
            "WHERE s.stock_outlet_id = :outletId " +
            "AND s.stock_avalible = true " +
            "AND UPPER(c.category_name) != 'ROPA' " +
            "AND UPPER(p.product_name) != 'CAMISETA NEON' " +
            "AND UPPER(c.category_name) != 'SIN DEFINICION'", nativeQuery = true)
    Long countOnlineStoreProductsByOutletId(@Param("outletId") Long outletId);

    @Query(value = "SELECT " +
            "p.product_id::text AS id, " +
            "p.product_name AS name, " +
            "c.category_name AS category, " +
            "cb.brand_name AS brand, " +
            "s.pvp_price AS price, " +
            "p.product_description AS description, " +
            "img.image_url AS image, " +
            "CASE WHEN s.stock_quantity > 0 AND s.stock_avalible THEN true ELSE false END AS inStock " +
            "FROM public.stock s " +
            "INNER JOIN public.products p ON p.product_id = s.stock_product_id " +
            "LEFT JOIN public.categories c ON p.category_id = c.category_id " +
            "LEFT JOIN public.catalog_brand_product cbp ON cbp.product_id = p.product_id " +
            "LEFT JOIN public.catalog_brand cb ON cb.brand_id = cbp.brand_id " +
            "LEFT JOIN public.image_stock img ON img.stock_product_id = s.stock_product_id AND img.stock_outlet_id = s.stock_outlet_id AND img.is_primary = true " +
            "WHERE s.stock_outlet_id = :outletId AND c.category_id = :categoryId " +
            "AND s.stock_avalible = true " +
            "AND UPPER(c.category_name) != 'ROPA' " +
            "AND UPPER(p.product_name) != 'CAMISETA NEON' " +
            "AND UPPER(c.category_name) != 'SIN DEFINICION' " +
            "ORDER BY p.product_name ASC " +
            "LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<Object[]> findOnlineStoreProductsByOutletIdAndCategoryId(@Param("outletId") Long outletId, @Param("categoryId") Long categoryId, @Param("pageSize") int pageSize, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) " +
            "FROM public.stock s " +
            "INNER JOIN public.products p ON p.product_id = s.stock_product_id " +
            "LEFT JOIN public.categories c ON p.category_id = c.category_id " +
            "WHERE s.stock_outlet_id = :outletId " +
            "AND s.stock_avalible = true " +
            "AND c.category_id = :categoryId " +
            "AND UPPER(c.category_name) != 'ROPA' " +
            "AND UPPER(p.product_name) != 'CAMISETA NEON' " +
            "AND UPPER(c.category_name) != 'SIN DEFINICION'", nativeQuery = true)
    Long countOnlineStoreProductsByOutletIdAndCategoryId(@Param("outletId") Long outletId, @Param("categoryId") Long categoryId);

    @Query(value = "SELECT " +
            "p.product_id::text AS id, " +
            "p.product_name AS name, " +
            "c.category_name AS category, " +
            "cb.brand_name AS brand, " +
            "s.pvp_price AS price, " +
            "p.product_description AS description, " +
            "img.image_url AS image, " +
            "CASE WHEN s.stock_quantity > 0 AND s.stock_avalible THEN true ELSE false END AS inStock " +
            "FROM public.stock s " +
            "INNER JOIN public.products p ON p.product_id = s.stock_product_id " +
            "LEFT JOIN public.categories c ON p.category_id = c.category_id " +
            "LEFT JOIN public.catalog_brand_product cbp ON cbp.product_id = p.product_id " +
            "LEFT JOIN public.catalog_brand cb ON cb.brand_id = cbp.brand_id " +
            "LEFT JOIN public.image_stock img ON img.stock_product_id = s.stock_product_id AND img.stock_outlet_id = s.stock_outlet_id AND img.is_primary = true " +
            "WHERE s.stock_outlet_id = :outletId " +
            "AND LOWER(p.product_name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND s.stock_avalible = true " +
            "AND UPPER(c.category_name) != 'ROPA' " +
            "AND UPPER(p.product_name) != 'CAMISETA NEON' " +
            "AND UPPER(c.category_name) != 'SIN DEFINICION' " +
            "ORDER BY p.product_name ASC " +
            "LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<Object[]> findOnlineStoreProductsByOutletIdAndName(@Param("outletId") Long outletId, @Param("name") String name, @Param("pageSize") int pageSize, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) " +
            "FROM public.stock s " +
            "INNER JOIN public.products p ON p.product_id = s.stock_product_id " +
            "LEFT JOIN public.categories c ON p.category_id = c.category_id " +
            "WHERE s.stock_outlet_id = :outletId " +
            "AND LOWER(p.product_name) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "AND s.stock_avalible = true " +
            "AND UPPER(c.category_name) != 'ROPA' " +
            "AND UPPER(p.product_name) != 'CAMISETA NEON' " +
            "AND UPPER(c.category_name) != 'SIN DEFINICION'", nativeQuery = true)
    Long countOnlineStoreProductsByOutletIdAndName(@Param("outletId") Long outletId, @Param("name") String name);

    @Query(value = "SELECT " +
            "p.product_id::text AS id, " +
            "p.product_name AS name, " +
            "c.category_name AS category, " +
            "cb.brand_name AS brand, " +
            "s.pvp_price AS price, " +
            "p.product_description AS description, " +
            "img.image_url AS image, " +
            "CASE WHEN s.stock_quantity > 0 AND s.stock_avalible THEN true ELSE false END AS inStock " +
            "FROM public.stock s " +
            "INNER JOIN public.products p ON p.product_id = s.stock_product_id " +
            "LEFT JOIN public.categories c ON p.category_id = c.category_id " +
            "LEFT JOIN public.catalog_brand_product cbp ON cbp.product_id = p.product_id " +
            "LEFT JOIN public.catalog_brand cb ON cb.brand_id = cbp.brand_id " +
            "LEFT JOIN public.image_stock img ON img.stock_product_id = s.stock_product_id AND img.stock_outlet_id = s.stock_outlet_id AND img.is_primary = true " +
            "WHERE s.stock_outlet_id = :outletId AND cb.brand_id = :brandId " +
            "AND s.stock_avalible = true " +
            "AND UPPER(c.category_name) != 'ROPA' " +
            "AND UPPER(p.product_name) != 'CAMISETA NEON' " +
            "AND UPPER(c.category_name) != 'SIN DEFINICION' " +
            "ORDER BY p.product_name ASC " +
            "LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<Object[]> findOnlineStoreProductsByOutletIdAndBrandId(@Param("outletId") Long outletId, @Param("brandId") Long brandId, @Param("pageSize") int pageSize, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) " +
            "FROM public.stock s " +
            "INNER JOIN public.products p ON p.product_id = s.stock_product_id " +
            "LEFT JOIN public.categories c ON p.category_id = c.category_id " +
            "LEFT JOIN public.catalog_brand_product cbp ON cbp.product_id = p.product_id " +
            "LEFT JOIN public.catalog_brand cb ON cb.brand_id = cbp.brand_id " +
            "WHERE s.stock_outlet_id = :outletId " +
            "AND s.stock_avalible = true " +
            "AND cb.brand_id = :brandId " +
            "AND UPPER(c.category_name) != 'ROPA' " +
            "AND UPPER(p.product_name) != 'CAMISETA NEON' " +
            "AND UPPER(c.category_name) != 'SIN DEFINICION'", nativeQuery = true)
    Long countOnlineStoreProductsByOutletIdAndBrandId(@Param("outletId") Long outletId, @Param("brandId") Long brandId);

}
