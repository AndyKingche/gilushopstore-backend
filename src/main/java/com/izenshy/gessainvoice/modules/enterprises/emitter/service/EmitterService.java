package com.izenshy.gessainvoice.modules.enterprises.emitter.service;

import com.izenshy.gessainvoice.modules.enterprises.emitter.dto.EmitterDTO;
import com.izenshy.gessainvoice.modules.enterprises.emitter.model.EmitterModel;

import java.util.Optional;

public interface EmitterService {
    Optional<EmitterModel> getEmitterById(Long id);
    Optional<EmitterDTO> getEmitterByRucStatus(String ruc);
    Optional<EmitterDTO> getEmitterByRucAndCodEstbAndPtoEmisionAndStatus(String ruc, String codestb, String ptoemision);
}
