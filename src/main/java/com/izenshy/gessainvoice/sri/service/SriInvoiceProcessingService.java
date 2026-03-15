package com.izenshy.gessainvoice.sri.service;

import com.izenshy.gessainvoice.common.response.GessaApiResponse;
import com.izenshy.gessainvoice.common.response.GessaPDFResponse;
import com.izenshy.gessainvoice.common.response.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.email.service.EmailConfigService;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.EnterpriseRepository;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceDetailRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.invoice.repository.InvoiceRepository;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceDetailService;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceService;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceTempAuthorizationService;
import com.izenshy.gessainvoice.modules.invoice.service.pdf.PdfGeneratorService;
import com.izenshy.gessainvoice.modules.person.client.dto.ClientRequestDTO;
import com.izenshy.gessainvoice.modules.person.client.dto.ClientResponseDTO;
import com.izenshy.gessainvoice.modules.person.client.mapper.ClientMapper;
import com.izenshy.gessainvoice.modules.person.client.model.ClientModel;
import com.izenshy.gessainvoice.modules.person.client.service.ClientService;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import com.izenshy.gessainvoice.modules.person.user.repository.UserRepository;
import com.izenshy.gessainvoice.modules.product.product.dto.ProductDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.product.model.ProductModel;
import com.izenshy.gessainvoice.modules.product.product.repository.ProductRepository;
import com.izenshy.gessainvoice.modules.product.product.service.ProductService;
import com.izenshy.gessainvoice.modules.product.stock.dto.StockDTO;
import com.izenshy.gessainvoice.modules.product.stock.model.StockModel;
import com.izenshy.gessainvoice.modules.product.stock.repository.StockRepository;
import com.izenshy.gessainvoice.modules.product.stock.service.StockService;
import com.izenshy.gessainvoice.sri.invoice.FacturaSRIDTO;
import com.izenshy.gessainvoice.sri.signature.service.XmlSignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SriInvoiceProcessingService {

    @Autowired
    private XmlSignatureService xmlSignatureService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceDetailService invoiceDetailService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private EmailConfigService emailConfigService;

    @Autowired
    private StockService stockService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Value("${sri.url.RecepcionComprobantesOffline}")
    private String recepcionUrl;

    @Value("${sri.url.autorizacion}")
    private String autorizacionUrl;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnterpriseRepository enterpriseRepository;

    @Autowired
    private ClientMapper clientMapper;

    @Autowired
    @Lazy
    private InvoiceTempAuthorizationService invoiceTempAuthorizationService;


    public InvoiceModel processInvoice(FacturaSRIDTO facturaDto) throws Exception {
        // Paso 1: Firmar el documento
        byte[] signedXmlBytes = xmlSignatureService.signerInvoice(facturaDto);

        // Paso 2: Extraer el valor del secuencial
        String secuencial = extractSecuencialFromXml(new String(signedXmlBytes));

        // Paso 3: Convertir a Base64
        String base64Xml = Base64.getEncoder().encodeToString(signedXmlBytes);

        // Paso 4: Enviar al SRI RecepcionComprobantesOffline
        String recepcionSoapRequest = buildRecepcionSoapRequest(base64Xml);
        String recepcionResponse = sendSoapRequest(recepcionUrl, recepcionSoapRequest);

        // Paso 5: Extraer la claveAcceso si la respuesta es RECIBIDA
        String claveAcceso = extractClaveAcceso(signedXmlBytes);

        if (claveAcceso == null) {
            // NO  fue recibida
            System.out.println("SRI Recepcion Response: " + recepcionResponse);
            throw new RuntimeException("Invoice not received by SRI");
        }

        // Paso 6: Enviar al SRI AutorizacionComprobantes
        String autorizacionSoapRequest = buildAutorizacionSoapRequest(claveAcceso);

        String autorizacionResponse = sendSoapRequest(autorizacionUrl, autorizacionSoapRequest);

        String establecimiento = extractEstabFromXml(new String(signedXmlBytes));
        String puntoEmision = extractPtoEmiFromXml(new String (signedXmlBytes));

        // Paso 7: Revisar si  la factura fue AUTORIZADO
        if (!isAutorizado(autorizacionResponse)) {
            throw new RuntimeException("Invoice not authorized by SRI");
        }else{
            // Paso 8: Guardar la factura en la base de datos
            return saveInvoiceToDatabase(facturaDto, claveAcceso, secuencial);
        }



    }

    public GessaPDFResponse processInvoiceDeluxe(FacturaSRIDTO facturaDto,
                                                 Long userId,
                                                 Long pointOutletId,
                                                 Long enterpriseId) throws Exception {
        // Paso 1: Firmar el documento
        byte[] signedXmlBytes = xmlSignatureService.signerInvoiceDeluxe(facturaDto, String.valueOf(pointOutletId));


        // Paso 2: Extraer el valor del secuencial
        String secuencial = extractSecuencialFromXml(new String(signedXmlBytes));

        // Paso 3: Convertir a Base64
        String base64Xml = Base64.getEncoder().encodeToString(signedXmlBytes);
        System.out.println("******************");
        System.out.println(base64Xml);


        // Paso 4: Enviar al SRI RecepcionComprobantesOffline
        String recepcionSoapRequest = buildRecepcionSoapRequest(base64Xml);
        String recepcionResponse = sendSoapRequest(recepcionUrl, recepcionSoapRequest);


        System.out.println("RECIBIDA? "+recepcionResponse);

        // Paso 5: Extraer la claveAcceso si la respuesta es RECIBIDA
        String claveAcceso = extractClaveAcceso(signedXmlBytes);

        if (claveAcceso == null) {
            // NO  fue recibida
            System.out.println("SRI Recepcion Response: " + recepcionResponse);
            throw new ResourceNotFoundException("La Factura no fue recibida por el  SRI");
        }

        // ========================================================================
        // VALIDAR RESPUESTA DE RECEPCIÓN
        // ========================================================================
        if (recepcionResponse.contains("<estado>DEVUELTA</estado>")) {
            System.out.println("⚠ Comprobante DEVUELTO - Verificando motivo...");

            // Verificar si es error 70 (comprobante en procesamiento)
            if (recepcionResponse.contains("<identificador>70</identificador>")) {
                System.out.println("✓ Error 70 detectado: Comprobante en procesamiento");
                System.out.println("✓ Continuando con el proceso de autorización...");

                String establecimiento = extractEstabFromXml(new String(signedXmlBytes));
                String puntoEmision = extractPtoEmiFromXml(new String (signedXmlBytes));

                InvoiceModel savedInvoice = saveInvoiceToDatabaseDeluxe(facturaDto, claveAcceso, secuencial, userId, pointOutletId, enterpriseId, establecimiento, puntoEmision);

                InvoiceTempAuthorizationRequestDTO invoiceTempAuthorizationRequestDTO = new InvoiceTempAuthorizationRequestDTO();

                invoiceTempAuthorizationRequestDTO.setAccessCode(claveAcceso);
                invoiceTempAuthorizationRequestDTO.setFileBase64(base64Xml);
                invoiceTempAuthorizationRequestDTO.setReceptionStatus("DEVUELTA");
                invoiceTempAuthorizationRequestDTO.setAuthorizationStatus("NO AUTORIZADO");
                invoiceTempAuthorizationRequestDTO.setInvoiceId(savedInvoice.getId());
                invoiceTempAuthorizationRequestDTO.setEnterpriseId(enterpriseId);
                invoiceTempAuthorizationRequestDTO.setOutletId(pointOutletId);

                invoiceTempAuthorizationService.saveFromDTO(invoiceTempAuthorizationRequestDTO);

                throw new ResourceNotFoundException(
                        "Comprobante Creado: Pero debe volver actualizarlo manualmente en la seccion de COMPROBANTES PENDIENTES"
                );

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

            Thread.sleep(5000);

            // Paso 6: Enviar al SRI AutorizacionComprobantes
            String autorizacionSoapRequest = buildAutorizacionSoapRequest(claveAcceso);

            String autorizacionResponse = sendSoapRequest(autorizacionUrl, autorizacionSoapRequest);
            System.out.println("AUTORIZACION? "+autorizacionResponse);

            String numeroAutorizacion = extractNumeroAutorizacion(autorizacionResponse);
            String fechaAutorizacion = extractFechaAutorizacion(autorizacionResponse);

            String establecimiento = extractEstabFromXml(new String(signedXmlBytes));
            String puntoEmision = extractPtoEmiFromXml(new String (signedXmlBytes));

            Thread.sleep(5000);

            // Paso 7: Revisar si  la factura fue AUTORIZADO
            if (!isAutorizado(autorizacionResponse)) {
                throw new ResourceNotFoundException("La Factura no fue autorizada por el SRI");
            }else{
                // Paso 8: Guardar la factura en la base de datos
                InvoiceModel savedInvoice = saveInvoiceToDatabaseDeluxe(facturaDto, claveAcceso, secuencial, userId, pointOutletId, enterpriseId, establecimiento, puntoEmision);

                // Paso 10: Generar PDF y enviar por email
                //generatePdfAndSendEmail(savedInvoice, facturaDto, numeroAutorizacion, fechaAutorizacion, claveAcceso, enterpriseId);

                // Paso 11: Generar PDF del ticket para impresión
                String ticketHtml = pdfGeneratorService.generateTicketHtml(savedInvoice, facturaDto, numeroAutorizacion, fechaAutorizacion, claveAcceso, enterpriseId);
                byte[] ticketPdf = pdfGeneratorService.generatePdfFromHtml(ticketHtml);

                GessaPDFResponse responseTicket = new GessaPDFResponse<>();

                responseTicket.setMessage("Se realizó correctamente");
                responseTicket.setType(true);
                responseTicket.setInvoiceId(savedInvoice.getId());
                responseTicket.setData(ticketPdf);

                //responseTicket.success("Se realizó correctamente", savedInvoice.getId(),ticketPdf);

                return responseTicket;
                //return ticketPdf;
            }

        } else {
            System.out.println("⚠ Estado desconocido en respuesta de recepción");

            throw new ResourceNotFoundException(
                    "⚠ Estado desconocido en respuesta de recepción"
            );
        }

    }

    public byte[] processUpdateInvoiceDeluxe(FacturaSRIDTO facturaDto,
                                       Long userId,
                                       Long pointOutletId,
                                       Long enterpriseId,
                                             Long invoiceId) throws Exception {

        // Paso 1: Firmar el documento
        byte[] signedXmlBytes = xmlSignatureService.signerInvoiceDeluxe(facturaDto, String.valueOf(pointOutletId));

        // Paso 2: Extraer el valor del secuencial
        String secuencial = extractSecuencialFromXml(new String(signedXmlBytes));

        // Paso 3: Convertir a Base64
        String base64Xml = Base64.getEncoder().encodeToString(signedXmlBytes);

        // Paso 4: Enviar al SRI RecepcionComprobantesOffline
        String recepcionSoapRequest = buildRecepcionSoapRequest(base64Xml);
        String recepcionResponse = sendSoapRequest(recepcionUrl, recepcionSoapRequest);

        // Paso 5: Extraer la claveAcceso si la respuesta es RECIBIDA
        String claveAcceso = extractClaveAcceso(signedXmlBytes);

        if (claveAcceso == null) {
            // NO  fue recibida
            System.out.println("SRI Recepcion Response: " + recepcionResponse);
            throw new RuntimeException("La Factura no fue recibida por el  SRI");
        }

        // ========================================================================
        // VALIDAR RESPUESTA DE RECEPCIÓN
        // ========================================================================
        if (recepcionResponse.contains("<estado>DEVUELTA</estado>")) {
            System.out.println("⚠ Comprobante DEVUELTO - Verificando motivo...");

            // Verificar si es error 70 (comprobante en procesamiento)
            if (recepcionResponse.contains("<identificador>70</identificador>")) {
                System.out.println("✓ Error 70 detectado: Comprobante en procesamiento");
                System.out.println("✓ Continuando con el proceso de autorización...");

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

        } else {
            System.out.println("⚠ Estado desconocido en respuesta de recepción");
        }

        // Paso 6: Enviar al SRI AutorizacionComprobantes
        String autorizacionSoapRequest = buildAutorizacionSoapRequest(claveAcceso);

        String autorizacionResponse = sendSoapRequest(autorizacionUrl, autorizacionSoapRequest);
        System.out.println("AUTORIZACION? "+autorizacionResponse);

        String numeroAutorizacion = extractNumeroAutorizacion(autorizacionResponse);
        String fechaAutorizacion = extractFechaAutorizacion(autorizacionResponse);

        String establecimiento = extractEstabFromXml(new String(signedXmlBytes));
        String puntoEmision = extractPtoEmiFromXml(new String (signedXmlBytes));

        // Paso 7: Revisar si  la factura fue AUTORIZADO
        if (!isAutorizado(autorizacionResponse)) {
            throw new RuntimeException("La Factura no autorizada por el SRI");
        }else{
            // Paso 8: Guardar la factura en la base de datos
            InvoiceModel savedInvoice = updatedInvoiceToDatabaseDeluxe(facturaDto,
                    claveAcceso,
                    secuencial,
                    userId,
                    pointOutletId,
                    enterpriseId,
                    establecimiento,
                    puntoEmision,
                    invoiceId);

            // Paso 10: Generar PDF y enviar por email
            //generatePdfAndSendEmail(savedInvoice, facturaDto, numeroAutorizacion, fechaAutorizacion, claveAcceso, enterpriseId);

            // Paso 11: Generar PDF del ticket para impresión
            String ticketHtml = pdfGeneratorService.generateTicketHtml(savedInvoice, facturaDto, numeroAutorizacion, fechaAutorizacion, claveAcceso, enterpriseId);
            byte[] ticketPdf = pdfGeneratorService.generatePdfFromHtml(ticketHtml);

            return ticketPdf;
        }

    }

    public byte[] processVoucherDeluxe(FacturaSRIDTO facturaDto,
                                       Long userId,
                                       Long pointOutletId,
                                       Long enterpriseId) throws Exception {

            // Paso 1: Guardar la factura en la base de datos
            InvoiceModel savedInvoice = saveComprobanteToDatabaseDeluxe(facturaDto, userId, pointOutletId, enterpriseId);

            LocalDate hoy = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechaAutorizacion = hoy.format(formatter);
            // Paso 2: Generar PDF del ticket para impresión
            String ticketHtml = pdfGeneratorService.generateTicketComprobanteHtml(savedInvoice, facturaDto,  fechaAutorizacion);
            byte[] ticketPdf = pdfGeneratorService.generatePdfFromHtml(ticketHtml);

            return ticketPdf;


    }

    public byte[] processVoucherUpdateDeluxe(FacturaSRIDTO facturaDto,
                                       Long userId,
                                       Long pointOutletId,
                                       Long enterpriseId,
                                             Long invoiceId) throws Exception {

        // Paso 1: Guardar la factura en la base de datos
        InvoiceModel savedInvoice = updatedComprobanteToDatabaseDeluxe(facturaDto, userId, pointOutletId, enterpriseId, invoiceId);

        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaAutorizacion = hoy.format(formatter);
        // Paso 2: Generar PDF del ticket para impresión
        String ticketHtml = pdfGeneratorService.generateTicketComprobanteHtml(savedInvoice, facturaDto,  fechaAutorizacion);
        byte[] ticketPdf = pdfGeneratorService.generatePdfFromHtml(ticketHtml);

        return ticketPdf;


    }

    public InvoiceModel processSavedSaleDeluxe(FacturaSRIDTO facturaDto,
                                       Long userId,
                                       Long pointOutletId,
                                       Long enterpriseId, String nameSaved) throws Exception {

        // Paso 1: Guardar la factura en la base de datos
        InvoiceModel savedInvoice = saveTempToDatabaseDeluxe(facturaDto, userId, pointOutletId, enterpriseId, nameSaved);

        return savedInvoice;


    }

    public InvoiceModel processUpdatedSaleDeluxe(FacturaSRIDTO facturaDto,
                                               Long userId,
                                               Long pointOutletId,
                                               Long enterpriseId,
                                                 Long invoiceId) throws Exception {

        // Paso 1: Guardar la factura en la base de datos
        InvoiceModel savedInvoice = updatedComprobanteToDatabaseDeluxe(facturaDto, userId, pointOutletId, enterpriseId, invoiceId);

        return savedInvoice;


    }


    public String buildRecepcionSoapRequest(String base64Xml) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:ec=\"http://ec.gob.sri.ws.recepcion\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <ec:validarComprobante>\n" +
                "         <xml>" + base64Xml + "</xml>\n" +
                "      </ec:validarComprobante>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }

    public String buildAutorizacionSoapRequest(String claveAcceso) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:ec=\"http://ec.gob.sri.ws.autorizacion\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <ec:autorizacionComprobante>\n" +
                "         <claveAccesoComprobante>" + claveAcceso + "</claveAccesoComprobante>\n" +
                "      </ec:autorizacionComprobante>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
    }

    public String sendSoapRequest(String url, String soapRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    private String extractClaveAcceso(byte[] signedXmlBytes) {
        try {
            // Convierte los bytes del XML firmado en texto
            String xmlContent = new String(signedXmlBytes, StandardCharsets.UTF_8);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            // Busca directamente el tag claveAcceso (sin importar namespaces)
            NodeList nodes = doc.getElementsByTagName("claveAcceso");
            if (nodes.getLength() > 0) {
                String claveAcceso = nodes.item(0).getTextContent().trim();
                System.out.println("Clave de acceso extraída: " + claveAcceso);
                return claveAcceso;
            } else {
                System.err.println("No se encontró la etiqueta <claveAcceso> en el XML");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isAutorizado(String response) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(response)));

            // Try different possible tag names due to namespaces
            String[] estadoTags = {"estado", "ns2:estado"};
            for (String tag : estadoTags) {
                if (doc.getElementsByTagName(tag).getLength() > 0) {
                    String estado = doc.getElementsByTagName(tag).item(0).getTextContent();
                    return "AUTORIZADO".equals(estado);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String extractSecuencialFromXml(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            if (doc.getElementsByTagName("secuencial").getLength() > 0) {
                return doc.getElementsByTagName("secuencial").item(0).getTextContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractEstabFromXml(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            if (doc.getElementsByTagName("estab").getLength() > 0) {
                return doc.getElementsByTagName("estab").item(0).getTextContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractPtoEmiFromXml(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            if (doc.getElementsByTagName("ptoEmi").getLength() > 0) {
                return doc.getElementsByTagName("ptoEmi").item(0).getTextContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractNumeroAutorizacion(String response) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(response)));

            // Try different possible tag names due to namespaces
            String[] numeroTags = {"numeroAutorizacion", "ns2:numeroAutorizacion"};
            for (String tag : numeroTags) {
                if (doc.getElementsByTagName(tag).getLength() > 0) {
                    return doc.getElementsByTagName(tag).item(0).getTextContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractFechaAutorizacion(String response) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(response)));

            // Try different possible tag names due to namespaces
            String[] fechaTags = {"fechaAutorizacion", "ns2:fechaAutorizacion"};
            for (String tag : fechaTags) {
                if (doc.getElementsByTagName(tag).getLength() > 0) {
                    return doc.getElementsByTagName(tag).item(0).getTextContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void generatePdfAndSendEmail(InvoiceModel invoice, FacturaSRIDTO facturaDto, String numeroAutorizacion,
                                        String fechaAutorizacion, String claveAcceso, Long enterpriseId) {
        try {
            // Generate HTML
            String htmlContent = pdfGeneratorService.generateInvoiceHtml(invoice, facturaDto, numeroAutorizacion,
                    fechaAutorizacion, claveAcceso, enterpriseId);

            // Generate PDF
            byte[] pdfBytes = pdfGeneratorService.generatePdfFromHtml(htmlContent);

            // Get client email
            ClientModel client = clientService.getClientById(invoice.getClientId().getId());
            String clientEmail = client.getClientEmail();

            if (clientEmail != null && !clientEmail.isEmpty()) {
                // Send email
//                emailConfigService.sendEmailWithAttachment(enterpriseId, clientEmail,
//                        "FACTURA GILU", "Esta factura es tuya.", pdfBytes);
            }

        } catch (Exception e) {
            // Log error but don't fail the invoice process
            System.err.println("Error generating PDF or sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private InvoiceModel saveInvoiceToDatabase(FacturaSRIDTO facturaDto, String claveAcceso, String secuencial) {
        // Create InvoiceRequestDTO from FacturaSRIDTO
        InvoiceRequestDTO invoiceRequest = new InvoiceRequestDTO();
        invoiceRequest.setAccessKey(claveAcceso);
        invoiceRequest.setSequential(secuencial);
        invoiceRequest.setInvoiceDate(LocalDate.parse(facturaDto.getInfoFactura().getFechaEmision(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        invoiceRequest.setInvoiceTotal(facturaDto.getInfoFactura().getImporteTotal());
        invoiceRequest.setInvoiceSubtotal(facturaDto.getInfoFactura().getTotalSinImpuestos());
        invoiceRequest.setInvoiceDiscount(facturaDto.getInfoFactura().getTotalDescuento());
        invoiceRequest.setPaymentType("CASH"); // Assuming default, adjust as needed
        invoiceRequest.setInvoiceStatus("AUTHORIZED");
        invoiceRequest.setInvoiceType("FACTURA");
        // Note: userId, clientId, enterpriseId need to be set from context or passed as parameters

        // Convert detalles to InvoiceDetailRequestDTO list
        if (facturaDto.getDetalles() != null && facturaDto.getDetalles().getDetalle() != null) {
            invoiceRequest.setDetails(facturaDto.getDetalles().getDetalle().stream().map(detalle -> {
                InvoiceDetailRequestDTO detail = new InvoiceDetailRequestDTO();
                detail.setQuantity(detalle.getCantidad().intValue());
                detail.setDescription(detalle.getDescripcion());
                detail.setTotalValue(detalle.getPrecioTotalSinImpuesto());
                detail.setUnitValue(detalle.getPrecioUnitario());
                detail.setTotalValueWithoutTax(detalle.getPrecioTotalSinImpuesto());
                detail.setUnitValueWithoutTax(detalle.getPrecioUnitario());
                // Calculate tax if needed
                if (detalle.getImpuestos() != null && detalle.getImpuestos().getImpuesto() != null) {
                    detail.setProductTax(detalle.getImpuestos().getImpuesto().getValor());
                }
                // Set other fields as needed
                return detail;
            }).toList());
        }

        // Save invoice (details are automatically saved by the mapper)
        InvoiceModel savedInvoice = invoiceService.saveInvoiceDTO(invoiceRequest);

        return savedInvoice;
    }

    private InvoiceModel saveInvoiceToDatabaseDeluxe(FacturaSRIDTO facturaDto, String claveAcceso, String secuencial,
                                               Long userId,
                                               Long pointOutletId,
                                               Long enterpriseId,
                                                     String establecimiento,
                                                     String puntoEmision) {
        // Create InvoiceRequestDTO from FacturaSRIDTO
        InvoiceRequestDTO invoiceRequest = new InvoiceRequestDTO();
        invoiceRequest.setAccessKey(claveAcceso);
        invoiceRequest.setSequential(secuencial);
        invoiceRequest.setInvoiceDate(LocalDate.parse(facturaDto.getInfoFactura().getFechaEmision(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        invoiceRequest.setInvoiceTotal(facturaDto.getInfoFactura().getImporteTotal());
        invoiceRequest.setInvoiceSubtotal(facturaDto.getInfoFactura().getTotalSinImpuestos());
        invoiceRequest.setInvoiceDiscount(facturaDto.getInfoFactura().getTotalDescuento());
        invoiceRequest.setPaymentType("CASH");
        invoiceRequest.setInvoiceStatus("AUTHORIZED");
        invoiceRequest.setUserId(userId);
        invoiceRequest.setEnterpriseId(enterpriseId);
        invoiceRequest.setIssuePoint(String.valueOf(pointOutletId));
        invoiceRequest.setEstablishment(establecimiento);
        invoiceRequest.setRemissionGuide(puntoEmision);
        invoiceRequest.setInvoiceType("FACTURA");

        // Buscar cliente según tipo de identificación
        if (facturaDto.getInfoFactura().getIdentificacionComprador().length() == 10) {
            var cliente = clientService.getClientByIdentificacion(facturaDto.getInfoFactura().getIdentificacionComprador());
            if (cliente != null) invoiceRequest.setClientId(cliente.getId());
        } else {
            var cliente = clientService.getClientByRuc(facturaDto.getInfoFactura().getIdentificacionComprador());
            if (cliente != null) invoiceRequest.setClientId(cliente.getId());
        }

        // Guardar la factura PRIMERO sin detalles para obtener el ID
        invoiceRequest.setDetails(null); // No enviar detalles inicialmente
        InvoiceModel savedInvoice = invoiceService.saveInvoiceDTO(invoiceRequest);

        // Convertir detalles a DTOs
        if (facturaDto.getDetalles() != null && facturaDto.getDetalles().getDetalle() != null) {
            List<InvoiceDetailRequestDTO> uniqueDetails = facturaDto.getDetalles().getDetalle().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(detalle -> {

                        InvoiceDetailRequestDTO detail = new InvoiceDetailRequestDTO();
                        detail.setInvoiceId(savedInvoice.getId());

                        ProductModel productfindId = productRepository
                                .getByProductCode(detalle.getCodigoPrincipal());

                        Long nuevoProductId = 0L;
                        if(productfindId == null){

                            ProductDeluxeDTO createnewProduct = new ProductDeluxeDTO();

                            createnewProduct.setProductCode(detalle.getCodigoPrincipal());
                            createnewProduct.setProductName(detalle.getDescripcion());
                            createnewProduct.setCategoryName("SIN DEFINICION");
                            createnewProduct.setProductDesc(detalle.getDescripcion());
                            createnewProduct.setDetailName("SIN DEFINICION");

                            ProductDeluxeDTO getCreatedProduct = saveNewProduct(createnewProduct);

                            nuevoProductId = getCreatedProduct.getId();

                            StockDTO newStock = new StockDTO();
                            newStock.setProductId(getCreatedProduct.getId());
                            newStock.setOutletId(pointOutletId);
                            newStock.setStockQuantity(3);
                            newStock.setStockAvalible(true);

                            BigDecimal precio = detalle.getPrecioUnitario()
                                    .setScale(2, RoundingMode.HALF_UP);

                            newStock.setUnitPrice(precio.floatValue());
                            newStock.setPvpPrice(precio.floatValue());
                            newStock.setStockMax(100);
                            newStock.setStockMin(1);
                            newStock.setApplyTax(true);
                            newStock.setIvaId(4L);

                            stockService.createOrUpdate(newStock);

                        }else{
                            nuevoProductId = productfindId.getId();
                        }

                        detail.setStockProductId(nuevoProductId);

                        detail.setStockOutletId(pointOutletId);

                        StockModel stockModel = stockRepository.findByIdProductIdAndIdOutletId(nuevoProductId, pointOutletId)
                                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el stock"));

                        float result = stockModel.getStockQuantity() - detalle.getCantidad().floatValue();

                        float substractQuantity = result <= 0 ? 0 : result;

                        stockModel.setStockQuantity(substractQuantity);

                        stockRepository.save(stockModel);

                        detail.setQuantity(detalle.getCantidad().intValue());
                        detail.setDescription(detalle.getDescripcion());
                        detail.setTotalValue(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValue(detalle.getPrecioUnitario());
                        detail.setTotalValueWithoutTax(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValueWithoutTax(detalle.getPrecioUnitario());
                        if (detalle.getImpuestos() != null && detalle.getImpuestos().getImpuesto() != null) {
                            detail.setProductTax(detalle.getImpuestos().getImpuesto().getValor());
                        }
                        return detail;
                    })
                    .collect(Collectors.toList());

            invoiceDetailService.saveInvoiceDetails(uniqueDetails, savedInvoice);
        }


        return savedInvoice;
    }

    private InvoiceModel updatedComprobanteToDatabaseDeluxe(FacturaSRIDTO facturaDto,
                                                            Long userId,
                                                            Long pointOutletId,
                                                            Long enterpriseId,
                                                            Long invoiceId) {

        // Create InvoiceRequestDTO from FacturaSRIDTO
        InvoiceModel findInvoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay facturas con ese id"));



        InvoiceRequestDTO invoiceRequest = new InvoiceRequestDTO();
        invoiceRequest.setAccessKey("");
        invoiceRequest.setSequential("");
        invoiceRequest.setInvoiceDate(LocalDate.parse(facturaDto.getInfoFactura().getFechaEmision(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        invoiceRequest.setInvoiceTotal(facturaDto.getInfoFactura().getImporteTotal());
        invoiceRequest.setInvoiceSubtotal(facturaDto.getInfoFactura().getTotalSinImpuestos());
        invoiceRequest.setInvoiceDiscount(facturaDto.getInfoFactura().getTotalDescuento());
        invoiceRequest.setPaymentType("CASH");
        invoiceRequest.setInvoiceStatus("VOUCHER");
        invoiceRequest.setUserId(userId);
        invoiceRequest.setEnterpriseId(enterpriseId);
        invoiceRequest.setIssuePoint(String.valueOf(pointOutletId));
        invoiceRequest.setEstablishment("");
        invoiceRequest.setRemissionGuide("");
        invoiceRequest.setInvoiceType("VOUCHER");
        invoiceRequest.setClientId(null);

        // Guardar la factura PRIMERO sin detalles para obtener el ID
        invoiceRequest.setDetails(null); // No enviar detalles inicialmente

        InvoiceModel savedInvoice = invoiceService.updateInvoiceDTO(findInvoice.getId(), invoiceRequest);

        // Convertir detalles a DTOs
        if (facturaDto.getDetalles() != null && facturaDto.getDetalles().getDetalle() != null) {
            List<InvoiceDetailRequestDTO> uniqueDetails = facturaDto.getDetalles().getDetalle().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(detalle -> {

                        InvoiceDetailRequestDTO detail = new InvoiceDetailRequestDTO();
                        detail.setInvoiceId(savedInvoice.getId());

                        ProductModel productfindId = productRepository
                                .getByProductCode(detalle.getCodigoPrincipal());

                        Long nuevoProductId = 0L;
                        if(productfindId == null){

                            ProductDeluxeDTO createnewProduct = new ProductDeluxeDTO();

                            createnewProduct.setProductCode(detalle.getCodigoPrincipal());
                            createnewProduct.setProductName(detalle.getDescripcion());
                            createnewProduct.setCategoryName("SIN DEFINICION");
                            createnewProduct.setProductDesc(detalle.getDescripcion());
                            createnewProduct.setDetailName("SIN DEFINICION");

                            ProductDeluxeDTO getCreatedProduct = saveNewProduct(createnewProduct);

                            nuevoProductId = getCreatedProduct.getId();

                            StockDTO newStock = new StockDTO();
                            newStock.setProductId(getCreatedProduct.getId());
                            newStock.setOutletId(pointOutletId);
                            newStock.setStockQuantity(3);
                            newStock.setStockAvalible(true);
                            newStock.setUnitPrice(detalle.getPrecioUnitario().floatValue());
                            newStock.setPvpPrice(detalle.getPrecioUnitario().floatValue());
                            newStock.setStockMax(100);
                            newStock.setStockMin(1);
                            newStock.setApplyTax(true);
                            newStock.setIvaId(4L);

                            stockService.createOrUpdate(newStock);

                        }else{
                            nuevoProductId = productfindId.getId();
                        }

                        detail.setStockProductId(nuevoProductId);

                        detail.setStockOutletId(pointOutletId);

                        StockModel stockModel = stockRepository.findByIdProductIdAndIdOutletId(nuevoProductId, pointOutletId)
                                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el stock"));

                        float result = stockModel.getStockQuantity() - detalle.getCantidad().floatValue();

                        float substractQuantity = result <= 0 ? 0 : result;

                        stockModel.setStockQuantity(substractQuantity);

                        stockRepository.save(stockModel);

                        detail.setQuantity(detalle.getCantidad().intValue());
                        detail.setDescription(detalle.getDescripcion());
                        detail.setTotalValue(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValue(detalle.getPrecioUnitario());
                        detail.setTotalValueWithoutTax(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValueWithoutTax(detalle.getPrecioUnitario());
                        if (detalle.getImpuestos() != null && detalle.getImpuestos().getImpuesto() != null) {
                            detail.setProductTax(detalle.getImpuestos().getImpuesto().getValor());
                        }
                        return detail;
                    })
                    .collect(Collectors.toList());

            //invoiceDetailService.saveInvoiceDetails(uniqueDetails, savedInvoice);
            invoiceDetailService.updatedInvoiceDetailDTO(uniqueDetails, savedInvoice.getId());
        }


        return savedInvoice;
    }

    private ProductDeluxeDTO saveNewProduct(ProductDeluxeDTO productDeluxeDTO){

        return productService.createProductDeluxe(productDeluxeDTO);

    }

    private InvoiceModel saveComprobanteToDatabaseDeluxe(FacturaSRIDTO facturaDto,
                                                     Long userId,
                                                     Long pointOutletId,
                                                     Long enterpriseId) {
        // Create InvoiceRequestDTO from FacturaSRIDTO
        InvoiceRequestDTO invoiceRequest = new InvoiceRequestDTO();
        invoiceRequest.setAccessKey("");
        invoiceRequest.setSequential("");
        invoiceRequest.setInvoiceDate(LocalDate.parse(facturaDto.getInfoFactura().getFechaEmision(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        invoiceRequest.setInvoiceTotal(facturaDto.getInfoFactura().getImporteTotal());
        invoiceRequest.setInvoiceSubtotal(facturaDto.getInfoFactura().getTotalSinImpuestos());
        invoiceRequest.setInvoiceDiscount(facturaDto.getInfoFactura().getTotalDescuento());
        invoiceRequest.setPaymentType("CASH");
        invoiceRequest.setInvoiceStatus("VOUCHER");
        invoiceRequest.setUserId(userId);
        invoiceRequest.setEnterpriseId(enterpriseId);
        invoiceRequest.setIssuePoint(String.valueOf(pointOutletId));
        invoiceRequest.setEstablishment("");
        invoiceRequest.setRemissionGuide("");
        invoiceRequest.setInvoiceType("VOUCHER");
        invoiceRequest.setClientId(null);

        // Guardar la factura PRIMERO sin detalles para obtener el ID
        invoiceRequest.setDetails(null); // No enviar detalles inicialmente
        InvoiceModel savedInvoice = invoiceService.saveInvoiceDTO(invoiceRequest);

        // Convertir detalles a DTOs
        if (facturaDto.getDetalles() != null && facturaDto.getDetalles().getDetalle() != null) {
            List<InvoiceDetailRequestDTO> uniqueDetails = facturaDto.getDetalles().getDetalle().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(detalle -> {

                        InvoiceDetailRequestDTO detail = new InvoiceDetailRequestDTO();
                        detail.setInvoiceId(savedInvoice.getId());

                        ProductModel productfindId = productRepository
                                .getByProductCode(detalle.getCodigoPrincipal());

                        Long nuevoProductId = 0L;
                        if(productfindId == null){

                            ProductDeluxeDTO createnewProduct = new ProductDeluxeDTO();

                            createnewProduct.setProductCode(detalle.getCodigoPrincipal());
                            createnewProduct.setProductName(detalle.getDescripcion());
                            createnewProduct.setCategoryName("SIN DEFINICION");
                            createnewProduct.setProductDesc(detalle.getDescripcion());
                            createnewProduct.setDetailName("SIN DEFINICION");

                            ProductDeluxeDTO getCreatedProduct = saveNewProduct(createnewProduct);

                            nuevoProductId = getCreatedProduct.getId();

                            StockDTO newStock = new StockDTO();
                            newStock.setProductId(getCreatedProduct.getId());
                            newStock.setOutletId(pointOutletId);
                            newStock.setStockQuantity(1);
                            newStock.setStockAvalible(true);
                            newStock.setUnitPrice(detalle.getPrecioUnitario().floatValue());
                            newStock.setPvpPrice(detalle.getPrecioUnitario().floatValue());
                            newStock.setStockMax(100);
                            newStock.setStockMin(1);
                            newStock.setApplyTax(true);
                            newStock.setIvaId(4L);

                            stockService.createOrUpdate(newStock);

                        }else{
                            nuevoProductId = productfindId.getId();
                        }

                        detail.setStockProductId(nuevoProductId);

                        detail.setStockOutletId(pointOutletId);

                        StockModel stockModel = stockRepository.findByIdProductIdAndIdOutletId(nuevoProductId, pointOutletId)
                                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el stock"));

                        float result = stockModel.getStockQuantity() - detalle.getCantidad().floatValue();

                        float substractQuantity = result <= 0 ? 0 : result;

                        stockModel.setStockQuantity(substractQuantity);

                        stockRepository.save(stockModel);

                        detail.setQuantity(detalle.getCantidad().intValue());
                        detail.setDescription(detalle.getDescripcion());
                        detail.setTotalValue(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValue(detalle.getPrecioUnitario());
                        detail.setTotalValueWithoutTax(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValueWithoutTax(detalle.getPrecioUnitario());
                        if (detalle.getImpuestos() != null && detalle.getImpuestos().getImpuesto() != null) {
                            detail.setProductTax(detalle.getImpuestos().getImpuesto().getValor());
                        }
                        return detail;
                    })
                    .collect(Collectors.toList());

            invoiceDetailService.saveInvoiceDetails(uniqueDetails, savedInvoice);
        }


        return savedInvoice;
    }

    private InvoiceModel updatedInvoiceToDatabaseDeluxe(FacturaSRIDTO facturaDto, String claveAcceso, String secuencial,
                                                        Long userId,
                                                        Long pointOutletId,
                                                        Long enterpriseId,
                                                        String establecimiento,
                                                        String puntoEmision, Long invoiceId) {

        // Create InvoiceRequestDTO from FacturaSRIDTO
        InvoiceModel findInvoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay facturas con ese id"));



        InvoiceRequestDTO invoiceRequest = new InvoiceRequestDTO();
        invoiceRequest.setAccessKey(claveAcceso);
        invoiceRequest.setSequential(secuencial);
        invoiceRequest.setInvoiceDate(LocalDate.parse(facturaDto.getInfoFactura().getFechaEmision(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        invoiceRequest.setInvoiceTotal(facturaDto.getInfoFactura().getImporteTotal());
        invoiceRequest.setInvoiceSubtotal(facturaDto.getInfoFactura().getTotalSinImpuestos());
        invoiceRequest.setInvoiceDiscount(facturaDto.getInfoFactura().getTotalDescuento());
        invoiceRequest.setPaymentType("CASH");
        invoiceRequest.setInvoiceStatus("AUTHORIZED");
        invoiceRequest.setUserId(userId);
        invoiceRequest.setEnterpriseId(enterpriseId);
        invoiceRequest.setIssuePoint(String.valueOf(pointOutletId));
        invoiceRequest.setEstablishment(establecimiento);
        invoiceRequest.setRemissionGuide(puntoEmision);
        invoiceRequest.setInvoiceType("FACTURA");

        // Buscar cliente según tipo de identificación
        if (facturaDto.getInfoFactura().getIdentificacionComprador().length() == 10) {

            ClientResponseDTO cliente = clientService.getClientByIdentificacion(facturaDto.getInfoFactura().getIdentificacionComprador());


            if (cliente != null) invoiceRequest.setClientId(cliente.getId());
        } else {
            ClientResponseDTO cliente = clientService.getClientByRuc(facturaDto.getInfoFactura().getIdentificacionComprador());

            if (cliente != null) invoiceRequest.setClientId(cliente.getId());
        }

        // Guardar la factura PRIMERO sin detalles para obtener el ID
        invoiceRequest.setDetails(null); // No enviar detalles inicialmente

        InvoiceModel savedInvoice = invoiceService.updateInvoiceDTO(findInvoice.getId(), invoiceRequest);

        // Convertir detalles a DTOs
        if (facturaDto.getDetalles() != null && facturaDto.getDetalles().getDetalle() != null) {
            List<InvoiceDetailRequestDTO> uniqueDetails = facturaDto.getDetalles().getDetalle().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(detalle -> {

                        InvoiceDetailRequestDTO detail = new InvoiceDetailRequestDTO();
                        detail.setInvoiceId(savedInvoice.getId());

                        ProductModel productfindId = productRepository
                                .getByProductCode(detalle.getCodigoPrincipal());

                        Long nuevoProductId = 0L;
                        if(productfindId == null){

                            ProductDeluxeDTO createnewProduct = new ProductDeluxeDTO();

                            createnewProduct.setProductCode(detalle.getCodigoPrincipal());
                            createnewProduct.setProductName(detalle.getDescripcion());
                            createnewProduct.setCategoryName("SIN DEFINICION");
                            createnewProduct.setProductDesc(detalle.getDescripcion());
                            createnewProduct.setDetailName("SIN DEFINICION");

                            ProductDeluxeDTO getCreatedProduct = saveNewProduct(createnewProduct);

                            nuevoProductId = getCreatedProduct.getId();

                            StockDTO newStock = new StockDTO();
                            newStock.setProductId(getCreatedProduct.getId());
                            newStock.setOutletId(pointOutletId);
                            newStock.setStockQuantity(3);
                            newStock.setStockAvalible(true);
                            newStock.setUnitPrice(detalle.getPrecioUnitario().floatValue());
                            newStock.setPvpPrice(detalle.getPrecioUnitario().floatValue());
                            newStock.setStockMax(100);
                            newStock.setStockMin(1);
                            newStock.setApplyTax(true);
                            newStock.setIvaId(4L);

                            stockService.createOrUpdate(newStock);

                        }else{
                            nuevoProductId = productfindId.getId();
                        }

                        detail.setStockProductId(nuevoProductId);

                        detail.setStockOutletId(pointOutletId);

                        StockModel stockModel = stockRepository.findByIdProductIdAndIdOutletId(nuevoProductId, pointOutletId)
                                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el stock"));

                        float result = stockModel.getStockQuantity() - detalle.getCantidad().floatValue();

                        float substractQuantity = result <= 0 ? 0 : result;

                        stockModel.setStockQuantity(substractQuantity);

                        stockRepository.save(stockModel);

                        detail.setQuantity(detalle.getCantidad().intValue());
                        detail.setDescription(detalle.getDescripcion());
                        detail.setTotalValue(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValue(detalle.getPrecioUnitario());
                        detail.setTotalValueWithoutTax(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValueWithoutTax(detalle.getPrecioUnitario());
                        if (detalle.getImpuestos() != null && detalle.getImpuestos().getImpuesto() != null) {
                            detail.setProductTax(detalle.getImpuestos().getImpuesto().getValor());
                        }
                        return detail;
                    })
                    .collect(Collectors.toList());

            //invoiceDetailService.saveInvoiceDetails(uniqueDetails, savedInvoice);
            invoiceDetailService.updatedInvoiceDetailDTO(uniqueDetails, savedInvoice.getId());
        }


        return savedInvoice;
    }

    private InvoiceModel updatedTempToDatabaseDeluxe(FacturaSRIDTO facturaDto, String claveAcceso, String secuencial,
                                                        Long userId,
                                                        Long pointOutletId,
                                                        Long enterpriseId,
                                                        String establecimiento,
                                                        String puntoEmision, Long invoiceId) {

        // Create InvoiceRequestDTO from FacturaSRIDTO
        InvoiceModel findInvoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay facturas con ese id"));



        InvoiceRequestDTO invoiceRequest = new InvoiceRequestDTO();
        invoiceRequest.setAccessKey(claveAcceso);
        invoiceRequest.setSequential(secuencial);
        invoiceRequest.setInvoiceDate(LocalDate.parse(facturaDto.getInfoFactura().getFechaEmision(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        invoiceRequest.setInvoiceTotal(facturaDto.getInfoFactura().getImporteTotal());
        invoiceRequest.setInvoiceSubtotal(facturaDto.getInfoFactura().getTotalSinImpuestos());
        invoiceRequest.setInvoiceDiscount(facturaDto.getInfoFactura().getTotalDescuento());
        invoiceRequest.setPaymentType("CASH");
        invoiceRequest.setInvoiceStatus("SAVED");
        invoiceRequest.setUserId(userId);
        invoiceRequest.setEnterpriseId(enterpriseId);
        invoiceRequest.setIssuePoint(String.valueOf(pointOutletId));
        invoiceRequest.setEstablishment("");
        invoiceRequest.setRemissionGuide("");
        invoiceRequest.setInvoiceType("SAVED");
        invoiceRequest.setClientId(null);

        // Guardar la factura PRIMERO sin detalles para obtener el ID
        invoiceRequest.setDetails(null); // No enviar detalles inicialmente

        InvoiceModel savedInvoice = invoiceService.updateInvoiceDTO(findInvoice.getId(), invoiceRequest);

        // Convertir detalles a DTOs
        if (facturaDto.getDetalles() != null && facturaDto.getDetalles().getDetalle() != null) {
            List<InvoiceDetailRequestDTO> uniqueDetails = facturaDto.getDetalles().getDetalle().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(detalle -> {

                        InvoiceDetailRequestDTO detail = new InvoiceDetailRequestDTO();
                        detail.setInvoiceId(savedInvoice.getId());

                        ProductModel productfindId = productRepository
                                .getByProductCode(detalle.getCodigoPrincipal());

                        Long nuevoProductId = 0L;
                        if(productfindId == null){

                            ProductDeluxeDTO createnewProduct = new ProductDeluxeDTO();

                            createnewProduct.setProductCode(detalle.getCodigoPrincipal());
                            createnewProduct.setProductName(detalle.getDescripcion());
                            createnewProduct.setCategoryName("SIN DEFINICION");
                            createnewProduct.setProductDesc(detalle.getDescripcion());
                            createnewProduct.setDetailName("SIN DEFINICION");

                            ProductDeluxeDTO getCreatedProduct = saveNewProduct(createnewProduct);

                            nuevoProductId = getCreatedProduct.getId();

                            StockDTO newStock = new StockDTO();
                            newStock.setProductId(getCreatedProduct.getId());
                            newStock.setOutletId(pointOutletId);
                            newStock.setStockQuantity(3);
                            newStock.setStockAvalible(true);
                            newStock.setUnitPrice(detalle.getPrecioUnitario().floatValue());
                            newStock.setPvpPrice(detalle.getPrecioUnitario().floatValue());
                            newStock.setStockMax(100);
                            newStock.setStockMin(1);
                            newStock.setApplyTax(true);
                            newStock.setIvaId(4L);

                            stockService.createOrUpdate(newStock);

                        }else{
                            nuevoProductId = productfindId.getId();
                        }

                        detail.setStockProductId(nuevoProductId);

                        detail.setStockOutletId(pointOutletId);

                        StockModel stockModel = stockRepository.findByIdProductIdAndIdOutletId(nuevoProductId, pointOutletId)
                                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el stock"));

                        float result = stockModel.getStockQuantity() - detalle.getCantidad().floatValue();

                        float substractQuantity = result <= 0 ? 0 : result;

                        stockModel.setStockQuantity(substractQuantity);

                        stockRepository.save(stockModel);

                        detail.setQuantity(detalle.getCantidad().intValue());
                        detail.setDescription(detalle.getDescripcion());
                        detail.setTotalValue(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValue(detalle.getPrecioUnitario());
                        detail.setTotalValueWithoutTax(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValueWithoutTax(detalle.getPrecioUnitario());
                        if (detalle.getImpuestos() != null && detalle.getImpuestos().getImpuesto() != null) {
                            detail.setProductTax(detalle.getImpuestos().getImpuesto().getValor());
                        }
                        return detail;
                    })
                    .collect(Collectors.toList());

            //invoiceDetailService.saveInvoiceDetails(uniqueDetails, savedInvoice);
            invoiceDetailService.updatedInvoiceDetailDTO(uniqueDetails, savedInvoice.getId());
        }


        return savedInvoice;
    }



    private InvoiceModel saveTempToDatabaseDeluxe(FacturaSRIDTO facturaDto,
                                                         Long userId,
                                                         Long pointOutletId,
                                                         Long enterpriseId,
                                                  String nameSaved) {

        // Create InvoiceRequestDTO from FacturaSRIDTO
        InvoiceRequestDTO invoiceRequest = new InvoiceRequestDTO();
        invoiceRequest.setAccessKey("");
        invoiceRequest.setSequential("");
        invoiceRequest.setInvoiceDate(LocalDate.parse(facturaDto.getInfoFactura().getFechaEmision(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        invoiceRequest.setInvoiceTotal(facturaDto.getInfoFactura().getImporteTotal());
        invoiceRequest.setInvoiceSubtotal(facturaDto.getInfoFactura().getTotalSinImpuestos());
        invoiceRequest.setInvoiceDiscount(facturaDto.getInfoFactura().getTotalDescuento());
        invoiceRequest.setPaymentType("CASH");
        invoiceRequest.setInvoiceStatus("SAVED");
        invoiceRequest.setUserId(userId);
        invoiceRequest.setEnterpriseId(enterpriseId);
        invoiceRequest.setIssuePoint(String.valueOf(pointOutletId));
        invoiceRequest.setEstablishment(nameSaved);
        invoiceRequest.setRemissionGuide("");
        invoiceRequest.setInvoiceType("SAVED");
        invoiceRequest.setClientId(null);

        // Guardar la factura PRIMERO sin detalles para obtener el ID
        invoiceRequest.setDetails(null); // No enviar detalles inicialmente
        InvoiceModel savedInvoice = invoiceService.saveInvoiceDTO(invoiceRequest);

        // Convertir detalles a DTOs
        if (facturaDto.getDetalles() != null && facturaDto.getDetalles().getDetalle() != null) {
            List<InvoiceDetailRequestDTO> uniqueDetails = facturaDto.getDetalles().getDetalle().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(detalle -> {

                        InvoiceDetailRequestDTO detail = new InvoiceDetailRequestDTO();
                        detail.setInvoiceId(savedInvoice.getId());
                        ProductModel productfindId = productRepository
                                .getByProductCode(detalle.getCodigoPrincipal());

                        Long nuevoProductId = 0L;
                        if(productfindId == null){

                            ProductDeluxeDTO createnewProduct = new ProductDeluxeDTO();

                            createnewProduct.setProductCode(detalle.getCodigoPrincipal());
                            createnewProduct.setProductName(detalle.getDescripcion());
                            createnewProduct.setCategoryName("SIN DEFINICION");
                            createnewProduct.setProductDesc(detalle.getDescripcion());
                            createnewProduct.setDetailName("SIN DEFINICION");

                            ProductDeluxeDTO getCreatedProduct = saveNewProduct(createnewProduct);

                            nuevoProductId = getCreatedProduct.getId();

                            StockDTO newStock = new StockDTO();
                            newStock.setProductId(getCreatedProduct.getId());
                            newStock.setOutletId(pointOutletId);
                            newStock.setStockQuantity(3);
                            newStock.setStockAvalible(true);
                            newStock.setUnitPrice(detalle.getPrecioUnitario().floatValue());
                            newStock.setPvpPrice(detalle.getPrecioUnitario().floatValue());
                            newStock.setStockMax(100);
                            newStock.setStockMin(1);
                            newStock.setApplyTax(true);
                            newStock.setIvaId(4L);

                            stockService.createOrUpdate(newStock);

                        }else{
                            nuevoProductId = productfindId.getId();
                        }

                        detail.setStockProductId(nuevoProductId);
                        detail.setStockOutletId(pointOutletId);

                        StockModel stockModel = stockRepository.findByIdProductIdAndIdOutletId(nuevoProductId, pointOutletId)
                                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el stock"));

                        float result = stockModel.getStockQuantity() - detalle.getCantidad().floatValue();

                        float substractQuantity = result <= 0 ? 0 : result;

                        stockModel.setStockQuantity(substractQuantity);

                        stockRepository.save(stockModel);

                        detail.setQuantity(detalle.getCantidad().intValue());
                        detail.setDescription(detalle.getDescripcion());
                        detail.setTotalValue(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValue(detalle.getPrecioUnitario());
                        detail.setTotalValueWithoutTax(detalle.getPrecioTotalSinImpuesto());
                        detail.setUnitValueWithoutTax(detalle.getPrecioUnitario());
                        if (detalle.getImpuestos() != null && detalle.getImpuestos().getImpuesto() != null) {
                            detail.setProductTax(detalle.getImpuestos().getImpuesto().getValor());
                        }
                        return detail;
                    })
                    .collect(Collectors.toList());

            invoiceDetailService.saveInvoiceDetails(uniqueDetails, savedInvoice);
        }


        return savedInvoice;
    }
}