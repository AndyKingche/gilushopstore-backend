package com.izenshy.gessainvoice.modules.product.reports.service.impl;

import com.izenshy.gessainvoice.modules.product.reports.model.*;
import com.izenshy.gessainvoice.modules.product.reports.repository.*;
import com.izenshy.gessainvoice.modules.product.reports.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportsServiceImpl implements ReportsService {

    private final BestSellingProductsRepository bestSellingProductsRepository;
    private final BestSellingProductsMonthlyRepository bestSellingProductsMonthlyRepository;
    private final BestSellingProductsDailyRepository bestSellingProductsDailyRepository;
    private final ProductsAtMinimumRepository productsAtMinimumRepository;
    private final TotalSoldDailyRepository totalSoldDailyRepository;
    private final TotalSoldMonthlyRepository totalSoldMonthlyRepository;
    private final ProductsSoldMonthlyRepository productsSoldMonthlyRepository;

    @Autowired
    public ReportsServiceImpl(BestSellingProductsRepository bestSellingProductsRepository,
                              BestSellingProductsMonthlyRepository bestSellingProductsMonthlyRepository,
                              BestSellingProductsDailyRepository bestSellingProductsDailyRepository,
                              ProductsAtMinimumRepository productsAtMinimumRepository,
                              TotalSoldDailyRepository totalSoldDailyRepository,
                              TotalSoldMonthlyRepository totalSoldMonthlyRepository,
                              ProductsSoldMonthlyRepository productsSoldMonthlyRepository) {
        this.bestSellingProductsRepository = bestSellingProductsRepository;
        this.bestSellingProductsMonthlyRepository = bestSellingProductsMonthlyRepository;
        this.bestSellingProductsDailyRepository = bestSellingProductsDailyRepository;
        this.productsAtMinimumRepository = productsAtMinimumRepository;
        this.totalSoldDailyRepository = totalSoldDailyRepository;
        this.totalSoldMonthlyRepository = totalSoldMonthlyRepository;
        this.productsSoldMonthlyRepository = productsSoldMonthlyRepository;
    }

    @Override
    public List<BestSellingProductsModel> getBestSellingProducts(Long enterpriseId, Long outletId) {
        return bestSellingProductsRepository.findByEnterpriseIdAndOutletId(enterpriseId, outletId);
    }

    @Override
    public List<BestSellingProductsMonthlyModel> getBestSellingProductsMonthly(Long enterpriseId, Long outletId) {
        return bestSellingProductsMonthlyRepository.findByEnterpriseIdAndOutletId(enterpriseId, outletId);
    }

    @Override
    public List<BestSellingProductsDailyModel> getBestSellingProductsDaily(Long enterpriseId, Long outletId) {
        return bestSellingProductsDailyRepository.findByEnterpriseIdAndOutletId(enterpriseId, outletId);
    }

    @Override
    public List<ProductsAtMinimumModel> getProductsAtMinimum(Long outletId) {
        return productsAtMinimumRepository.findByOutletId(outletId);
    }

    @Override
    public List<TotalSoldDailyModel> getTotalSoldDaily(Long enterpriseId, Long outletId) {
        return totalSoldDailyRepository.findByEnterpriseIdAndOutletId(enterpriseId, outletId);
    }

    @Override
    public List<TotalSoldMonthlyModel> getTotalSoldMonthly(Long enterpriseId, Long outletId) {
        return totalSoldMonthlyRepository.findByEnterpriseIdAndOutletId(enterpriseId, outletId);
    }

    @Override
    public List<ProductsSoldMonthlyModel> getProductsSoldMonthly(Long enterpriseId, Long outletId) {
        return productsSoldMonthlyRepository.findByEnterpriseIdAndOutletId(enterpriseId, outletId);
    }
}