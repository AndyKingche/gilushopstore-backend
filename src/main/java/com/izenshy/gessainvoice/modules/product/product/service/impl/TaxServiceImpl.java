package com.izenshy.gessainvoice.modules.product.product.service.impl;

import com.izenshy.gessainvoice.common.exception.BadRequestException;
import com.izenshy.gessainvoice.common.exception.ResourceAlreadyExistsException;
import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.product.product.dto.TaxDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.TaxResponse;
import com.izenshy.gessainvoice.modules.product.product.mapper.TaxMapper;
import com.izenshy.gessainvoice.modules.product.product.model.TaxModel;
import com.izenshy.gessainvoice.modules.product.product.repository.TaxRepository;
import com.izenshy.gessainvoice.modules.product.product.service.TaxService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaxServiceImpl implements TaxService {

    private final TaxRepository taxRepository;
    private final TaxMapper taxMapper;

    @Autowired
    public TaxServiceImpl(TaxRepository taxRepository, TaxMapper taxMapper) {
        this.taxRepository = taxRepository;
        this.taxMapper = taxMapper;
    }

    @Override
    public TaxDTO createTax(TaxDTO taxDTO) {
        if (taxDTO.getTaxCode() == null || taxDTO.getTaxCode().isEmpty()) {
            throw new BadRequestException("Tax code is required");
        }

        taxRepository.findByTaxCode(taxDTO.getTaxCode()).ifPresent(t -> {
            throw new ResourceAlreadyExistsException("Tax with code " + taxDTO.getTaxCode() + " already exists");
        });

        TaxModel tax = taxMapper.dtoToModel(taxDTO);
        TaxModel saved = taxRepository.save(tax);
        return taxMapper.modelToDTO(saved);
    }

    @Override
    public TaxDTO updateTax(Long id, TaxDTO taxDTO) {
        TaxModel existing = taxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with id " + id));

        if (!existing.getTaxCode().equals(taxDTO.getTaxCode())
                && taxRepository.findByTaxCode(taxDTO.getTaxCode()).isPresent()) {
            throw new ResourceAlreadyExistsException("Tax code " + taxDTO.getTaxCode() + " already exists");
        }

        existing.setTaxCode(taxDTO.getTaxCode());
        existing.setTaxPercentage(taxDTO.getTaxPercentage());
        existing.setCodeSri(taxDTO.getCodeSri());
        existing.setTaxValue(taxDTO.getTaxValue());

        TaxModel updated = taxRepository.save(existing);
        return taxMapper.modelToDTO(updated);
    }

    @Override
    public void deleteTax(Long id) {
        if (!taxRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tax not found with id " + id);
        }
        taxRepository.deleteById(id);
    }

    @Override
    public TaxResponse getTaxById(Long id) {
        TaxModel tax = taxRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tax not found with id " + id));
        return taxMapper.modelToResponse(tax);
    }

    @Override
    public List<TaxResponse> getAllTaxes() {
        List<TaxModel> taxes = taxRepository.findAll();
        return taxMapper.modelsToResponses(taxes);
    }

    @Override
    public Optional<TaxResponse> findByTaxCode(String taxCode) {
        return taxRepository.findByTaxCode(taxCode)
                .map(taxMapper::modelToResponse);
    }
}
