package com.izenshy.gessainvoice.modules.product.stock.service;

import com.izenshy.gessainvoice.modules.product.stock.dto.ListStockDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.stock.dto.StockDTO;
import com.izenshy.gessainvoice.modules.product.stock.dto.StockDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.stock.model.StockModel;

import java.util.List;
import java.util.Optional;

public interface StockService {

    StockModel save(StockModel stock);
    Optional<StockModel> findByProductAndOutlet(Long productId, Long outletId);
    Optional<StockModel> findByProductCodeAndOutletId(String productCode, Long outletId);
    List<StockModel> findByProduct(Long productId);
    List<StockModel> findByOutlet(Long outletId);
    List<StockModel> findAll();
    void deleteByProductAndOutlet(Long productId, Long outletId);
    StockDTO createOrUpdate(StockDTO dto);
    StockDTO getByProductAndOutlet(Long productId, Long outletId);
    List<StockDTO> getByProduct(Long productId);
    List<StockDTO> getByOutlet(Long outletId);
    List<StockDTO> getAll();
    StockDeluxeDTO getDeluxe(Long productId, Long outletId);
    ListStockDeluxeDTO getAllDeluxe();
    ListStockDeluxeDTO getAllDeluxeOuletId(Long outletId);
    ListStockDeluxeDTO searchDeluxe(String query, Long outletId);
    void uploadStockList(List<StockDeluxeDTO> stockList, Long outletId);

}
