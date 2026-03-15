package com.izenshy.gessainvoice.modules.enterprises.certificate.service;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;

import java.util.List;
import java.util.Optional;

public interface OutletService {

    Optional<OutletModel> findById(Long outletId);
    OutletModel save(OutletModel outlet);
    OutletModel update(OutletModel outlet);
    List<OutletModel> findByEnterpriseId(Long enterpriseId);
    List<OutletModel> findAll();
    void delete(Long outletId);
}