package com.izenshy.gessainvoice.modules.product.product.service;

import com.izenshy.gessainvoice.modules.product.product.dto.ImageStockDTO;

import java.util.List;
import java.util.UUID;

public interface ImageStockService {
    ImageStockDTO createImageStock(ImageStockDTO imageStockDTO);
    ImageStockDTO updateImageStock(Long id, ImageStockDTO imageStockDTO);
    void deleteImageStock(Long id);
    ImageStockDTO getImageStockById(Long id);
    List<ImageStockDTO> getAllImageStocks();
    List<ImageStockDTO> getImageStocksByStock(Long productId, Long outletId);
}