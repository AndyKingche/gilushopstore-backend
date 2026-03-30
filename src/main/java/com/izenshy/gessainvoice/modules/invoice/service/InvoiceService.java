package com.izenshy.gessainvoice.modules.invoice.service;

import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceHeaderDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceDetailModel;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {

    InvoiceModel saveInvoice(InvoiceModel invoice);
    InvoiceModel saveInvoiceDTO(InvoiceRequestDTO invoiceRequestDTO);
    InvoiceModel saveInvoiceHeader(InvoiceRequestDTO dto);
    InvoiceModel updateInvoiceDTO(Long id, InvoiceRequestDTO invoiceRequestDTO);
    InvoiceModel getInvoiceById(Long id);
    List<InvoiceModel> getAllInvoices();
    void deleteInvoice(Long id);
    InvoiceModel updateInvoiceDetails(Long invoiceId, List<InvoiceDetailRequestDTO> detailDTOs);
    List<InvoiceDetailModel> saveInvoiceDetails(List<InvoiceDetailRequestDTO> detailDTOs);

    // Search methods
    List<InvoiceResponseDTO> getInvoicesByUserId(Long userId);
    List<InvoiceResponseDTO> getInvoicesByClientId(Long clientId);
    List<InvoiceResponseDTO> getInvoicesByEnterpriseId(Long enterpriseId);
    InvoiceResponseDTO getLastInvoiceByEnterpriseId(Long enterpriseId);
    InvoiceResponseDTO getLastInvoiceByEnterpriseIdandFactura(Long enterpriseId, String pointOutlet);
    InvoiceResponseDTO getLastInvoiceByEnterpriseIdandFacturaComprobante(Long enterpriseId, String pointOutlet);
    List<InvoiceResponseDTO> getAllInvoiceByEnterpriseIdandFacturaComprobante(Long enterpriseId, String pointOutlet);

    BigDecimal getLastInvoiceTotalByUserAndEnterpriseAndDate(Long userId, Long enterpriseId, LocalDate date);
    InvoiceHeaderDTO getInvoiceWithDetails(Long invoiceId);
}
