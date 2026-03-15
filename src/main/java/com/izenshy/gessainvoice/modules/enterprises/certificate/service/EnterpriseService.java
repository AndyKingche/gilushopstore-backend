package com.izenshy.gessainvoice.modules.enterprises.certificate.service;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;

import java.util.Optional;

public interface EnterpriseService {
    Optional<EnterpriseModel> getEnterpriseByRuc(String ruc);
    Optional<EnterpriseModel> findById(Long enterpriseId);
}
