package com.izenshy.gessainvoice.modules.enterprises.certificate.repository;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnterpriseRepository extends JpaRepository<EnterpriseModel, Long> {
    Optional<EnterpriseModel> findByEnterpriseIdentificationAndEnterpriseStatusTrue(String enterpriseIdentification);
}
