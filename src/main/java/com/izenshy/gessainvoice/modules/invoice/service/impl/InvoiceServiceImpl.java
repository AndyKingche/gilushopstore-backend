package com.izenshy.gessainvoice.modules.invoice.service.impl;

import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.EnterpriseRepository;
import com.izenshy.gessainvoice.modules.enterprises.emitter.repository.EmitterRepository;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceHeaderDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceHeaderDetailDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.mapper.InvoiceDetailMapper;
import com.izenshy.gessainvoice.modules.invoice.mapper.InvoiceMapper;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceDetailModel;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.invoice.repository.InvoiceDetailRepository;
import com.izenshy.gessainvoice.modules.invoice.repository.InvoiceRepository;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceService;
import com.izenshy.gessainvoice.modules.person.client.model.ClientModel;
import com.izenshy.gessainvoice.modules.person.client.repository.ClientRepository;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import com.izenshy.gessainvoice.modules.person.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final InvoiceDetailMapper invoiceDetailMapper;
    private final EmitterRepository emitterRepository;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, InvoiceMapper invoiceMapper,
            UserRepository userRepository, ClientRepository clientRepository,
            EnterpriseRepository enterpriseRepository, InvoiceDetailRepository invoiceDetailRepository,
            InvoiceDetailMapper invoiceDetailMapper, EmitterRepository emitterRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.invoiceDetailRepository = invoiceDetailRepository;
        this.invoiceDetailMapper = invoiceDetailMapper;
        this.emitterRepository = emitterRepository;
    }

    @Override
    public InvoiceModel saveInvoice(InvoiceModel invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public InvoiceModel saveInvoiceDTO(InvoiceRequestDTO invoiceRequestDTO) {
        // Validate references exist
        if (invoiceRequestDTO.getUserId() != null && !userRepository.existsById(invoiceRequestDTO.getUserId())) {
            throw new ResourceNotFoundException("User does not exist");
        }
        if (invoiceRequestDTO.getClientId() != null && !clientRepository.existsById(invoiceRequestDTO.getClientId())) {
            throw new ResourceNotFoundException("Client does not exist");
        }
        if (invoiceRequestDTO.getEnterpriseId() != null
                && !enterpriseRepository.existsById(invoiceRequestDTO.getEnterpriseId())) {
            throw new ResourceNotFoundException("Enterprise does not exist");
        }

        // Convertir el DTO a entidad (sin detalles todavía vinculados)
        InvoiceModel invoice = invoiceMapper.requestDtoToModel(invoiceRequestDTO);

        // Guardar primero la cabecera (necesitamos su ID)
        InvoiceModel savedInvoice = invoiceRepository.save(invoice);

        // Si tiene detalles, los vinculamos con la factura guardada
        if (invoiceRequestDTO.getDetails() != null && !invoiceRequestDTO.getDetails().isEmpty()) {
            List<com.izenshy.gessainvoice.modules.invoice.model.InvoiceDetailModel> detailModels = invoiceRequestDTO
                    .getDetails().stream()
                    .map(detailDTO -> {
                        var detailModel = invoiceDetailMapper.requestDtoToModel(detailDTO);
                        detailModel.setInvoice(savedInvoice); // 🔥 Aquí se vincula correctamente
                        return detailModel;
                    })
                    .toList();

            // Guardar todos los detalles
            invoiceDetailRepository.saveAll(detailModels);

            // Asignar los detalles a la factura y actualizar (opcional)
            savedInvoice.setDetails(detailModels);
        }

        return savedInvoice;
    }

    @Override
    public InvoiceModel saveInvoiceHeader(InvoiceRequestDTO dto) {
        InvoiceModel invoice = invoiceMapper.requestDtoToModel(dto);
        return invoiceRepository.save(invoice);
    }

    @Override
    public InvoiceModel updateInvoiceDTO(Long id, InvoiceRequestDTO invoiceRequestDTO) {
        // Validate references exist
        if (invoiceRequestDTO.getUserId() != null && !userRepository.existsById(invoiceRequestDTO.getUserId())) {
            throw new ResourceNotFoundException("User does not exist");
        }
        if (invoiceRequestDTO.getClientId() != null && !clientRepository.existsById(invoiceRequestDTO.getClientId())) {
            throw new ResourceNotFoundException("Client does not exist");
        }
        if (invoiceRequestDTO.getEnterpriseId() != null
                && !enterpriseRepository.existsById(invoiceRequestDTO.getEnterpriseId())) {
            throw new ResourceNotFoundException("Enterprise does not exist");
        }

        InvoiceModel existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id " + id));

        // Update simple fields on the existing managed entity
        existingInvoice.setInvoiceStatus(invoiceRequestDTO.getInvoiceStatus());
        existingInvoice.setInvoiceTax(invoiceRequestDTO.getInvoiceTax());
        existingInvoice.setInvoiceDate(invoiceRequestDTO.getInvoiceDate());
        existingInvoice.setInvoiceTotal(invoiceRequestDTO.getInvoiceTotal());
        existingInvoice.setInvoiceSubtotal(invoiceRequestDTO.getInvoiceSubtotal());
        existingInvoice.setInvoiceDiscount(invoiceRequestDTO.getInvoiceDiscount());
        existingInvoice.setPaymentType(invoiceRequestDTO.getPaymentType());
        existingInvoice.setSequential(invoiceRequestDTO.getSequential());
        existingInvoice.setRemissionGuide(invoiceRequestDTO.getRemissionGuide());
        existingInvoice.setAccessKey(invoiceRequestDTO.getAccessKey());
        existingInvoice.setIssuePoint(invoiceRequestDTO.getIssuePoint());
        existingInvoice.setEstablishment(invoiceRequestDTO.getEstablishment());
        existingInvoice.setInvoiceType(invoiceRequestDTO.getInvoiceType());

        // Handle related entities
        if (invoiceRequestDTO.getUserId() != null) {
            UserModel user = new UserModel();
            user.setId(invoiceRequestDTO.getUserId());
            existingInvoice.setUserId(user);
        } else {
            existingInvoice.setUserId(null);
        }

        if (invoiceRequestDTO.getClientId() != null) {
            ClientModel client = new ClientModel();
            client.setId(invoiceRequestDTO.getClientId());
            existingInvoice.setClientId(client);
        } else {
            existingInvoice.setClientId(null);
        }

        if (invoiceRequestDTO.getEnterpriseId() != null) {
            EnterpriseModel enterprise = new EnterpriseModel();
            enterprise.setId(invoiceRequestDTO.getEnterpriseId());
            existingInvoice.setEnterpriseId(enterprise);
        } else {
            existingInvoice.setEnterpriseId(null);
        }

        // Update details - clear existing and add new ones (maintain the same
        // collection reference)
        if (invoiceRequestDTO.getDetails() != null) {
            existingInvoice.getDetails().clear();
            List<InvoiceDetailModel> newDetails = invoiceRequestDTO.getDetails().stream()
                    .map(invoiceDetailMapper::requestDtoToModel)
                    .collect(Collectors.toList());
            for (InvoiceDetailModel detail : newDetails) {
                detail.setInvoice(existingInvoice);
                existingInvoice.getDetails().add(detail);
            }
        } else {
            existingInvoice.getDetails().clear();
        }

        return invoiceRepository.save(existingInvoice);
    }

    @Override
    public InvoiceModel getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id " + id));
    }

    @Override
    public List<InvoiceModel> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    @Override
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Invoice not found with id " + id);
        }
        invoiceRepository.deleteById(id);
    }

    @Override
    public InvoiceModel updateInvoiceDetails(Long invoiceId, List<InvoiceDetailRequestDTO> detailDTOs) {
        InvoiceModel invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con ID: " + invoiceId));

        // Asignar el invoiceId a cada detalle
        detailDTOs.forEach(detail -> detail.setInvoiceId(invoiceId));

        // Guardar los detalles
        List<InvoiceDetailModel> savedDetails = saveInvoiceDetails(detailDTOs);
        invoice.setDetails(savedDetails);

        return invoiceRepository.save(invoice);
    }

    @Override
    public List<InvoiceDetailModel> saveInvoiceDetails(List<InvoiceDetailRequestDTO> detailDTOs) {
        List<InvoiceDetailModel> details = detailDTOs.stream()
                .map(invoiceDetailMapper::requestDtoToModel)
                .collect(Collectors.toList());

        return invoiceDetailRepository.saveAll(details);
    }

    @Override
    public List<InvoiceResponseDTO> getInvoicesByUserId(Long userId) {
        List<InvoiceModel> invoices = invoiceRepository.findByUserId_Id(userId);
        return invoices.stream()
                .map(invoiceMapper::modelToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceResponseDTO> getInvoicesByClientId(Long clientId) {
        List<InvoiceModel> invoices = invoiceRepository.findByClientId_Id(clientId);
        return invoices.stream()
                .map(invoiceMapper::modelToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceResponseDTO> getInvoicesByEnterpriseId(Long enterpriseId) {
        List<InvoiceModel> invoices = invoiceRepository.findByEnterpriseId_Id(enterpriseId);
        return invoices.stream()
                .map(invoiceMapper::modelToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceResponseDTO getLastInvoiceByEnterpriseId(Long enterpriseId) {
        InvoiceModel lastInvoice = invoiceRepository.findTopByEnterpriseId_IdOrderByIdDesc(enterpriseId);
        return lastInvoice != null ? invoiceMapper.modelToResponseDto(lastInvoice) : null;
    }

    @Override
    public InvoiceResponseDTO getLastInvoiceByEnterpriseIdandFactura(Long enterpriseId, String pointOulet) {
        InvoiceModel lastInvoice = invoiceRepository
                .findTopByEnterpriseId_IdAndInvoiceTypeAndIssuePointOrderByIdDesc(enterpriseId, "FACTURA", pointOulet);

        return lastInvoice != null ? invoiceMapper.modelToResponseDto(lastInvoice) : null;
    }

    @Override
    public InvoiceResponseDTO getLastInvoiceByEnterpriseIdandFacturaComprobante(Long enterpriseId, String pointOutlet) {
        InvoiceModel lastInvoice = invoiceRepository
                .findTopByEnterpriseId_IdAndInvoiceTypeAndIssuePointOrderByIdDesc(enterpriseId, "SAVED", pointOutlet);

        return lastInvoice != null ? invoiceMapper.modelToResponseDto(lastInvoice) : null;

    }

    @Override
    public List<InvoiceResponseDTO> getAllInvoiceByEnterpriseIdandFacturaComprobante(Long enterpriseId,
            String pointOutlet) {
        List<InvoiceModel> invoices = invoiceRepository.findByEnterpriseId_IdAndInvoiceTypeAndIssuePointOrderByIdDesc(
                enterpriseId,
                "SAVED",
                pointOutlet);

        return invoices.stream().map(invoiceMapper::modelToResponseDto)
                .toList();
    }

    @Override
    public BigDecimal getLastInvoiceTotalByUserAndEnterpriseAndDate(Long userId, Long enterpriseId, LocalDate date) {
        return invoiceRepository.getLastInvoiceTotalByUserAndEnterpriseAndDate(userId, enterpriseId, date);
    }

    @Override
    public InvoiceHeaderDTO getInvoiceWithDetails(Long invoiceId) {
        InvoiceModel invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
        // String nombreEmpresa =
        // enterpriseRepository.findById(invoice.getEnterpriseId().getId())
        // .orElseThrow(()-> new RuntimeException("Empresa no
        // encontrada")).getEnterpriseName();
        EnterpriseModel enterprise = enterpriseRepository.findById(invoice.getEnterpriseId().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));

        ClientModel client = null;

        List<InvoiceHeaderDetailDTO> details = invoice.getDetails().stream().map(d -> {
            InvoiceHeaderDetailDTO dto = new InvoiceHeaderDetailDTO();
            dto.setId(d.getId());
            dto.setCantidad(d.getQuantity());
            dto.setDescripcion(d.getDescription());
            dto.setTotalValue(d.getTotalValue());
            dto.setPrecioUnitario(d.getUnitValue());
            dto.setPrecioTotalSinImpuestoalueWithoutTax(d.getTotalValueWithoutTax());
            return dto;
        }).toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        InvoiceHeaderDTO response = new InvoiceHeaderDTO();

        response.setId(invoice.getId());
        response.setEnterpriseName(enterprise.getEnterpriseName());

        response.setRucEnterprise(enterprise.getEnterpriseIdentification());
        response.setFechaAutorizacion(invoice.getDateCreated().format(formatter));

        if (invoice.getInvoiceType().equals("FACTURA")) {

            client = clientRepository.findById(invoice.getClientId().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Emitter no encontrado"));

            response.setClientFullName(client.getClientFullName());
            response.setClientRuc(client.getClientRuc());
            response.setClientAddress(client.getClientAddress());
            String addressEmpresa = emitterRepository
                    .findByEnterpriseId_IdAndEmitterCodEstb(invoice.getEnterpriseId().getId(),
                            invoice.getEstablishment())
                    .orElseThrow(() -> new ResourceNotFoundException("Emitter no encontrado"))
                    .getEmitterDirMatriz();

            response.setEstablishmentAddress(addressEmpresa);
            response.setEstablishment(invoice.getEstablishment());
            response.setRemissionGuide(invoice.getRemissionGuide());
            response.setSequential(invoice.getSequential());
            response.setAccessKey(invoice.getAccessKey());

        } else {

            response.setClientFullName("COMPROBANTE DE VENTA");
            response.setClientRuc("COMPROBANTE DE VENTA");
            response.setClientAddress("COMPROBANTE DE VENTA");
        }
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setInvoiceTotal(invoice.getInvoiceTotal());
        response.setInvoiceDiscount(invoice.getInvoiceDiscount());
        response.setInvoiceSubtotal(invoice.getInvoiceSubtotal());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setDetalles(details);

        return response;
    }

    @Override
    public List<InvoiceResponseDTO> getInvoicesByEnterpriseIdAndDate(Long enterpriseId, LocalDate date) {
        // TODO Auto-generated method stub
        List<InvoiceModel> invoices = invoiceRepository.findByEnterpriseId_IdAndInvoiceDate(enterpriseId, date);
        return invoices.stream()
                .map(invoiceMapper::modelToResponseDto)
                .collect(Collectors.toList());
    }
}
