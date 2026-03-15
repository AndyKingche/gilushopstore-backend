package com.izenshy.gessainvoice.modules.product.product.service.impl;

import com.izenshy.gessainvoice.modules.product.product.model.DetailModel;
import com.izenshy.gessainvoice.modules.product.product.repository.DetailRepository;
import com.izenshy.gessainvoice.modules.product.product.service.DetailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetailServiceImpl implements DetailService {
    private final DetailRepository detailRepository;

    public DetailServiceImpl(DetailRepository detailRepository) {
        this.detailRepository = detailRepository;
    }

    @Override
    public Optional<DetailModel> findById(Long id) {
        return detailRepository.findById(id);
    }

    @Override
    public List<DetailModel> findAll() {
        return detailRepository.findAll();
    }

    @Override
    public DetailModel create(DetailModel detail) {
        return detailRepository.save(detail);
    }

    @Override
    public DetailModel update(Long id, DetailModel detail) {
        
        return detailRepository.save(detail);
    }

    @Override
    public void delete(Long id) {
        detailRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String detailName) {
        return detailRepository.existsByDetailNameIgnoreCase(detailName);
    }
}
