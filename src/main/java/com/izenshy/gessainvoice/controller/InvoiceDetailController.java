package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.common.response.GessaApiResponse;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceDetailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gessa/invoice-details")
@Tag(name = "Invoice detail", description = "Esta sección es dedicada a las operaciones relacionadas de facturas del sistema")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class InvoiceDetailController {

    private final InvoiceDetailService invoiceDetailService;

    @Autowired
    public InvoiceDetailController(InvoiceDetailService invoiceDetailService) {
        this.invoiceDetailService = invoiceDetailService;
    }

    @PostMapping("/invoice/{invoiceId}")
    public ResponseEntity<GessaApiResponse<InvoiceDetailResponseDTO>> createInvoiceDetail(
            @PathVariable Long invoiceId,
            @RequestBody InvoiceDetailRequestDTO requestDTO) {
        try {
            var detail = invoiceDetailService.saveInvoiceDetailDTO(requestDTO, invoiceId);
            var responseDTO = invoiceDetailService.getInvoiceDetailsByInvoiceId(invoiceId)
                    .stream()
                    .filter(d -> d.getId().equals(detail.getId()))
                    .findFirst()
                    .orElse(null);
            return ResponseEntity.ok(GessaApiResponse.success("Invoice detail created successfully", responseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error creating invoice detail: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GessaApiResponse<InvoiceDetailResponseDTO>> updateInvoiceDetail(
            @PathVariable Long id,
            @RequestBody InvoiceDetailRequestDTO requestDTO) {
        try {
            var detail = invoiceDetailService.updateInvoiceDetailDTO(id, requestDTO);
            var responseDTO = invoiceDetailService.getInvoiceDetailsByInvoiceId(detail.getInvoice().getId())
                    .stream()
                    .filter(d -> d.getId().equals(detail.getId()))
                    .findFirst()
                    .orElse(null);
            return ResponseEntity.ok(GessaApiResponse.success("Invoice detail updated successfully", responseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error updating invoice detail: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GessaApiResponse<InvoiceDetailResponseDTO>> getInvoiceDetailById(@PathVariable Long id) {
        try {
            var detail = invoiceDetailService.getInvoiceDetailById(id);
            var responseDTO = invoiceDetailService.getInvoiceDetailsByInvoiceId(detail.getInvoice().getId())
                    .stream()
                    .filter(d -> d.getId().equals(detail.getId()))
                    .findFirst()
                    .orElse(null);
            return ResponseEntity.ok(GessaApiResponse.success("Invoice detail retrieved successfully", responseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error retrieving invoice detail: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<GessaApiResponse<List<InvoiceDetailResponseDTO>>> getAllInvoiceDetails() {
        try {
            var details = invoiceDetailService.getAllInvoiceDetails();
            var responseDTOs = details.stream()
                    .map(detail -> invoiceDetailService.getInvoiceDetailsByInvoiceId(detail.getInvoice().getId())
                            .stream()
                            .filter(d -> d.getId().equals(detail.getId()))
                            .findFirst()
                            .orElse(null))
                    .toList();
            return ResponseEntity.ok(GessaApiResponse.success("Invoice details retrieved successfully", responseDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error retrieving invoice details: " + e.getMessage()));
        }
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<GessaApiResponse<List<InvoiceDetailResponseDTO>>> getInvoiceDetailsByInvoiceId(@PathVariable Long invoiceId) {
        try {
            var details = invoiceDetailService.getInvoiceDetailsByInvoiceId(invoiceId);
            return ResponseEntity.ok(GessaApiResponse.success("Invoice details retrieved successfully", details));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error retrieving invoice details: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GessaApiResponse<Void>> deleteInvoiceDetail(@PathVariable Long id) {
        try {
            invoiceDetailService.deleteInvoiceDetail(id);
            return ResponseEntity.ok(GessaApiResponse.success("Invoice detail deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error deleting invoice detail: " + e.getMessage()));
        }
    }
}