package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.common.response.GessaApiResponse;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import com.izenshy.gessainvoice.modules.product.product.model.ProductModel;
import com.izenshy.gessainvoice.modules.product.product.model.TaxModel;
import com.izenshy.gessainvoice.modules.product.stock.dto.ListStockDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.stock.dto.StockDTO;
import com.izenshy.gessainvoice.modules.product.stock.dto.StockDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.stock.dto.StockResponseDTO;
import com.izenshy.gessainvoice.modules.product.stock.mapper.StockMapper;
import com.izenshy.gessainvoice.modules.product.stock.model.StockModel;
import com.izenshy.gessainvoice.modules.product.stock.service.StockService;
import com.izenshy.gessainvoice.modules.product.product.service.TaxService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/gessa/stock")
@Tag(name = "Stock", description = "Esta sección es dedicada a las operaciones relacionadas con el Stock")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class StockController {
    private final StockService stockService;
    private final TaxService taxService;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    public StockController(StockService stockService, TaxService taxService) {
        this.stockService = stockService;
        this.taxService = taxService;
    }

    // Crear o actualizar un stock
    @PostMapping
    public ResponseEntity<StockDTO> createOrUpdateStock(@RequestBody StockDTO stockDTO) {
        StockModel stock = new StockModel();

        // Clave compuesta
        stock.getId().setProductId(stockDTO.getProductId());
        stock.getId().setOutletId(stockDTO.getOutletId());

        // ⚡ Muy importante: setear las relaciones como referencias
        ProductModel productRef = new ProductModel();
        productRef.setId(stockDTO.getProductId());
        stock.setProductId(productRef);

        OutletModel outletRef = new OutletModel();
        outletRef.setOutletId(stockDTO.getOutletId());
        stock.setOutletId(outletRef);

        TaxModel taxModel = new TaxModel();
        taxModel.setId(stockDTO.getIvaId());
        stock.setIvaId(taxModel);
        // Otros campos
        stock.setStockQuantity(stockDTO.getStockQuantity());
        stock.setStockAvalible(stockDTO.getStockAvalible());
        stock.setUnit_price(stockDTO.getUnitPrice());
        stock.setPvp_price(stockDTO.getPvpPrice());
        stock.setStockMax(stockDTO.getStockMax());
        stock.setStockMin(stockDTO.getStockMin());
        stock.setApply_tax(stockDTO.getApplyTax());

        StockModel saved = stockService.save(stock);

        StockDTO response = new StockDTO();
        response.setProductId(saved.getId().getProductId());
        response.setOutletId(saved.getId().getOutletId());
        response.setStockQuantity(saved.getStockQuantity());
        response.setStockAvalible(saved.getStockAvalible());
        response.setUnitPrice(saved.getUnit_price());
        response.setPvpPrice(saved.getPvp_price());
        response.setStockMax(saved.getStockMax());
        response.setStockMin(saved.getStockMin());
        response.setApplyTax(saved.getApply_tax());

        return ResponseEntity.ok(response);
    }

    // Obtener un stock por producto y outlet
    @GetMapping("/{productId}/{outletId}")
    public ResponseEntity<StockDTO> getStock(
            @PathVariable Long productId,
            @PathVariable Long outletId) {

        Optional<StockModel> stockOpt = stockService.findByProductAndOutlet(productId, outletId);

        return stockOpt.map(stock -> {
            StockDTO dto = new StockDTO();
            dto.setProductId(stock.getId().getProductId());
            dto.setOutletId(stock.getId().getOutletId());
            dto.setStockQuantity(stock.getStockQuantity());
            dto.setStockAvalible(stock.getStockAvalible());
            dto.setUnitPrice(stock.getUnit_price());
            dto.setPvpPrice(stock.getPvp_price());
            dto.setStockMax(stock.getStockMax());
            dto.setStockMin(stock.getStockMin());
            dto.setApplyTax(stock.getApply_tax());
            dto.setIvaId(stock.getIvaId() != null ? stock.getIvaId().getId() : null);
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Obtener un stock por código de producto y outlet
    @GetMapping("/by-code/{productCode}/{outletId}")
    public ResponseEntity<StockDTO> getStockByCode(
            @PathVariable String productCode,
            @PathVariable Long outletId) {

        Optional<StockModel> stockOpt = stockService.findByProductCodeAndOutletId(productCode, outletId);

        return stockOpt.map(stock -> {
            StockDTO dto = new StockDTO();
            dto.setProductId(stock.getId().getProductId());
            dto.setOutletId(stock.getId().getOutletId());
            dto.setStockQuantity(stock.getStockQuantity());
            dto.setStockAvalible(stock.getStockAvalible());
            dto.setUnitPrice(stock.getUnit_price());
            dto.setPvpPrice(stock.getPvp_price());
            dto.setStockMax(stock.getStockMax());
            dto.setStockMin(stock.getStockMin());
            dto.setApplyTax(stock.getApply_tax());
            dto.setIvaId(stock.getIvaId() != null ? stock.getIvaId().getId() : null);
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-code-aux/{productCode}/{outletId}")
    public ResponseEntity<StockResponseDTO> getStockByCodeAux(
            @PathVariable String productCode,
            @PathVariable Long outletId) {

        Optional<StockModel> stockOpt = stockService.findByProductCodeAndOutletId(productCode, outletId);

        if (stockOpt.isPresent()) {
            StockResponseDTO response = stockMapper.modelToResponseDTO(stockOpt.get());
            if (response.getIvaId() != null) {
                try {
                    var taxResponse = taxService.getTaxById(response.getIvaId());
                    response.setTaxValue(taxResponse.getTaxValue());
                    response.setCodeSri(taxResponse.getCodeSri());
                } catch (Exception e) {
                    // Handle if tax not found, perhaps set to null
                }
            }

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Obtener stock deluxe por producto y outlet
    @GetMapping("/deluxe/{productId}/{outletId}")
    public ResponseEntity<StockDeluxeDTO> getStockDeluxe(
            @PathVariable Long productId,
            @PathVariable Long outletId) {

        StockDeluxeDTO deluxe = stockService.getDeluxe(productId, outletId);
        return ResponseEntity.ok(deluxe);
    }

    // Listar todos los stocks
    @GetMapping
    public ResponseEntity<ListStockDeluxeDTO> getAllStocks() {
        List<StockDeluxeDTO> deluxeList = stockService.findAll().stream().map(stock -> {
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
        response.setListProduct(deluxeList);
        return ResponseEntity.ok(response);
    }

    // Listar todos los stocks
    @GetMapping("deluxe-list/{outletId}")
    public ResponseEntity<ListStockDeluxeDTO> getAllStocksOulet(@PathVariable Long outletId) {
        ListStockDeluxeDTO deluxeList = stockService.getAllDeluxeOuletId(outletId);
        return ResponseEntity.ok(deluxeList);
    }

    // Buscar stocks por query y outlet
    @GetMapping("/search")
    public ResponseEntity<ListStockDeluxeDTO> searchStocks(
            @RequestParam String query,
            @RequestParam Long outletId) {
        ListStockDeluxeDTO searchResult = stockService.searchDeluxe(query, outletId);
        return ResponseEntity.ok(searchResult);
    }

    // Eliminar (desactivar stock)
    @DeleteMapping("/{productId}/{outletId}")
    public ResponseEntity<Void> deleteStock(
            @PathVariable Long productId,
            @PathVariable Long outletId) {
        stockService.deleteByProductAndOutlet(productId, outletId);
        return ResponseEntity.noContent().build();
    }

    // Subir archivo Excel con lista de stock
    @PostMapping("/upload")
    public ResponseEntity<GessaApiResponse> uploadStockFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("outletId") Long outletId) {
        try {
            List<StockDeluxeDTO> stockList = parseExcelFile(file);
            stockService.uploadStockList(stockList, outletId);

            GessaApiResponse response = new GessaApiResponse();
            response.setMessage("Stock uploaded successfully");
            response.setSuccess(true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GessaApiResponse responseError = new GessaApiResponse();
            responseError.setMessage("Error uploading stock: \" + e.getMessage()");
            responseError.setSuccess(false);
            return ResponseEntity.badRequest().body(responseError);
        }
    }

    @PostMapping("/create-list-stock/{outletId}")
    public ResponseEntity<GessaApiResponse> uploadStockList(
            @RequestBody List<StockDeluxeDTO> listStock,@PathVariable Long outletId
            ) {
        try {

            stockService.uploadStockList(listStock, outletId);

            GessaApiResponse response = new GessaApiResponse();
            response.setMessage("Stock uploaded successfully");
            response.setSuccess(true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GessaApiResponse responseError = new GessaApiResponse();
            responseError.setMessage("Error uploading stock: \" + e.getMessage()");
            responseError.setSuccess(false);
            return ResponseEntity.badRequest().body(responseError);
        }
    }

    private List<StockDeluxeDTO> parseExcelFile(MultipartFile file) throws IOException {
        List<StockDeluxeDTO> stockList = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                StockDeluxeDTO dto = new StockDeluxeDTO();

                // productName(0), productCode(1), productDesc(2), categoryName(3), detailName(4),
                // stockQuantity(5), stockAvalible(6), unitPrice(7), pvpPrice(8), stockMax(9), stockMin(10), applyTax(11), ivaId(12)
                dto.setProductName(getCellValueAsString(row.getCell(0)));
                dto.setProductCode(getCellValueAsString(row.getCell(1)));
                dto.setProductDesc(getCellValueAsString(row.getCell(2)));
                dto.setCategoryName(getCellValueAsString(row.getCell(3)));
                dto.setDetailName(getCellValueAsString(row.getCell(4)));
                Cell qtyCell = row.getCell(5);
                if (qtyCell != null && qtyCell.getCellType() == CellType.NUMERIC) {
                    dto.setStockQuantity((float) qtyCell.getNumericCellValue());
                }

                dto.setStockAvalible(getCellValueAsBoolean(row.getCell(6)));

                Cell unitCell = row.getCell(7);
                if (unitCell != null && unitCell.getCellType() == CellType.NUMERIC) {
                    dto.setUnitPrice((float) unitCell.getNumericCellValue());
                }
                Cell pvpCell = row.getCell(8);
                if (pvpCell != null && pvpCell.getCellType() == CellType.NUMERIC) {
                    dto.setPvpPrice((float) pvpCell.getNumericCellValue());
                }
                Cell maxCell = row.getCell(9);
                if (maxCell != null && maxCell.getCellType() == CellType.NUMERIC) {
                    dto.setStockMax((int) maxCell.getNumericCellValue());
                }
                Cell minCell = row.getCell(10);
                if (minCell != null && minCell.getCellType() == CellType.NUMERIC) {
                    dto.setStockMin((int) minCell.getNumericCellValue());
                }

                Cell taxValue = row.getCell(11);
                if (taxValue != null && taxValue.getCellType() == CellType.NUMERIC) {
                    if((int)taxValue.getNumericCellValue()==0){
                        dto.setApplyTax(false);
                        dto.setTaxCode(getCellValueAsString(row.getCell(11)));
                    }else{
                        dto.setApplyTax(true);
                        dto.setTaxCode(getCellValueAsString(row.getCell(11)));
                    }
                }
                //dto.setApplyTax(getCellValueAsBoolean(row.getCell(11)));

                //dto.setTaxCode(getCellValueAsString(row.getCell(12)));

                stockList.add(dto);
            }
        }
        return stockList;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Boolean getCellValueAsBoolean(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> Boolean.parseBoolean(cell.getStringCellValue());
            default -> null;
        };
    }
}