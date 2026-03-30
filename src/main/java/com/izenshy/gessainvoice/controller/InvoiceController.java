package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.common.response.GessaApiResponse;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.PdfDocumentDTO;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceService;
import com.izenshy.gessainvoice.modules.invoice.service.pdf.PdfGeneratorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gessa/invoices")
@Tag(name = "Invoice", description = "Esta sección es dedicada a las operaciones relacionadas de facturas del sistema")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfGeneratorService pdfGeneratorService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService, PdfGeneratorService pdfGeneratorService) {
        this.invoiceService = invoiceService;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @PostMapping
    public ResponseEntity<GessaApiResponse<InvoiceResponseDTO>> createInvoice(@RequestBody InvoiceRequestDTO requestDTO) {
        try {
            var invoice = invoiceService.saveInvoiceDTO(requestDTO);
            var responseDTO = invoiceService.getInvoicesByUserId(invoice.getId()).get(0); // Get the created invoice
            return ResponseEntity.ok(GessaApiResponse.success("Invoice created successfully", responseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error creating invoice: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GessaApiResponse<InvoiceResponseDTO>> updateInvoice(@PathVariable Long id, @RequestBody InvoiceRequestDTO requestDTO) {
        try {
            var invoice = invoiceService.updateInvoiceDTO(id, requestDTO);
            var responseDTO = invoiceService.getInvoicesByUserId(invoice.getId()).get(0);
            return ResponseEntity.ok(GessaApiResponse.success("Invoice updated successfully", responseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error updating invoice: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GessaApiResponse<InvoiceResponseDTO>> getInvoiceById(@PathVariable Long id) {
        try {
            var invoice = invoiceService.getInvoiceById(id);
            var responseDTO = invoiceService.getInvoicesByUserId(invoice.getId()).get(0);
            return ResponseEntity.ok(GessaApiResponse.success("Invoice retrieved successfully", responseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error retrieving invoice: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<GessaApiResponse<List<InvoiceResponseDTO>>> getAllInvoices() {
        try {
            var invoices = invoiceService.getAllInvoices();
            var responseDTOs = invoices.stream()
                    .map(invoice -> invoiceService.getInvoicesByUserId(invoice.getId()).get(0))
                    .toList();
            return ResponseEntity.ok(GessaApiResponse.success("Invoices retrieved successfully", responseDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error retrieving invoices: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GessaApiResponse<Void>> deleteInvoice(@PathVariable Long id) {
        try {
            invoiceService.deleteInvoice(id);
            return ResponseEntity.ok(GessaApiResponse.success("Invoice deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error deleting invoice: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<GessaApiResponse<List<InvoiceResponseDTO>>> getInvoicesByUserId(@PathVariable Long userId) {
        try {
            var invoices = invoiceService.getInvoicesByUserId(userId);
            return ResponseEntity.ok(GessaApiResponse.success("Invoices retrieved successfully", invoices));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error retrieving invoices: " + e.getMessage()));
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<GessaApiResponse<List<InvoiceResponseDTO>>> getInvoicesByClientId(@PathVariable Long clientId) {
        try {
            var invoices = invoiceService.getInvoicesByClientId(clientId);
            return ResponseEntity.ok(GessaApiResponse.success("Invoices retrieved successfully", invoices));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error retrieving invoices: " + e.getMessage()));
        }
    }

    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<GessaApiResponse<List<InvoiceResponseDTO>>> getInvoicesByEnterpriseId(@PathVariable Long enterpriseId) {
        try {
            var invoices = invoiceService.getInvoicesByEnterpriseId(enterpriseId);
            return ResponseEntity.ok(GessaApiResponse.success("Invoices retrieved successfully", invoices));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error retrieving invoices: " + e.getMessage()));
        }
    }
    @GetMapping("/last/{enterpriseId}/{pointOutlet}")
    public ResponseEntity<InvoiceResponseDTO> getLastInvoiceByEnterpriseIdAndFactura(
            @PathVariable Long enterpriseId,
            @PathVariable String pointOutlet) {

        InvoiceResponseDTO response = invoiceService.getLastInvoiceByEnterpriseIdandFactura(enterpriseId, pointOutlet);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/generar-pdf/sri")
    public ResponseEntity<byte[]> generarFacturaPdf(@RequestBody PdfDocumentDTO data) {
        try {
            // Llamamos al servicio pasando el DTO completo
            byte[] pdfBytes = pdfGeneratorService.exportReport(data);

            // Configuramos los encabezados HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // "inline" para que se abra en el navegador, "attachment" para forzar descarga
            headers.setContentDispositionFormData("inline", "factura_" + data.getNfactura() + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/generar-pdf/billing")
    public ResponseEntity<byte[]> generarBillingPdf(@RequestBody PdfDocumentDTO data) {
        try {
            // Llamamos al servicio pasando el DTO completo
            byte[] pdfBytes = pdfGeneratorService.generatePdfFromBillingHtml(data);

            // Configuramos los encabezados HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // "inline" para que se abra en el navegador, "attachment" para forzar descarga
            headers.setContentDispositionFormData("inline", "factura_" + data.getNfactura() + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/ultima-venta-usuario")
    public ResponseEntity<GessaApiResponse<BigDecimal>> getLastInvoiceTotal(
            @RequestParam Long userId,
            @RequestParam Long enterpriseId,
            @RequestParam String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate localDate = LocalDate.parse(date, formatter);
            BigDecimal total = invoiceService.getLastInvoiceTotalByUserAndEnterpriseAndDate(userId, enterpriseId, localDate);
            return ResponseEntity.ok(GessaApiResponse.success("Last invoice total retrieved successfully", total));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(GessaApiResponse.error("Error retrieving last invoice total: " + e.getMessage()));
        }
    }
}