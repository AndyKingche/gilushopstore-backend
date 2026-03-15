package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.common.response.GessaPDFResponse;
import com.izenshy.gessainvoice.modules.enterprises.certificate.service.DigitalCertificateService;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceService;
import org.springframework.http.HttpHeaders;
import com.izenshy.gessainvoice.sri.invoice.FacturaSRI;
import com.izenshy.gessainvoice.sri.invoice.FacturaSRIDTO;
import com.izenshy.gessainvoice.sri.service.SriInvoiceProcessingService;
import com.izenshy.gessainvoice.sri.signature.service.XmlSignatureService;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.io.StringWriter;
import java.util.List;


@RestController
@RequestMapping("api/v1/gessa/sri/")
@CrossOrigin("*")
@Tag(name = "SRI", description = "Esta sección es dedicada a las operaciones relacionadas con los procesos de uso del SRI")
public class SRIController {

    private XmlSignatureService xmlSignerService;
    private DigitalCertificateService digitalCertificateService;
    private SriInvoiceProcessingService sriInvoiceProcessingService;
    private InvoiceService invoiceService;

    @Autowired
    public SRIController(XmlSignatureService xmlSignerService,
                          DigitalCertificateService digitalCertificateService,
                          SriInvoiceProcessingService sriInvoiceProcessingService,
                         InvoiceService invoiceService) {
        this.xmlSignerService = xmlSignerService;
        this.digitalCertificateService = digitalCertificateService;
        this.sriInvoiceProcessingService = sriInvoiceProcessingService;
        this.invoiceService = invoiceService;
    }

    @PostMapping("invoice-builder")
    public ResponseEntity<String> invoiceBuilder(@RequestBody FacturaSRI facturaSRI) {
        try {
            StringWriter stringWriter = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(FacturaSRI.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(facturaSRI, stringWriter);
            String xmlFactura = stringWriter.toString();
            return ResponseEntity.ok(xmlFactura);
        } catch (Exception e) {
            System.out.println("ERROR"+e.getMessage());
            throw new RuntimeException(e);
        }

    }

    //Esta es la que estoy usando
    @PostMapping("invoice-signer")
    public ResponseEntity<String> invoiceSigner(@RequestBody FacturaSRIDTO facturaSRIDTO) {
        try {

            byte[] firmado = xmlSignerService.signerInvoice(facturaSRIDTO);

            return ResponseEntity.ok(new String(firmado));

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al firmar XML", e);
        }
    }

    @PostMapping(path = "save-certificate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> subirCertificado(@RequestParam("userId") String userId,
                                                    @RequestParam("password") String password,
                                                    @RequestParam("dateExpired") String dateExpiredStr,
                                                    @RequestParam("file") MultipartFile file) {
        try {

            digitalCertificateService.saveCertificateCI(Long.valueOf(userId), password, dateExpiredStr, file.getBytes());
            return ResponseEntity.ok("Certificado guardado correctamente para RUC: " + userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar certificado: " + e.getMessage());
        }
    }

    @PostMapping("process-invoice/{userId}/{pointOutletId}/{enterpriseId}")
    public ResponseEntity<GessaPDFResponse> processInvoice(@PathVariable("userId") Long userId,
                                                           @PathVariable("pointOutletId") Long pointOutletId,
                                                           @PathVariable("enterpriseId") Long enterpriseId,
                                                           @RequestBody FacturaSRIDTO facturaSRIDTO) {
        try {
           // byte[] ticketPdf = sriInvoiceProcessingService.processInvoiceDeluxe(facturaSRIDTO, userId, pointOutletId, enterpriseId).getData();
            GessaPDFResponse ticketPdf = sriInvoiceProcessingService.processInvoiceDeluxe(facturaSRIDTO, userId, pointOutletId, enterpriseId);


            /*
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ticket.pdf");
*/
            return ResponseEntity.ok(ticketPdf);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar la factura con SRI", e);
        }
    }

    @PostMapping("process-invoice-update/{userId}/{pointOutletId}/{enterpriseId}/{invoiceId}")
    public ResponseEntity<byte[]> processInvoiceUpdate(@PathVariable("userId") Long userId,
                                                 @PathVariable("pointOutletId") Long pointOutletId,
                                                 @PathVariable("enterpriseId") Long enterpriseId,
                                                       @PathVariable("invoiceId") Long invoiceId,
                                                 @RequestBody FacturaSRIDTO facturaSRIDTO) {
        try {
            byte[] ticketPdf = sriInvoiceProcessingService.processUpdateInvoiceDeluxe(facturaSRIDTO, userId, pointOutletId, enterpriseId, invoiceId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ticket.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(ticketPdf);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar la factura con SRI", e);
        }
    }

    @PostMapping("process-voucher/{userId}/{pointOutletId}/{enterpriseId}")
    public ResponseEntity<byte[]> processVoucher(@PathVariable("userId") Long userId,
                                                 @PathVariable("pointOutletId") Long pointOutletId,
                                                 @PathVariable("enterpriseId") Long enterpriseId,
                                                 @RequestBody FacturaSRIDTO facturaSRIDTO) {
        try {
            byte[] ticketPdf = sriInvoiceProcessingService.processVoucherDeluxe(facturaSRIDTO, userId, pointOutletId, enterpriseId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ticket.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(ticketPdf);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar la factura con SRI", e);
        }
    }

    @PostMapping("process-voucher-update/{userId}/{pointOutletId}/{enterpriseId}/{invoiceId}")
    public ResponseEntity<byte[]> processVoucherUpdate(@PathVariable("userId") Long userId,
                                                 @PathVariable("pointOutletId") Long pointOutletId,
                                                 @PathVariable("enterpriseId") Long enterpriseId,
                                                       @PathVariable("invoiceId") Long invoiceId,
                                                 @RequestBody FacturaSRIDTO facturaSRIDTO) {
        try {
            byte[] ticketPdf = sriInvoiceProcessingService.processVoucherUpdateDeluxe(facturaSRIDTO, userId, pointOutletId, enterpriseId, invoiceId
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ticket.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(ticketPdf);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar la factura con SRI", e);
        }
    }

    @PostMapping("process-saved/{userId}/{pointOutletId}/{enterpriseId}/{nameSaved}")
    public ResponseEntity<InvoiceModel> processSaved(@PathVariable("userId") Long userId,
                                                     @PathVariable("pointOutletId") Long pointOutletId,
                                                     @PathVariable("enterpriseId") Long enterpriseId,
                                                     @PathVariable("nameSaved") String nameSaved,
                                                     @RequestBody FacturaSRIDTO facturaSRIDTO) {
        try {

            InvoiceModel nuevoComprobante = sriInvoiceProcessingService.processSavedSaleDeluxe(facturaSRIDTO,userId,pointOutletId,enterpriseId, nameSaved);

            return ResponseEntity.ok(nuevoComprobante);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar el comprobante", e);
        }
    }

    @PostMapping("process-saved-updated/{userId}/{pointOutletId}/{enterpriseId}/{invoiceId}")
    public ResponseEntity<InvoiceModel> processSavedUpdated(@PathVariable("userId") Long userId,
                                                     @PathVariable("pointOutletId") Long pointOutletId,
                                                     @PathVariable("enterpriseId") Long enterpriseId,
                                                            @PathVariable("invoiceId") Long invoiceId,
                                                     @RequestBody FacturaSRIDTO facturaSRIDTO) {
        try {

            InvoiceModel nuevoComprobante = sriInvoiceProcessingService.processUpdatedSaleDeluxe(facturaSRIDTO,userId,pointOutletId,enterpriseId, invoiceId);

            return ResponseEntity.ok(nuevoComprobante);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar el comprobante", e);
        }
    }

    @GetMapping("get-sale-saved/{pointOutletId}/{enterpriseId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getSavedSale(
                                                     @PathVariable("pointOutletId") Long pointOutletId,
                                                     @PathVariable("enterpriseId") Long enterpriseId
                                                     ) {
        try {

            List<InvoiceResponseDTO> nuevoComprobante = invoiceService.getAllInvoiceByEnterpriseIdandFacturaComprobante(enterpriseId, String.valueOf(pointOutletId));

            return ResponseEntity.ok(nuevoComprobante);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener la lista de las compras guardadas realizadas", e);
        }
    }

}
