package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.modules.product.reports.dto.*;
import com.izenshy.gessainvoice.modules.product.reports.model.*;
import com.izenshy.gessainvoice.modules.product.reports.service.ReportsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/gessa/reports")
@Tag(name = "Reports", description = "Esta sección es dedicada a los reportes de productos")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class ReportsController {

    private final ReportsService reportsService;

    @Autowired
    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    // Productos más vendidos en general
    @GetMapping("/best-selling-products/{enterpriseId}/{outletId}")
    public ResponseEntity<List<BestSellingProductsModel>> getBestSellingProducts(
            @PathVariable Long enterpriseId,
            @PathVariable Long outletId) {
        List<BestSellingProductsModel> result = reportsService.getBestSellingProducts(enterpriseId, outletId);
        return ResponseEntity.ok(result);
    }

    // Productos más vendidos este mes
    @GetMapping("/best-selling-products-monthly/{enterpriseId}/{outletId}")
    public ResponseEntity<List<BestSellingProductsMonthlyModel>> getBestSellingProductsMonthly(
            @PathVariable Long enterpriseId,
            @PathVariable Long outletId) {
        List<BestSellingProductsMonthlyModel> result = reportsService.getBestSellingProductsMonthly(enterpriseId, outletId);
        return ResponseEntity.ok(result);
    }

    // Productos más vendidos hoy
    @GetMapping("/best-selling-products-daily/{enterpriseId}/{outletId}")
    public ResponseEntity<List<BestSellingProductsDailyModel>> getBestSellingProductsDaily(
            @PathVariable Long enterpriseId,
            @PathVariable Long outletId) {
        List<BestSellingProductsDailyModel> result = reportsService.getBestSellingProductsDaily(enterpriseId, outletId);
        return ResponseEntity.ok(result);
    }

    // Productos en mínimo
    @GetMapping("/products-at-minimum/{outletId}")
    public ResponseEntity<List<ProductsAtMinimumModel>> getProductsAtMinimum(
            @PathVariable Long outletId) {
        List<ProductsAtMinimumModel> result = reportsService.getProductsAtMinimum(outletId);
        return ResponseEntity.ok(result);
    }

    // Total vendido hoy
    @GetMapping("/total-sold-daily/{enterpriseId}/{outletId}")
    public ResponseEntity<List<TotalSoldDailyModel>> getTotalSoldDaily(
            @PathVariable Long enterpriseId,
            @PathVariable Long outletId) {
        List<TotalSoldDailyModel> result = reportsService.getTotalSoldDaily(enterpriseId, outletId);
        return ResponseEntity.ok(result);
    }

    // Total vendido este mes
    @GetMapping("/total-sold-monthly/{enterpriseId}/{outletId}")
    public ResponseEntity<List<TotalSoldMonthlyModel>> getTotalSoldMonthly(
            @PathVariable Long enterpriseId,
            @PathVariable Long outletId) {
        List<TotalSoldMonthlyModel> result = reportsService.getTotalSoldMonthly(enterpriseId, outletId);
        return ResponseEntity.ok(result);
    }

    // Productos vendidos este mes
    @GetMapping("/products-sold-monthly/{enterpriseId}/{outletId}")
    public ResponseEntity<List<ProductsSoldMonthlyModel>> getProductsSoldMonthly(
            @PathVariable Long enterpriseId,
            @PathVariable Long outletId) {
        List<ProductsSoldMonthlyModel> result = reportsService.getProductsSoldMonthly(enterpriseId, outletId);
        return ResponseEntity.ok(result);
    }

    
}