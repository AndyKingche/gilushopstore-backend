package com.izenshy.gessainvoice.modules.invoice.service.impl;

import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.mapper.InvoiceDetailMapper;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceDetailModel;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.invoice.repository.InvoiceDetailRepository;
import com.izenshy.gessainvoice.modules.invoice.repository.InvoiceRepository;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceDetailService;
import com.izenshy.gessainvoice.modules.product.stock.model.StockPKModel;
import com.izenshy.gessainvoice.modules.product.stock.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceDetailServiceImpl implements InvoiceDetailService {

    private final InvoiceDetailRepository invoiceDetailRepository;
    private final InvoiceDetailMapper invoiceDetailMapper;
    private final StockRepository stockRepository;
    private final InvoiceRepository invoiceRepository;

    @Autowired
    public InvoiceDetailServiceImpl(InvoiceDetailRepository invoiceDetailRepository,
                                    InvoiceDetailMapper invoiceDetailMapper,
                                    StockRepository stockRepository, InvoiceRepository invoiceRepository) {
        this.invoiceDetailRepository = invoiceDetailRepository;
        this.invoiceDetailMapper = invoiceDetailMapper;
        this.stockRepository = stockRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public InvoiceDetailModel saveInvoiceDetail(InvoiceDetailModel invoiceDetail) {
        return invoiceDetailRepository.save(invoiceDetail);
    }


    @Override
    public InvoiceDetailModel saveInvoiceDetailDTO(InvoiceDetailRequestDTO invoiceDetailRequestDTO, Long invoiceId) {
        // Validate stock exists
        if (invoiceDetailRequestDTO.getStockProductId() != null && invoiceDetailRequestDTO.getStockOutletId() != null) {
            boolean stockExists = stockRepository
                    .findByIdProductIdAndIdOutletId(
                            invoiceDetailRequestDTO.getStockProductId(),
                            invoiceDetailRequestDTO.getStockOutletId()
                    )
                    .isPresent();
            if (!stockExists) {
                throw new RuntimeException("Stock does not exist");
            }
        }

        InvoiceDetailModel detail = invoiceDetailMapper.requestDtoToModel(invoiceDetailRequestDTO);
        InvoiceModel invoice = new InvoiceModel();
        invoice.setId(invoiceId);
        detail.setInvoice(invoice);

        return invoiceDetailRepository.save(detail);
    }

    @Override
    public void saveInvoiceDetails(List<InvoiceDetailRequestDTO> details, InvoiceModel parentInvoice) {
        List<InvoiceDetailModel> models = details.stream()
                .map(d -> {
                    InvoiceDetailModel model = invoiceDetailMapper.requestDtoToModel(d);
                    model.setInvoice(parentInvoice); // Establecer la relación
                    return model;
                })
                .toList();

        invoiceDetailRepository.saveAll(models);
    }

    @Override
    public InvoiceDetailModel updateInvoiceDetailDTO(Long id, InvoiceDetailRequestDTO invoiceDetailRequestDTO) {
        // Validate stock exists
        if (invoiceDetailRequestDTO.getStockProductId() != null && invoiceDetailRequestDTO.getStockOutletId() != null) {
            boolean stockExists = stockRepository
                    .findByIdProductIdAndIdOutletId(
                            invoiceDetailRequestDTO.getStockProductId(),
                            invoiceDetailRequestDTO.getStockOutletId()
                    )
                    .isPresent();
            if (!stockExists) {
                throw new RuntimeException("Stock does not exist");
            }
        }

        InvoiceDetailModel existingDetail = invoiceDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Invoice detail not found with id " + id
                ));

        invoiceDetailMapper.updateModelFromDto(invoiceDetailRequestDTO, existingDetail);
        return invoiceDetailRepository.save(existingDetail);
    }

    @Override
    public InvoiceDetailModel getInvoiceDetailById(Long id) {
        return invoiceDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice detail not found with id " + id));
    }

    @Override
    public List<InvoiceDetailModel> getAllInvoiceDetails() {
        return invoiceDetailRepository.findAll();
    }

    @Override
    public List<InvoiceDetailResponseDTO> getInvoiceDetailsByInvoiceId(Long invoiceId) {
        List<InvoiceDetailModel> details = invoiceDetailRepository.findByInvoice_Id(invoiceId);
        return details.stream()
                .map(invoiceDetailMapper::modelToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteInvoiceDetail(Long id) {
        if (!invoiceDetailRepository.existsById(id)) {
            throw new RuntimeException("Invoice detail not found with id " + id);
        }
        invoiceDetailRepository.deleteById(id);
    }

    @Override
    public InvoiceDetailModel updatedInvoiceDetailDTO(List<InvoiceDetailRequestDTO> dtoList, Long invoiceId) {

        InvoiceDetailModel lastSaved = null;

        for (InvoiceDetailRequestDTO dto : dtoList) {

            // Validar stock
            if (dto.getStockProductId() != null && dto.getStockOutletId() != null) {
                boolean stockExists = stockRepository
                        .findByIdProductIdAndIdOutletId(
                                dto.getStockProductId(),
                                dto.getStockOutletId()
                        )
                        .isPresent();

                if (!stockExists) {
                    throw new RuntimeException("Stock does not exist");
                }
            }

            // UPDATE
            if (dto.getId() != null) {
                InvoiceDetailModel existing = invoiceDetailRepository.findById(dto.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invoice detail not found with id " + dto.getId()
                                )
                        );

                invoiceDetailMapper.updateModelFromDto(dto, existing);
                // NO tocar id, uuid, invoice

                lastSaved = invoiceDetailRepository.save(existing);
                continue;
            }

            // CREATE
            InvoiceDetailModel detail = invoiceDetailMapper.requestDtoToModel(dto);
            InvoiceModel invoice = new InvoiceModel();
            invoice.setId(invoiceId);
            detail.setInvoice(invoice);

            lastSaved = invoiceDetailRepository.save(detail);
        }

        return lastSaved;
    }
}