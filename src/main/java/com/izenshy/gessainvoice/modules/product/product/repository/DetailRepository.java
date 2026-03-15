package com.izenshy.gessainvoice.modules.product.product.repository;

import com.izenshy.gessainvoice.modules.product.product.model.DetailModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DetailRepository extends JpaRepository<DetailModel, Long>  {
    Optional<DetailModel> findByDetailNameIgnoreCase(String categoryName);
    boolean existsByDetailNameIgnoreCase(String categoryName);

}
