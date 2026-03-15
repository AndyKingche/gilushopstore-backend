package com.izenshy.gessainvoice.modules.invoice.service;

import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationAuxResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceTempAuthorizationModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceTempAuthorizationService {

    InvoiceTempAuthorizationModel save(InvoiceTempAuthorizationModel model);

    InvoiceTempAuthorizationModel saveFromDTO(InvoiceTempAuthorizationRequestDTO dto);

    InvoiceTempAuthorizationModel update(Long id, InvoiceTempAuthorizationRequestDTO dto);

    Optional<InvoiceTempAuthorizationModel> findById(Long id);

    List<InvoiceTempAuthorizationModel> findAll();

    void delete(Long id);

    Optional<InvoiceTempAuthorizationModel> findByTempUuid(UUID tempUuid);

    List<InvoiceTempAuthorizationModel> findByEnterpriseId(Long enterpriseId);

    List<InvoiceTempAuthorizationModel> findByOutletId(Long outletId);

    List<InvoiceTempAuthorizationModel> findByInvoiceId(Long invoiceId);

    Optional<InvoiceTempAuthorizationModel> findByAccessCode(String accessCode);

    List<InvoiceTempAuthorizationModel> findByReceptionStatus(String receptionStatus);

    List<InvoiceTempAuthorizationModel> findByAuthorizationStatus(String authorizationStatus);

    InvoiceTempAuthorizationResponseDTO toResponseDTO(InvoiceTempAuthorizationModel model);

    List<InvoiceTempAuthorizationResponseDTO> toResponseDTOList(List<InvoiceTempAuthorizationModel> models);

    List<InvoiceTempAuthorizationAuxResponseDTO> toResponseAuxDTOList(List<InvoiceTempAuthorizationModel> models);

    boolean reSendBillingSRI(Long invoiceId, Long enterpriseId, Long outletId);

    List<InvoiceTempAuthorizationModel> findByEnterpriseId_IdAndOutletId_OutletId(Long enterpriseId, Long outletId);
}
