package com.izenshy.gessainvoice.modules.product.product.service.impl;

import com.izenshy.gessainvoice.common.exception.ResourceAlreadyExistsException;
import com.izenshy.gessainvoice.modules.product.product.model.CategoryModel;
import com.izenshy.gessainvoice.modules.product.product.repository.CategoryRepository;
import com.izenshy.gessainvoice.modules.product.product.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryModel> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<CategoryModel> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public CategoryModel create(CategoryModel category) {
        if (categoryRepository.existsByCategoryNameIgnoreCase(category.getCategoryName())) {
            throw new ResourceAlreadyExistsException("Category with name '" + category.getCategoryName() + "' already exists.");
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public CategoryModel update(Long id, CategoryModel category) {
        CategoryModel existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id));

        // Validar duplicados si cambia el nombre
        if (!existing.getCategoryName().equalsIgnoreCase(category.getCategoryName())
                && categoryRepository.existsByCategoryNameIgnoreCase(category.getCategoryName())) {
            throw new ResourceAlreadyExistsException("Category with name '" + category.getCategoryName() + "' already exists.");
        }

        existing.setCategoryName(category.getCategoryName());
        existing.setCategoryDesc(category.getCategoryDesc());
        return categoryRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found with id " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String categoryName) {
        return categoryRepository.existsByCategoryNameIgnoreCase(categoryName);
    }
}
