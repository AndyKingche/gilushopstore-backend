package com.izenshy.gessainvoice.modules.product.product.service;

import com.izenshy.gessainvoice.modules.product.product.model.DetailModel;

import java.util.List;
import java.util.Optional;

public interface DetailService {
    Optional<DetailModel> findById(Long id);
    List<DetailModel> findAll();
    DetailModel create(DetailModel detail);
    DetailModel update(Long id, DetailModel detail);
    void delete(Long id);
    boolean existsByName(String detailName);
}
