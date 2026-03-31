package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.common.exception.BadRequestException;
import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.common.response.GessaApiResponse;
import com.izenshy.gessainvoice.modules.email.dto.EmailRequestDTO;
import com.izenshy.gessainvoice.modules.email.service.EmailConfigService;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.EnterpriseRepository;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.OutletRepository;
import com.izenshy.gessainvoice.modules.enterprises.emitter.model.EmitterModel;
import com.izenshy.gessainvoice.modules.enterprises.emitter.repository.EmitterRepository;
import com.izenshy.gessainvoice.modules.invoice.dto.PdfDetalleDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.PdfDocumentDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.PdfFormaPagoDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.PdfSubDetallesDTO;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceDetailModel;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.invoice.repository.InvoiceDetailRepository;
import com.izenshy.gessainvoice.modules.invoice.repository.InvoiceRepository;
import com.izenshy.gessainvoice.modules.invoice.service.pdf.PdfGeneratorService;
import com.izenshy.gessainvoice.modules.person.client.model.ClientModel;
import com.izenshy.gessainvoice.modules.person.client.repository.ClientRepository;
import com.izenshy.gessainvoice.modules.product.product.model.ProductModel;
import com.izenshy.gessainvoice.modules.product.product.repository.ProductRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("api/v1/gessa/email")
@Tag(name = "Email", description = "Esta sección es dedicada a las operaciones relacionadas con el envío de correos electrónicos")
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class EmailController {

    private final EmailConfigService emailConfigService;
    private final InvoiceRepository invoiceRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final ClientRepository clientRepository;
    private final OutletRepository outletRepository;
    private final EmitterRepository emitterModelRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final ProductRepository productRepository;
    private final PdfGeneratorService pdfGeneratorService;

    @Autowired
    public EmailController(EmailConfigService emailConfigService,
                           InvoiceRepository invoiceRepository, EnterpriseRepository enterpriseRepository, ClientRepository clientRepository,
                           OutletRepository outletRepository,
                           EmitterRepository emitterModelRepository, InvoiceDetailRepository invoiceDetailRepository, ProductRepository productRepository, PdfGeneratorService pdfGeneratorService) {
        this.emailConfigService = emailConfigService;
        this.invoiceRepository = invoiceRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.clientRepository = clientRepository;
        this.outletRepository = outletRepository;
        this.emitterModelRepository = emitterModelRepository;
        this.invoiceDetailRepository = invoiceDetailRepository;
        this.productRepository = productRepository;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @PostMapping("/send-with-attachment/{enterpriseId}")
    public ResponseEntity<GessaApiResponse<String>> sendEmailWithAttachment(
            @PathVariable Long enterpriseId,
            @RequestBody EmailRequestDTO emailRequest) {

        try {
            byte[] pdfBytes = Base64.getDecoder().decode(emailRequest.getPdfBase64());
            emailConfigService.sendEmailWithAttachment(enterpriseId, emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody(), pdfBytes);

            GessaApiResponse<String> response = new GessaApiResponse<>();
            response.setSuccess(true);
            response.setMessage("Correo enviado exitosamente");
            response.setData("Correo enviado a " + emailRequest.getTo());

            return ResponseEntity.ok(response);

        } catch (BadRequestException e) {
            GessaApiResponse<String> response = new GessaApiResponse<>();
            response.setSuccess(false);
            response.setMessage("Error al decodificar el PDF en base64: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            GessaApiResponse<String> response = new GessaApiResponse<>();
            response.setSuccess(false);
            response.setMessage("Error al enviar el correo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/send-with-attachmentic-invoice/{enterpriseId}/{invoiceId}")
    public ResponseEntity<GessaApiResponse<String>> sendEmailWithAttachmentInvoice(
            @PathVariable Long enterpriseId,
            @PathVariable Long invoiceId) {

        try {
            InvoiceModel invoiceModel = invoiceRepository.findById(invoiceId)
                    .orElseThrow(()-> new ResourceNotFoundException("NO hay facturas con ese Id"));

            EnterpriseModel enterpriseModel = enterpriseRepository.findById(invoiceModel.getEnterpriseId().getId())
                    .orElseThrow(()-> new ResourceNotFoundException("NO hay empresas con ese Id"));

            EmitterModel emitterModel = emitterModelRepository.findByEmitterRucAndEmitterStatusTrue(enterpriseModel.getEnterpriseIdentification())
                    .orElseThrow(()-> new ResourceNotFoundException("NO hay emitters con ese Id"));


            ClientModel clientModel = clientRepository.findById(invoiceModel.getClientId().getId())
                    .orElseThrow(()-> new ResourceNotFoundException("NO hay clientes con ese Id"));

            OutletModel outletModel = outletRepository.findById(Long.parseLong(invoiceModel.getIssuePoint()))
                    .orElseThrow(()-> new ResourceNotFoundException("NO hay puntos de venta con ese Id"));


            //MI EMPRESA
            PdfDocumentDTO newDocumento = new PdfDocumentDTO();
            newDocumento.setNombrePrincipal(enterpriseModel.getEnterpriseOwnerName());
            newDocumento.setNombreEmpresa(enterpriseModel.getEnterpriseName());
            newDocumento.setDireccionMatriz(outletModel.getOutletAddress());
            newDocumento.setDireccionSucursal(outletModel.getOutletAddress());

            newDocumento.setContabilidad("NO");
            newDocumento.setRuc(enterpriseModel.getEnterpriseIdentification());
            newDocumento.setNfactura(invoiceModel.getRemissionGuide()+"-"+invoiceModel.getEstablishment()+"-"+invoiceModel.getSequential());
            newDocumento.setNautorizacion(invoiceModel.getAccessKey());
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            newDocumento.setFechaautorizacion(
                    invoiceModel.getDateCreated().format(formatter)
            );
            if(emitterModel.getEmitterAmbiente().equals("1")){
                newDocumento.setAmbiente("PRUEBAS");
            }else{
                newDocumento.setAmbiente("PRODUCCION");
            }

            newDocumento.setEmision("NORMAL");

            newDocumento.setNombreApellidos(clientModel.getClientFullName());
            newDocumento.setIdentificacion(clientModel.getClientIdentification());
            newDocumento.setFecha(invoiceModel.getDateCreated().format(formatter));
            newDocumento.setDireccion(clientModel.getClientAddress());

            //PRODUCTOS DETALLES

            List<InvoiceDetailModel> finddetalles = invoiceDetailRepository.findByInvoice_Id(invoiceId);
                List<PdfDetalleDTO> listadatosFactura = new ArrayList<>();


            for(InvoiceDetailModel invoiceDetailModel: finddetalles){
                ProductModel productModel = productRepository.findById(invoiceDetailModel.getStock().getId().getProductId())
                                .orElseThrow(()-> new ResourceNotFoundException("Producto no encontrado"));

                PdfDetalleDTO datosFactura = new PdfDetalleDTO();

                datosFactura.setCodPrincipal(productModel.getProductCode());
                datosFactura.setDescripcion(invoiceDetailModel.getDescription());
                datosFactura.setCantidad(invoiceDetailModel.getQuantity().toString());
                datosFactura.setPrecioUnit(invoiceDetailModel.getUnitValue().toString());
                datosFactura.setPrecioTotal(invoiceDetailModel.getTotalValue().toString());
                datosFactura.setSubsidio("0.00");
                datosFactura.setPreciosinSubsidio("0.00");
                datosFactura.setDescuento("0.00");

                listadatosFactura.add(datosFactura);
            }

            //SUBTOTAL
            List<PdfSubDetallesDTO> listadetalleDTOS = new ArrayList<>();
            PdfSubDetallesDTO pdfSubDetallesDTO = new PdfSubDetallesDTO();

            pdfSubDetallesDTO.setSubtotal15porciento(invoiceModel.getInvoiceSubtotal().toString());
            pdfSubDetallesDTO.setNoobjiva("0.00");
            pdfSubDetallesDTO.setNoextiva("0.00");
            BigDecimal subSinImpuestos = invoiceModel.getInvoiceTotal().subtract(invoiceModel.getInvoiceSubtotal());
            pdfSubDetallesDTO.setSinimpuesto(invoiceModel.getInvoiceSubtotal().toString());
            pdfSubDetallesDTO.setTotaldesc("0.00");
            pdfSubDetallesDTO.setIce("0.00");
            pdfSubDetallesDTO.setIva15(subSinImpuestos.toString());
            pdfSubDetallesDTO.setIrbpnr("0.00");
            pdfSubDetallesDTO.setPropina("0.00");
            pdfSubDetallesDTO.setValortotal(invoiceModel.getInvoiceTotal().toString());

            listadetalleDTOS.add(pdfSubDetallesDTO);

            //PAGOS
            List<PdfFormaPagoDTO> listaFormaPagoDTOS = new ArrayList<>();
            PdfFormaPagoDTO  pdfFormaPagoDTO = new PdfFormaPagoDTO();

            pdfFormaPagoDTO.setFormaPago("20 - OTROS CON UTILIZACION DEL SISTEMA\n" +
                    "FINANCIERO");
            pdfFormaPagoDTO.setValorpago(invoiceModel.getInvoiceTotal().toString());

            listaFormaPagoDTOS.add(pdfFormaPagoDTO);

            newDocumento.setDatosFactura(listadatosFactura);
            newDocumento.setDetalleDTOS(listadetalleDTOS);
            newDocumento.setFormaPagoDTOS(listaFormaPagoDTOS);

            byte [] pdfBytes = pdfGeneratorService.generatePdfFromBillingHtml(newDocumento);

            //byte [] pdfBytes = pdfGeneratorService.generatePdfFromHtml(pdfBytesString);

             emailConfigService.sendEmailWithAttachmentInvoice(enterpriseId, clientModel.getClientEmail(), pdfBytes);


            GessaApiResponse<String> response = new GessaApiResponse<>();
            response.setSuccess(true);
            response.setMessage("Correo enviado exitosamente");
            response.setData(null);

            return ResponseEntity.ok(response);

        } catch (BadRequestException e) {
            GessaApiResponse<String> response = new GessaApiResponse<>();
            response.setSuccess(false);
            response.setMessage("Error al decodificar el PDF en base64: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            GessaApiResponse<String> response = new GessaApiResponse<>();
            response.setSuccess(false);
            response.setMessage("Error al enviar el correo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}