package com.izenshy.gessainvoice.modules.product.stock.service.impl;

import com.izenshy.gessainvoice.modules.product.stock.dto.ListStockDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.stock.dto.OnlineStoreProductDTO;
import com.izenshy.gessainvoice.modules.product.stock.dto.StockDTO;
import com.izenshy.gessainvoice.modules.product.stock.dto.StockDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.stock.mapper.StockMapper;
import com.izenshy.gessainvoice.modules.product.stock.model.StockModel;
import com.izenshy.gessainvoice.modules.product.stock.model.StockPKModel;
import com.izenshy.gessainvoice.modules.product.stock.repository.StockRepository;
import com.izenshy.gessainvoice.modules.product.stock.service.StockService;
import com.izenshy.gessainvoice.modules.product.product.model.CategoryModel;
import com.izenshy.gessainvoice.modules.product.product.model.DetailModel;
import com.izenshy.gessainvoice.modules.product.product.model.ProductModel;
import com.izenshy.gessainvoice.modules.product.product.repository.CategoryRepository;
import com.izenshy.gessainvoice.modules.product.product.repository.DetailRepository;
import com.izenshy.gessainvoice.modules.product.product.repository.ProductRepository;
import com.izenshy.gessainvoice.modules.product.product.service.CategoryService;
import com.izenshy.gessainvoice.modules.product.product.service.DetailService;
import com.izenshy.gessainvoice.modules.product.product.service.ProductService;
import com.izenshy.gessainvoice.modules.product.product.service.TaxService;
import com.izenshy.gessainvoice.modules.product.product.model.TaxModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.OutletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {
    private final StockRepository stockRepository;
    private final StockMapper stockMapper;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DetailRepository detailRepository;
    private final OutletRepository outletRepository;
    private final TaxService taxService;

    @Autowired
    public StockServiceImpl(StockRepository stockRepository, StockMapper stockMapper,
                            ProductRepository productRepository, CategoryRepository categoryRepository,
                            DetailRepository detailRepository, OutletRepository outletRepository,
                            TaxService taxService) {
        this.stockRepository = stockRepository;
        this.stockMapper = stockMapper;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.detailRepository = detailRepository;
        this.outletRepository = outletRepository;
        this.taxService = taxService;
    }

    @Override
    public StockModel save(StockModel stock) {
        return stockRepository.save(stock);
    }

    @Override
    public Optional<StockModel> findByProductAndOutlet(Long productId, Long outletId) {
        return stockRepository.findByIdProductIdAndIdOutletId(productId, outletId);
    }

    @Override
    public Optional<StockModel> findByProductCodeAndOutletId(String productCode, Long outletId) {
        return stockRepository.findByProductId_ProductCodeAndOutletId_OutletId(productCode, outletId);
    }

    @Override
    public List<StockModel> findByProduct(Long productId) {
        return stockRepository.findByIdProductId(productId);
    }

    @Override
    public List<StockModel> findByOutlet(Long outletId) {
        return stockRepository.findByIdOutletId(outletId);
    }

    @Override
    public List<StockModel> findAll() {
        return stockRepository.findAll();
    }

    @Override
    public void deleteByProductAndOutlet(Long productId, Long outletId) {
        stockRepository.findByIdProductIdAndIdOutletId(productId,outletId)
                .ifPresent(stock -> {
                    stock.setStockAvalible(false);
                    stockRepository.save(stock);
        });
                
    }

    @Override
    public StockDTO createOrUpdate(StockDTO dto) {
        StockModel model = stockMapper.dtoToModel(dto);
        StockModel saved = stockRepository.save(model);
        return stockMapper.modelToDTO(saved);
    }

    @Override
    public StockDTO getByProductAndOutlet(Long productId, Long outletId) {
        return stockRepository.findByIdProductIdAndIdOutletId(productId, outletId)
                .map(stockMapper::modelToDTO)
                .orElseThrow(() -> new RuntimeException("Stock not found"));
    }

    @Override
    public List<StockDTO> getByProduct(Long productId) {
        return stockMapper.modelsToDTOs(stockRepository.findByIdProductId(productId));
    }

    @Override
    public List<StockDTO> getByOutlet(Long outletId) {
        return stockMapper.modelsToDTOs(stockRepository.findByIdOutletId(outletId));
    }

    @Override
    public List<StockDTO> getAll() {
        return stockMapper.modelsToDTOs(stockRepository.findAll());
    }

    @Override
    public StockDeluxeDTO getDeluxe(Long productId, Long outletId) {
        return stockRepository.findByIdProductIdAndIdOutletId(productId, outletId)
                .map(stockMapper::modelToDeluxeDTO)
                .orElseThrow(() -> new RuntimeException("Stock not found"));
    }

    @Override
    public ListStockDeluxeDTO getAllDeluxe() {
        List<StockDeluxeDTO> deluxeList = stockMapper.modelsToDeluxeDTOs(stockRepository.findAll());
        ListStockDeluxeDTO response = new ListStockDeluxeDTO();
        response.setListProduct(deluxeList);
        return response;
    }

    @Override
    public ListStockDeluxeDTO getAllDeluxeOuletId(Long outletId) {

        List<StockDeluxeDTO> list = stockRepository.findByIdOutletId(outletId).stream().map(stock -> {
            StockDeluxeDTO dto = new StockDeluxeDTO();
            dto.setProductId(stock.getId().getProductId());
            dto.setOutletId(stock.getId().getOutletId());
            dto.setStockQuantity(stock.getStockQuantity());
            dto.setStockAvalible(stock.getStockAvalible());
            dto.setUnitPrice(stock.getUnit_price());
            dto.setPvpPrice(stock.getPvp_price());
            dto.setStockMax(stock.getStockMax());
            dto.setStockMin(stock.getStockMin());
            dto.setApplyTax(stock.getApply_tax());
            dto.setTaxCode(stock.getIvaId() != null ? stock.getIvaId().getTaxCode() : null);
            if (stock.getProductId() != null) {
                dto.setProductName(stock.getProductId().getProductName());
                dto.setProductCode(stock.getProductId().getProductCode());
                dto.setProductDesc(stock.getProductId().getProductDesc());
                if (stock.getProductId().getCategoryId() != null) {
                    dto.setCategoryName(stock.getProductId().getCategoryId().getCategoryName());
                }
                if (stock.getProductId().getDetailId() != null) {
                    dto.setDetailName(stock.getProductId().getDetailId().getDetailName());
                }
            }
            return dto;
        }).collect(Collectors.toList());

        ListStockDeluxeDTO response = new ListStockDeluxeDTO();
        response.setListProduct(list);
        return response;
    }

    @Override
    public ListStockDeluxeDTO searchDeluxe(String query, Long outletId) {
        List<StockDeluxeDTO> list = stockRepository.searchByQueryAndOutlet(query, outletId).stream().map(stock -> {
            StockDeluxeDTO dto = new StockDeluxeDTO();
            dto.setProductId(stock.getId().getProductId());
            dto.setOutletId(stock.getId().getOutletId());
            dto.setStockQuantity(stock.getStockQuantity());
            dto.setStockAvalible(stock.getStockAvalible());
            dto.setUnitPrice(stock.getUnit_price());
            dto.setPvpPrice(stock.getPvp_price());
            dto.setStockMax(stock.getStockMax());
            dto.setStockMin(stock.getStockMin());
            dto.setApplyTax(stock.getApply_tax());
            dto.setTaxCode(String.valueOf(stock.getIvaId().getId()));
            if (stock.getProductId() != null) {
                dto.setProductName(stock.getProductId().getProductName());
                dto.setProductCode(stock.getProductId().getProductCode());
                dto.setProductDesc(stock.getProductId().getProductDesc());
                if (stock.getProductId().getCategoryId() != null) {
                    dto.setCategoryName(stock.getProductId().getCategoryId().getCategoryName());
                }
                if (stock.getProductId().getDetailId() != null) {
                    dto.setDetailName(stock.getProductId().getDetailId().getDetailName());
                }
            }
            return dto;
        }).collect(Collectors.toList());

        ListStockDeluxeDTO response = new ListStockDeluxeDTO();
        response.setListProduct(list);
        return response;
    }

    @Override
    public void uploadStockList(List<StockDeluxeDTO> stockList, Long outletId) {
        OutletModel outlet = outletRepository.findById(outletId)
                .orElseThrow(() -> new RuntimeException("Outlet not found with id: " + outletId));

        for (StockDeluxeDTO stockDTO : stockList) {
            // Check or create category
            CategoryModel category = null;
            if (stockDTO.getCategoryName() != null && !stockDTO.getCategoryName().isEmpty()) {
                category = categoryRepository.findByCategoryNameIgnoreCase(stockDTO.getCategoryName())
                        .orElseGet(() -> {
                            CategoryModel newCategory = new CategoryModel();
                            newCategory.setCategoryName(stockDTO.getCategoryName());
                            return categoryRepository.save(newCategory);
                        });
            }

            // Check or create detail
            DetailModel detail = null;
            if (stockDTO.getDetailName() != null && !stockDTO.getDetailName().isEmpty()) {
                detail = detailRepository.findByDetailNameIgnoreCase(stockDTO.getDetailName())
                        .orElseGet(() -> {
                            DetailModel newDetail = new DetailModel();
                            newDetail.setDetailName(stockDTO.getDetailName());
                            return detailRepository.save(newDetail);
                        });
            }

            // Check or create product
            final CategoryModel finalCategory = category;
            final DetailModel finalDetail = detail;
            ProductModel product = productRepository.findByProductCode(stockDTO.getProductCode())
                    .orElseGet(() -> {
                        ProductModel newProduct = new ProductModel();
                        newProduct.setProductName(stockDTO.getProductName());
                        newProduct.setProductCode(stockDTO.getProductCode());
                        newProduct.setProductDesc(stockDTO.getProductDesc());
                        newProduct.setCategoryId(finalCategory);
                        newProduct.setDetailId(finalDetail);
                        return productRepository.save(newProduct);
                    });

            // Check if stock already exists
            Optional<StockModel> existingStockOpt = stockRepository.findByIdProductIdAndIdOutletId(product.getId(), outletId);
            StockModel stock;
            if (existingStockOpt.isPresent()) {
                // Update existing stock by adding quantity
                stock = existingStockOpt.get();
                stock.setStockQuantity(stock.getStockQuantity() + stockDTO.getStockQuantity());
                // Update other fields if provided
                if (stockDTO.getStockAvalible() != null) {
                    stock.setStockAvalible(stockDTO.getStockAvalible());
                }
                if (stockDTO.getUnitPrice() > 0) {
                    stock.setUnit_price(stockDTO.getUnitPrice());
                }
                if (stockDTO.getPvpPrice() > 0) {
                    stock.setPvp_price(stockDTO.getPvpPrice());
                }
                if (stockDTO.getStockMax() > 0) {
                    stock.setStockMax(stockDTO.getStockMax());
                }
                if (stockDTO.getStockMin() >= 0) {
                    stock.setStockMin(stockDTO.getStockMin());
                }
                if (stockDTO.getApplyTax() != null) {
                    stock.setApply_tax(stockDTO.getApplyTax());
                }
                if (stockDTO.getTaxCode() != null && !stockDTO.getTaxCode().isEmpty()) {
                    var taxOpt = taxService.findByTaxCode(stockDTO.getTaxCode());
                    if (taxOpt.isPresent()) {
                        TaxModel taxRef = new TaxModel();
                        taxRef.setId(taxOpt.get().getId());
                        stock.setIvaId(taxRef);
                    }
                }
            } else {
                // Create new stock
                stock = new StockModel();
                stock.getId().setProductId(product.getId());
                stock.getId().setOutletId(outletId);
                stock.setProductId(product);
                stock.setOutletId(outlet);
                stock.setStockQuantity(stockDTO.getStockQuantity());
                stock.setStockAvalible(stockDTO.getStockAvalible() != null ? stockDTO.getStockAvalible() : true);
                stock.setUnit_price(stockDTO.getUnitPrice());
                stock.setPvp_price(stockDTO.getPvpPrice());
                stock.setStockMax(stockDTO.getStockMax());
                stock.setStockMin(stockDTO.getStockMin());
                stock.setApply_tax(stockDTO.getApplyTax() != null ? stockDTO.getApplyTax() : false);
                if (stockDTO.getTaxCode() != null && !stockDTO.getTaxCode().isEmpty()) {
                    var taxOpt = taxService.findByTaxCode(stockDTO.getTaxCode());
                    if (taxOpt.isPresent()) {
                        TaxModel taxRef = new TaxModel();
                        taxRef.setId(taxOpt.get().getId());
                        stock.setIvaId(taxRef);
                    }
                }
            }

            stockRepository.save(stock);
        }
    }

    @Override
    public List<OnlineStoreProductDTO> getOnlineStoreProducts(Long outletId, int pageSize, int offset) {
        List<Object[]> results = stockRepository.findOnlineStoreProductsByOutletId(outletId, pageSize, offset);
        return results.stream()
                .map(row -> new OnlineStoreProductDTO(
                        (String) row[0],  // id
                        (String) row[1],  // name
                        (String) row[2],  // category
                        (String) row[3],  // brand
                        ((Number) row[4]).doubleValue(),  // price
                        (String) row[5],  // description
                        (String) row[6],  // image
                        (Boolean) row[7]  // inStock
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Long getOnlineStoreProductsCount(Long outletId) {
        return stockRepository.countOnlineStoreProductsByOutletId(outletId);
    }
}
