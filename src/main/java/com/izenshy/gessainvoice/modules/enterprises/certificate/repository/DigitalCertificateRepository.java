package com.izenshy.gessainvoice.modules.enterprises.certificate.repository;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.DigitalCertificateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DigitalCertificateRepository extends JpaRepository<DigitalCertificateModel, Long> {

    //Optional<DigitalCertificateModel> findByUserId_IdAndDigCertStatusTrue(Long userId);
    Optional<DigitalCertificateModel> findByEnterpriseId_IdAndDigCertStatusTrue(Long enterpriseId);
    Optional<DigitalCertificateModel> findByEnterpriseId_IdAndDigCertStatusTrueAndDigCertExpirationDateAfter(Long enterpriseId, LocalDate dateExpired);
    //Optional<DigitalCertificateModel> findByUserId_IdAndDigCertStatusTrueAndDigCertExpirationDateAfter(Long userId, LocalDate dateExpired);


}
