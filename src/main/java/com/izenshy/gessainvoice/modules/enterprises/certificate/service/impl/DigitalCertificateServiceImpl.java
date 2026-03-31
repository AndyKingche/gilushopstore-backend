package com.izenshy.gessainvoice.modules.enterprises.certificate.service.impl;

import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.DigitalCertificateModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.DigitalCertificateRepository;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.EnterpriseRepository;
import com.izenshy.gessainvoice.modules.enterprises.certificate.service.DigitalCertificateService;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import com.izenshy.gessainvoice.modules.person.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Optional;

@Service
public class DigitalCertificateServiceImpl implements DigitalCertificateService {
    private final DigitalCertificateRepository digitalCertificateRepository;
    private final UserRepository userRepository;
    private final PasswordGenerateService passwordGenerateService;
    private final EnterpriseRepository enterpriseRepository;

    @Autowired
    public DigitalCertificateServiceImpl(DigitalCertificateRepository digitalCertificateRepository, UserRepository userRepository, PasswordGenerateService passwordGenerateService, EnterpriseRepository enterpriseRepository) {
        this.digitalCertificateRepository = digitalCertificateRepository;
        this.userRepository = userRepository;
        this.passwordGenerateService = passwordGenerateService;
        this.enterpriseRepository = enterpriseRepository;
    }

    /*@Override
    public DigitalCertificateModel saveCertificateCI(Long userId, String password, String dateExpired, byte[] certFile) {

        digitalCertificateRepository.findByUserId_IdAndDigCertStatusTrue(userId)
                .ifPresent(cert -> {
                    cert.setDigCertStatus(false);
                    digitalCertificateRepository.save(cert);
                });

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + userId));

        DigitalCertificateModel newCert = new DigitalCertificateModel();
        newCert.setDigCertName(user.getUserLastname() +" "+user.getUserName());
        newCert.setDigCertPassword(passwordGenerateService.encrypt(password, user.getId()));
        newCert.setDigCertificate(certFile);
        newCert.setDigCertStatus(true);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate dateExpiredFormat = LocalDate.parse(dateExpired, formatter);
        newCert.setDigCertExpirationDate(dateExpiredFormat);
        newCert.setUserId(user);
        return digitalCertificateRepository.save(newCert);
    }     */

    @Override
    public DigitalCertificateModel saveCertificateCI(Long enterpriseId, String password, String dateExpired, byte[] certFile) {

        digitalCertificateRepository.findByEnterpriseId_IdAndDigCertStatusTrue(enterpriseId)
                .ifPresent(cert -> {
                    cert.setDigCertStatus(false);
                    digitalCertificateRepository.save(cert);
                });

        UserModel user = userRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + enterpriseId));

        EnterpriseModel enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrado con id: " + enterpriseId));

        DigitalCertificateModel newCert = new DigitalCertificateModel();
        newCert.setDigCertName(enterprise.getEnterpriseOwnerName());
        newCert.setDigCertPassword(passwordGenerateService.encrypt(password, enterprise.getId()));
        newCert.setDigCertificate(certFile);
        newCert.setDigCertStatus(true);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate dateExpiredFormat = LocalDate.parse(dateExpired, formatter);
        newCert.setDigCertExpirationDate(dateExpiredFormat);
        newCert.setEnterpriseId(enterprise);
        return digitalCertificateRepository.save(newCert);
    }

    @Override
    public boolean estaCertificadoVigente(byte[] certBytes, String password) {

        try (InputStream in = new ByteArrayInputStream(certBytes)) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(in, password.toCharArray());

            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate cert = ks.getCertificate(alias);
                if (cert instanceof X509Certificate x509Cert) {
                    x509Cert.checkValidity();
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /*
    @Override
    public byte[] cargarCertificadoHabilitado(Long userId) {
        DigitalCertificateModel certifcateFound = digitalCertificateRepository.findByUserId_IdAndDigCertStatusTrueAndDigCertExpirationDateAfter(userId, LocalDate.now())
                .orElseThrow(() ->
                        new ResponseStatusException
                                (HttpStatus.BAD_REQUEST, "Certificado Expirado"));

        return certifcateFound.getDigCertificate();
    }

     */
    @Override
    public byte[] cargarCertificadoHabilitado(Long enterpriseId) {
        DigitalCertificateModel certifcateFound = digitalCertificateRepository.findByEnterpriseId_IdAndDigCertStatusTrueAndDigCertExpirationDateAfter(enterpriseId, LocalDate.now())
                .orElseThrow(() ->
                        new ResponseStatusException
                                (HttpStatus.BAD_REQUEST, "Certificado Expirado"));

        return certifcateFound.getDigCertificate();
    }

    @Override
    public Optional<DigitalCertificateModel> getCertificate(Long enterpriseId, LocalDate dateExpired) {
        return Optional.ofNullable(digitalCertificateRepository.findByEnterpriseId_IdAndDigCertStatusTrueAndDigCertExpirationDateAfter(enterpriseId, dateExpired)
                .orElseThrow(() ->
                        new ResponseStatusException
                                (HttpStatus.BAD_REQUEST, "Certificado no existe")));
    }

}
