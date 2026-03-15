package com.izenshy.gessainvoice.modules.product.product.service;

import com.izenshy.gessainvoice.modules.product.product.model.CategoryModel;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<CategoryModel> findAll();
    Optional<CategoryModel> findById(Long id);
    CategoryModel create(CategoryModel category);
    CategoryModel update(Long id, CategoryModel category);
    void delete(Long id);
    boolean existsByName(String categoryName);
}
