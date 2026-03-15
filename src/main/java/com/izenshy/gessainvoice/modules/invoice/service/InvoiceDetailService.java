package com.izenshy.gessainvoice.modules.invoice.service;

import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceDetailModel;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;

import java.util.List;

public interface InvoiceDetailService {

    InvoiceDetailModel saveInvoiceDetail(InvoiceDetailModel invoiceDetail);
    InvoiceDetailModel saveInvoiceDetailDTO(InvoiceDetailRequestDTO invoiceDetailRequestDTO, Long invoiceId);
    void saveInvoiceDetails(List<InvoiceDetailRequestDTO> details, InvoiceModel parentInvoice);
    InvoiceDetailModel updateInvoiceDetailDTO(Long id, InvoiceDetailRequestDTO invoiceDetailRequestDTO);
    InvoiceDetailModel getInvoiceDetailById(Long id);
    List<InvoiceDetailModel> getAllInvoiceDetails();
    List<InvoiceDetailResponseDTO> getInvoiceDetailsByInvoiceId(Long invoiceId);
    void deleteInvoiceDetail(Long id);
    InvoiceDetailModel updatedInvoiceDetailDTO(
            List<InvoiceDetailRequestDTO> dtoList,
            Long invoiceId
    );
}