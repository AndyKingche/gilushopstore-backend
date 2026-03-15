package com.izenshy.gessainvoice.modules.product.product.service;

import com.izenshy.gessainvoice.modules.product.product.dto.TaxDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.TaxResponse;

import java.util.List;
import java.util.Optional;

public interface TaxService {
    TaxDTO createTax(TaxDTO taxDTO);
    TaxDTO updateTax(Long id, TaxDTO taxDTO);
    void deleteTax(Long id);
    TaxResponse getTaxById(Long id);
    List<TaxResponse> getAllTaxes();
    Optional<TaxResponse> findByTaxCode(String taxCode);
}
