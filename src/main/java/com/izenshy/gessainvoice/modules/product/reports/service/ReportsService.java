package com.izenshy.gessainvoice.modules.product.reports.service;

import com.izenshy.gessainvoice.modules.product.reports.model.*;

import java.util.List;

public interface ReportsService {

    List<BestSellingProductsModel> getBestSellingProducts(Long enterpriseId, Long outletId);
    List<BestSellingProductsMonthlyModel> getBestSellingProductsMonthly(Long enterpriseId, Long outletId);
    List<BestSellingProductsDailyModel> getBestSellingProductsDaily(Long enterpriseId, Long outletId);
    List<ProductsAtMinimumModel> getProductsAtMinimum(Long outletId);
    List<TotalSoldDailyModel> getTotalSoldDaily(Long enterpriseId, Long outletId);
    List<TotalSoldMonthlyModel> getTotalSoldMonthly(Long enterpriseId, Long outletId);
    List<ProductsSoldMonthlyModel> getProductsSoldMonthly(Long enterpriseId, Long outletId);
}