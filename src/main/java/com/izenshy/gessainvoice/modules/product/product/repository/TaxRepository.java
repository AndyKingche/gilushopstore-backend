package com.izenshy.gessainvoice.modules.product.product.repository;

import com.izenshy.gessainvoice.modules.product.product.dto.TaxResponse;
import com.izenshy.gessainvoice.modules.product.product.model.TaxModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaxRepository extends JpaRepository<TaxModel, Long> {
    Optional<TaxModel> findByTaxCode(String taxCode);
}
