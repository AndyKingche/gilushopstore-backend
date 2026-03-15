package com.izenshy.gessainvoice.modules.enterprises.certificate.service;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.DigitalCertificateModel;

import java.time.LocalDate;
import java.util.Optional;

public interface DigitalCertificateService {

    DigitalCertificateModel saveCertificateCI(Long enterpriseId, String password, String dateExpired, byte[] certFile);
    boolean estaCertificadoVigente(byte[] certBytes, String password);
    byte[] cargarCertificadoHabilitado(Long enterpriseId);
    Optional<DigitalCertificateModel> getCertificate(Long enterpriseId, LocalDate dateExpired);
}
