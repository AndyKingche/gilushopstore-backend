package com.izenshy.gessainvoice.modules.enterprises.emitter.repository;

import com.izenshy.gessainvoice.modules.enterprises.emitter.model.EmitterModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmitterRepository extends JpaRepository<EmitterModel, Long> {
    Optional<EmitterModel> findByEmitterRucAndEmitterStatusTrue(String ruc);
    Optional<EmitterModel> findByEmitterRucAndEmitterCodEstbAndEmitterPtoEmisionAndEmitterStatusTrue(String emitterRuc,
                                                                                                     String emitterCodEstb,
                                                                                                     String emitterPtoEmision);
}
