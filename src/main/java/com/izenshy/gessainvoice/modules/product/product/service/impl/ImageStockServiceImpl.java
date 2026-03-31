package com.izenshy.gessainvoice.modules.product.product.service.impl;

import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.product.product.dto.ImageStockDTO;
import com.izenshy.gessainvoice.modules.product.product.mapper.ImageStockMapper;
import com.izenshy.gessainvoice.modules.product.product.model.ImageStock;
import com.izenshy.gessainvoice.modules.product.product.repository.ImageStockRepository;
import com.izenshy.gessainvoice.modules.product.product.service.ImageStockService;
import com.izenshy.gessainvoice.modules.product.stock.model.StockModel;
import com.izenshy.gessainvoice.modules.product.stock.model.StockPKModel;
import com.izenshy.gessainvoice.modules.product.stock.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ImageStockServiceImpl implements ImageStockService {
    private final ImageStockRepository imageStockRepository;
    private final StockRepository stockRepository;
    private final ImageStockMapper imageStockMapper;

    @Autowired
    public ImageStockServiceImpl(ImageStockRepository imageStockRepository, StockRepository stockRepository, ImageStockMapper imageStockMapper) {
        this.imageStockRepository = imageStockRepository;
        this.stockRepository = stockRepository;
        this.imageStockMapper = imageStockMapper;
    }

    @Override
    public ImageStockDTO createImageStock(ImageStockDTO imageStockDTO) {
        Optional<StockModel> stock = stockRepository.findByIdProductIdAndIdOutletId(imageStockDTO.getStockProductId(), imageStockDTO.getStockOutletId());
        if (stock.isEmpty()) {
            throw new ResourceNotFoundException("Stock not found");
        }
        ImageStock imageStock = imageStockMapper.dtoToModel(imageStockDTO);
        imageStock.setStock(stock.get());
        ImageStock saved = imageStockRepository.save(imageStock);
        return imageStockMapper.modelToDTO(saved);
    }

    @Override
    public ImageStockDTO updateImageStock(Long id, ImageStockDTO imageStockDTO) {
        Optional<ImageStock> existing = imageStockRepository.findById(id);
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("ImageStock not found");
        }
        Optional<StockModel> stock = stockRepository.findByIdProductIdAndIdOutletId(imageStockDTO.getStockProductId(), imageStockDTO.getStockOutletId());
        if (stock.isEmpty()) {
            throw new ResourceNotFoundException("Stock not found");
        }
        ImageStock imageStock = imageStockMapper.dtoToModel(imageStockDTO);
        imageStock.setId(id);
        imageStock.setStock(stock.get());
        ImageStock saved = imageStockRepository.save(imageStock);
        return imageStockMapper.modelToDTO(saved);
    }

    @Override
    public void deleteImageStock(Long id) {
        if (!imageStockRepository.existsById(id)) {
            throw new ResourceNotFoundException("ImageStock not found");
        }
        imageStockRepository.deleteById(id);
    }

    @Override
    public ImageStockDTO getImageStockById(Long id) {
        Optional<ImageStock> imageStock = imageStockRepository.findById(id);
        if (imageStock.isEmpty()) {
            throw new ResourceNotFoundException("ImageStock not found");
        }
        return imageStockMapper.modelToDTO(imageStock.get());
    }

    @Override
    public List<ImageStockDTO> getAllImageStocks() {
        List<ImageStock> imageStocks = imageStockRepository.findAll();
        return imageStocks.stream().map(imageStockMapper::modelToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ImageStockDTO> getImageStocksByStock(Long productId, Long outletId) {
        List<ImageStock> imageStocks = imageStockRepository.findByStock_Id_ProductIdAndStock_Id_OutletId(productId, outletId);
        return imageStocks.stream().map(imageStockMapper::modelToDTO).collect(Collectors.toList());
    }
}