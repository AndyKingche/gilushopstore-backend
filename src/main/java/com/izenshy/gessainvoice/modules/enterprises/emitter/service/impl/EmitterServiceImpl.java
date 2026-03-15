package com.izenshy.gessainvoice.modules.enterprises.emitter.service.impl;

import com.izenshy.gessainvoice.modules.enterprises.emitter.dto.EmitterDTO;
import com.izenshy.gessainvoice.modules.enterprises.emitter.mapper.EmitterMapper;
import com.izenshy.gessainvoice.modules.enterprises.emitter.model.EmitterModel;
import com.izenshy.gessainvoice.modules.enterprises.emitter.repository.EmitterRepository;
import com.izenshy.gessainvoice.modules.enterprises.emitter.service.EmitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmitterServiceImpl implements EmitterService {

    private final EmitterRepository emitterRepository;

    @Autowired
    public EmitterServiceImpl(EmitterRepository emitterRepository) {
        this.emitterRepository = emitterRepository;
    }

    @Override
    public Optional<EmitterModel> getEmitterById(Long id) {
        return emitterRepository.findById(id);
    }

    @Override
    public Optional<EmitterDTO> getEmitterByRucStatus(String ruc) {

        return emitterRepository.findByEmitterRucAndEmitterStatusTrue(ruc)
                .map(EmitterMapper.INSTANCE::modelToDTO);
    }

    @Override
    public Optional<EmitterDTO> getEmitterByRucAndCodEstbAndPtoEmisionAndStatus(String ruc, String codestb, String ptoemision) {

        return emitterRepository.findByEmitterRucAndEmitterCodEstbAndEmitterPtoEmisionAndEmitterStatusTrue(ruc,codestb, ptoemision)
                .map(EmitterMapper.INSTANCE::modelToDTO);
    }
}
