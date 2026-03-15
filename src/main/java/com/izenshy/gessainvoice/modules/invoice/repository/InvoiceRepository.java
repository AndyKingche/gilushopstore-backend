package com.izenshy.gessainvoice.modules.invoice.repository;

import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceModel, Long> {

    List<InvoiceModel> findByUserId_Id(Long userId);
    List<InvoiceModel> findByClientId_Id(Long clientId);
    List<InvoiceModel> findByEnterpriseId_Id(Long enterpriseId);
    InvoiceModel findTopByEnterpriseId_IdOrderByIdDesc(Long enterpriseId);
    InvoiceModel findTopByEnterpriseId_IdAndInvoiceTypeOrderByIdDesc(Long enterpriseId, String invoiceType);
    InvoiceModel findTopByEnterpriseId_IdAndInvoiceTypeAndIssuePointOrderByIdDesc(
            Long enterpriseId,
            String invoiceType,
            String issuePoint
    );
    List<InvoiceModel> findByEnterpriseId_IdAndInvoiceTypeAndIssuePointOrderByIdDesc(
                    Long enterpriseId,
                    String invoiceType,
                    String issuePoint
            );
}
