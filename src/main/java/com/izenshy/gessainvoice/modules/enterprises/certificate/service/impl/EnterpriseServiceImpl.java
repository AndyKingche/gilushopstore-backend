package com.izenshy.gessainvoice.modules.enterprises.certificate.service.impl;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.EnterpriseRepository;
import com.izenshy.gessainvoice.modules.enterprises.certificate.service.EnterpriseService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EnterpriseServiceImpl implements EnterpriseService {
    private final EnterpriseRepository enterpriseRepository;

    public EnterpriseServiceImpl(EnterpriseRepository enterpriseRepository) {
        this.enterpriseRepository = enterpriseRepository;
    }

    @Override
    public Optional<EnterpriseModel> getEnterpriseByRuc(String ruc) {
        return enterpriseRepository.findByEnterpriseIdentificationAndEnterpriseStatusTrue(ruc);
    }

    @Override
    public Optional<EnterpriseModel> findById(Long enterpriseId) {
        return enterpriseRepository.findById(enterpriseId);
    }
}
