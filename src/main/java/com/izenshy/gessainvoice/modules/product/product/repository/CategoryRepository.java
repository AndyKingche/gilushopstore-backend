package com.izenshy.gessainvoice.modules.product.product.repository;

import com.izenshy.gessainvoice.modules.product.product.model.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {
    // Buscar categoría por nombre (para validar duplicados)
    Optional<CategoryModel> findByCategoryNameIgnoreCase(String categoryName);

    // Verificar si ya existe por nombre
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
