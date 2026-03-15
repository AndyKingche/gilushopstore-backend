package com.izenshy.gessainvoice.modules.invoice.repository;

import com.izenshy.gessainvoice.modules.invoice.model.InvoiceDetailModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetailModel, Long> {

    List<InvoiceDetailModel> findByInvoice_Id(Long invoiceId);
}