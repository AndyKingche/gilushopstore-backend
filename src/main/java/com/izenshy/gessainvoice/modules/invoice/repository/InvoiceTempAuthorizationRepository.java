package com.izenshy.gessainvoice.modules.invoice.repository;

import com.izenshy.gessainvoice.modules.invoice.model.InvoiceTempAuthorizationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceTempAuthorizationRepository extends JpaRepository<InvoiceTempAuthorizationModel, Long> {

    Optional<InvoiceTempAuthorizationModel> findByTempUuid(UUID tempUuid);

    List<InvoiceTempAuthorizationModel> findByEnterpriseId_Id(Long enterpriseId);

    List<InvoiceTempAuthorizationModel> findByOutletId_OutletId(Long outletId);

    List<InvoiceTempAuthorizationModel> findByInvoiceId_Id(Long invoiceId);

    Optional<InvoiceTempAuthorizationModel> findByAccessCode(String accessCode);

    List<InvoiceTempAuthorizationModel> findByReceptionStatus(String receptionStatus);

    List<InvoiceTempAuthorizationModel> findByAuthorizationStatus(String authorizationStatus);

    Optional<InvoiceTempAuthorizationModel> findByInvoiceId_IdAndEnterpriseId_Id(Long invoiceId, Long enterpriseId);

    InvoiceTempAuthorizationModel findByInvoiceId_IdAndEnterpriseId_IdAndOutletId_OutletId(Long invoiceId, Long enterpriseId, Long outletId);

    List<InvoiceTempAuthorizationModel> findByEnterpriseId_IdAndOutletId_OutletId(Long enterpriseId, Long outletId);

}
