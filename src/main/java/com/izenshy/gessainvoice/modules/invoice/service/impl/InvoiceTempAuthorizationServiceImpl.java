package com.izenshy.gessainvoice.modules.invoice.service.impl;

import com.izenshy.gessainvoice.common.response.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.EnterpriseRepository;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.OutletRepository;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationAuxResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.mapper.InvoiceTempAuthorizationMapper;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceTempAuthorizationModel;
import com.izenshy.gessainvoice.modules.invoice.repository.InvoiceRepository;
import com.izenshy.gessainvoice.modules.invoice.repository.InvoiceTempAuthorizationRepository;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceTempAuthorizationService;
import com.izenshy.gessainvoice.sri.service.SriInvoiceProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvoiceTempAuthorizationServiceImpl implements InvoiceTempAuthorizationService {

    private final InvoiceTempAuthorizationRepository repository;
    private final InvoiceTempAuthorizationMapper mapper;
    private final EnterpriseRepository enterpriseRepository;
    private final OutletRepository outletRepository;
    private final InvoiceRepository invoiceRepository;
    private final SriInvoiceProcessingService sriInvoiceProcessingService;

    @Value("${sri.url.RecepcionComprobantesOffline}")
    private String recepcionUrl;

    @Value("${sri.url.autorizacion}")
    private String autorizacionUrl;

    @Autowired
    public InvoiceTempAuthorizationServiceImpl(
            InvoiceTempAuthorizationRepository repository,
            InvoiceTempAuthorizationMapper mapper,
            EnterpriseRepository enterpriseRepository,
            OutletRepository outletRepository,
            InvoiceRepository invoiceRepository, SriInvoiceProcessingService sriInvoiceProcessingService) {
        this.repository = repository;
        this.mapper = mapper;
        this.enterpriseRepository = enterpriseRepository;
        this.outletRepository = outletRepository;
        this.invoiceRepository = invoiceRepository;
        this.sriInvoiceProcessingService = sriInvoiceProcessingService;
    }

    @Override
    public InvoiceTempAuthorizationModel save(InvoiceTempAuthorizationModel model) {
        return repository.save(model);
    }

    @Override
    public InvoiceTempAuthorizationModel saveFromDTO(InvoiceTempAuthorizationRequestDTO dto) {
        // Validate references exist
        if (dto.getEnterpriseId() != null && !enterpriseRepository.existsById(dto.getEnterpriseId())) {
            throw new RuntimeException("Enterprise does not exist");
        }
        if (dto.getOutletId() != null && !outletRepository.existsById(dto.getOutletId())) {
            throw new RuntimeException("Outlet does not exist");
        }
        if (dto.getInvoiceId() != null && !invoiceRepository.existsById(dto.getInvoiceId())) {
            throw new RuntimeException("Invoice does not exist");
        }

        InvoiceTempAuthorizationModel model = mapper.requestDtoToModel(dto);
        return repository.save(model);
    }

    @Override
    public InvoiceTempAuthorizationModel update(Long id, InvoiceTempAuthorizationRequestDTO dto) {
        InvoiceTempAuthorizationModel existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice temp authorization not found with id: " + id));

        // Validate references exist
        if (dto.getEnterpriseId() != null && !enterpriseRepository.existsById(dto.getEnterpriseId())) {
            throw new RuntimeException("Enterprise does not exist");
        }
        if (dto.getOutletId() != null && !outletRepository.existsById(dto.getOutletId())) {
            throw new RuntimeException("Outlet does not exist");
        }
        if (dto.getInvoiceId() != null && !invoiceRepository.existsById(dto.getInvoiceId())) {
            throw new RuntimeException("Invoice does not exist");
        }

        // Update fields
        if (dto.getFileBase64() != null) {
            existing.setFileBase64(dto.getFileBase64());
        }
        if (dto.getAccessCode() != null) {
            existing.setAccessCode(dto.getAccessCode());
        }
        if (dto.getReceptionStatus() != null) {
            existing.setReceptionStatus(dto.getReceptionStatus());
        }
        if (dto.getAuthorizationStatus() != null) {
            existing.setAuthorizationStatus(dto.getAuthorizationStatus());
        }
        if (dto.getEnterpriseId() != null) {
            var enterprise = new com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel();
            enterprise.setId(dto.getEnterpriseId());
            existing.setEnterpriseId(enterprise);
        }
        if (dto.getOutletId() != null) {
            var outlet = new com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel();
            outlet.setOutletId(dto.getOutletId());
            existing.setOutletId(outlet);
        }
        if (dto.getInvoiceId() != null) {
            var invoice = new com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel();
            invoice.setId(dto.getInvoiceId());
            existing.setInvoiceId(invoice);
        }

        return repository.save(existing);
    }

    @Override
    public Optional<InvoiceTempAuthorizationModel> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<InvoiceTempAuthorizationModel> findAll() {
        return repository.findAll();
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Invoice temp authorization not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public Optional<InvoiceTempAuthorizationModel> findByTempUuid(UUID tempUuid) {
        return repository.findByTempUuid(tempUuid);
    }

    @Override
    public List<InvoiceTempAuthorizationModel> findByEnterpriseId(Long enterpriseId) {
        return repository.findByEnterpriseId_Id(enterpriseId);
    }

    @Override
    public List<InvoiceTempAuthorizationModel> findByOutletId(Long outletId) {
        return repository.findByOutletId_OutletId(outletId);
    }

    @Override
    public List<InvoiceTempAuthorizationModel> findByInvoiceId(Long invoiceId) {
        return repository.findByInvoiceId_Id(invoiceId);
    }

    @Override
    public Optional<InvoiceTempAuthorizationModel> findByAccessCode(String accessCode) {
        return repository.findByAccessCode(accessCode);
    }

    @Override
    public List<InvoiceTempAuthorizationModel> findByReceptionStatus(String receptionStatus) {
        return repository.findByReceptionStatus(receptionStatus);
    }

    @Override
    public List<InvoiceTempAuthorizationModel> findByAuthorizationStatus(String authorizationStatus) {
        return repository.findByAuthorizationStatus(authorizationStatus);
    }

    @Override
    public InvoiceTempAuthorizationResponseDTO toResponseDTO(InvoiceTempAuthorizationModel model) {
        return mapper.modelToResponseDto(model);
    }

    @Override
    public List<InvoiceTempAuthorizationResponseDTO> toResponseDTOList(List<InvoiceTempAuthorizationModel> models) {
        return models.stream()
                .map(mapper::modelToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceTempAuthorizationAuxResponseDTO> toResponseAuxDTOList(List<InvoiceTempAuthorizationModel> models) {
        return models.stream()
                .map(mapper::modelToAuxResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean reSendBillingSRI(Long invoiceId, Long enterpriseId, Long outletId) {
        InvoiceTempAuthorizationModel findTemp = repository
                .findByInvoiceId_IdAndEnterpriseId_IdAndOutletId_OutletId
                        (invoiceId, enterpriseId, outletId);

        String recepcionSoapRequest = sriInvoiceProcessingService
                .buildRecepcionSoapRequest(findTemp.getFileBase64());

        String recepcionResponse = sriInvoiceProcessingService
                .sendSoapRequest(recepcionUrl, recepcionSoapRequest);

        if (recepcionResponse.contains("<estado>DEVUELTA</estado>")) {
            System.out.println("⚠ Comprobante DEVUELTO - Verificando motivo...");

            // Verificar si es error 70 (comprobante en procesamiento)
            if (recepcionResponse.contains("<identificador>70</identificador>")) {
                System.out.println("✓ Error 70 detectado: Comprobante en procesamiento");
                System.out.println("✓ Continuando con el proceso de autorización...");

                return false;
                // NO lanzar excepción, continuar normalmente
            } else {
                // Otro error diferente al 70
                System.out.println("✗ Comprobante devuelto por otro motivo:");
                System.out.println(recepcionResponse);

                // Extraer el código de error y mensaje

                throw new ResourceNotFoundException(
                        "Comprobante DEVUELTO por el SRI - Código"
                );
            }
        } else if (recepcionResponse.contains("<estado>RECIBIDA</estado>")) {
            System.out.println("✓ Comprobante RECIBIDO correctamente por el SRI");

            // Paso 6: Enviar al SRI AutorizacionComprobantes
            String autorizacionSoapRequest = sriInvoiceProcessingService
                    .buildAutorizacionSoapRequest(findTemp.getAccessCode());

            String autorizacionResponse = sriInvoiceProcessingService.sendSoapRequest(autorizacionUrl, autorizacionSoapRequest);
            System.out.println("AUTORIZACION? "+autorizacionResponse);

            // Paso 7: Revisar si  la factura fue AUTORIZADO
            if (!sriInvoiceProcessingService.isAutorizado(autorizacionResponse)) {
                throw new RuntimeException("La Factura no autorizada por el SRI");
            }else{

                delete(findTemp.getId());
                return true;
            }
        } else {
            System.out.println("⚠ Estado desconocido en respuesta de recepción");
            return false;
        }

    }

    @Override
    public List<InvoiceTempAuthorizationModel> findByEnterpriseId_IdAndOutletId_OutletId(Long enterpriseId, Long outletId) {
        return repository.findByEnterpriseId_IdAndOutletId_OutletId(enterpriseId,outletId);
    }
}
