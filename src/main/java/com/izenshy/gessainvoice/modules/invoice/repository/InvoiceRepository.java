package com.izenshy.gessainvoice.modules.invoice.repository;

import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Query(value = "SELECT COALESCE( (SELECT invoice_total FROM public.invoice_header WHERE user_id = ?1 AND enterprise_id = ?2 AND invoice_date = ?3 ORDER BY invoice_date DESC, invoice_id DESC LIMIT 1), 0)", nativeQuery = true)
    BigDecimal getLastInvoiceTotalByUserAndEnterpriseAndDate(Long userId, Long enterpriseId, LocalDate date);
}
