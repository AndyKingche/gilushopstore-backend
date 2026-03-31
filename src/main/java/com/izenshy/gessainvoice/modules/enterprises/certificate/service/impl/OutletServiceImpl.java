package com.izenshy.gessainvoice.modules.enterprises.certificate.service.impl;

import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.EnterpriseRepository;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.OutletRepository;
import com.izenshy.gessainvoice.modules.enterprises.certificate.service.OutletService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OutletServiceImpl implements OutletService {

    private final OutletRepository outletRepository;
    private final EnterpriseRepository enterpriseRepository;
    private static final Logger logger = LoggerFactory.getLogger(OutletServiceImpl.class);

    @Autowired
    public OutletServiceImpl(OutletRepository outletRepository, EnterpriseRepository enterpriseRepository) {
        this.outletRepository = outletRepository;
        this.enterpriseRepository = enterpriseRepository;
    }

    @Override
    public Optional<OutletModel> findById(Long outletId) {
        return outletRepository.findById(outletId);
    }

    @Override
    public OutletModel save(OutletModel outlet) {
        // Validate that enterprise exists
        if (outlet.getEnterpriseId() == null || !enterpriseRepository.existsById(outlet.getEnterpriseId().getId())) {
            throw new ResourceNotFoundException("Enterprise does not exist");
        }
        return outletRepository.save(outlet);
    }

    @Override
    public OutletModel update(OutletModel outlet) {
        // Validate that enterprise exists
        if (outlet.getEnterpriseId() == null || !enterpriseRepository.existsById(outlet.getEnterpriseId().getId())) {
            throw new ResourceNotFoundException("Enterprise does not exist");
        }
        return outletRepository.save(outlet);
    }

    @Override
    public List<OutletModel> findByEnterpriseId(Long enterpriseId) {
        return outletRepository.findByEnterpriseId_Id(enterpriseId);
    }

    @Override
    public List<OutletModel> findAll() {
        return outletRepository.findAll();
    }

    @Override
    public void delete(Long outletId) {
        Optional<OutletModel> outletOpt = outletRepository.findById(outletId);
        if (outletOpt.isPresent()) {
            OutletModel outlet = outletOpt.get();
            outlet.setOutletStatus(false);
            outletRepository.save(outlet);
        } else {
            //throw new RuntimeException("Outlet not found with id " + outletId);
            logger.error("Outlet not found with id " + outletId);
            throw new ResourceNotFoundException("Outlet not found with id " + outletId);
        }
    }
}