package com.izenshy.gessainvoice.modules.invoice.service.pdf;

import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.service.EnterpriseService;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceHeaderDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.PdfDocumentDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.PdfSubDetallesDTO;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.invoice.repository.InvoiceRepository;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceService;
import com.izenshy.gessainvoice.modules.person.client.model.ClientModel;
import com.izenshy.gessainvoice.modules.person.client.service.ClientService;
import com.izenshy.gessainvoice.sri.invoice.FacturaSRIDTO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PdfGeneratorService {

  @Autowired
  private EnterpriseService enterpriseService;

  @Autowired
  private ClientService clientService;

  @Autowired
  private InvoiceRepository invoiceRepository;

  public byte[] generatePdfFromHtml(String htmlContent) throws Exception {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      PdfRendererBuilder builder = new PdfRendererBuilder();

      builder.useFastMode();
      builder.withHtmlContent(htmlContent, null);
      builder.toStream(outputStream);
      builder.run();

      return outputStream.toByteArray();
    }
  }

  public String generarBarcodeBase64(String texto) throws Exception {
    BitMatrix matrix = new MultiFormatWriter().encode(texto, BarcodeFormat.CODE_128, 400, 100);
    BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    return Base64.getEncoder().encodeToString(baos.toByteArray());
  }

  public String generateInvoiceHtml(InvoiceModel invoice, FacturaSRIDTO facturaDto, String numeroAutorizacion,
      String fechaAutorizacion, String claveAcceso, Long enterpriseId) throws Exception {

    // Get enterprise info
    Optional<EnterpriseModel> enterpriseOpt = enterpriseService.getEnterpriseByRuc(facturaDto.rucEmpresa);
    EnterpriseModel enterprise = enterpriseOpt.orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

    // Get client info
    ClientModel client = clientService.getClientById(invoice.getClientId().getId());

    // Generate barcode SVG
    String barcodeSvg = generarBarcodeBase64(claveAcceso);
    String styles = """
             @page {
               size: A4;
               margin: 10mm;
             }

             body {
               font-family: Arial, sans-serif;
               font-size: 18px;
               color: #111;
               margin: 0;
               padding: 0;
               height: 100vh;

             }

             .header {
               width: 100%;
               display: block;
             }

             .izquierda {
               width: 40%;
               float: left;
               height: 20%;
             }

             .derecha {
               width: 50%;
              float: right;
              margin-right: 18px;
             }

             .right {
               width: 38%;
               overflow: hidden;
               text-align: right;
             }

             h1 {
               margin: 0;
               font-size: 20px;
               letter-spacing: 2px;
             }

             .small {
               font-size: 10px;
               color: #333;
             }

             .company {
               font-weight: 700;
               font-size: 14px;
             }

             .info-box {
               border: 1px solid #222;
               padding: 8px;
               margin-bottom: 8px;
               border-radius: 10px;
               width: 100%;
             }
             .info-box-social {
               margin-left: 0px ;
               margin-right: 0px;
               border-radius: 10px;
               border: 1px solid #222;
               padding: 8px;

             }


             .content-info-box-square {
               text-align: right;
             }

             .info-box-square {
               border: 1px solid #222;
               padding: 8px;
               width: 38.5%;
               margin-bottom: 8px;
               font-size: 10px;
               display: inline-block;
             }

             .valor-square-info {
               overflow: hidden;
             }

             .valor-square-info div:first-child {
               float: left;
             }

             .valor-square-info div:last-child {
               float: right;
             }

             .ahorro-square-info {
               overflow: hidden;
               margin-top: 5px;
             }

             .ahorro-square-info div:first-child {
               float: left;
               width: 70%;
             }

             .ahorro-square-info div:last-child {
               float: right;
               width: 30%;
               text-align: right;
             }

             .two-cols {
               width: 100%;
               overflow: hidden;
             }

             .col {
               float: left;
               width: 48%;
             }

             .col + .col {
               margin-left: 4%;
             }

             table {
               width: 100%;
               border-collapse: collapse;
               margin-top: 6px;
             }

             th,
             td {
               border: 1px solid #000000;
               padding: 6px;
               text-align: left;
               font-size: 13px;
             }

             th {
               font-weight: 700;
               font-size: 13px;
             }

             .right-align {
               text-align: right;
             }

             .left-align {
               text-align: left;
             }

             .totals {
               width: 40%;
               float: right;
             }

             .totals table td {
               border-collapse: collapse;
             }

             .bold {
               font-weight: 700;
             }

             .center {
               text-align: center;
             }

             .small-note {
               font-size: 10px;
               color: #555;
               margin-top: 16px;
             }

             .access-key-info {
               text-align: center;
             }

             .access-key {
               width: 30%;
               display: inline-block;
             }

             .info-factura {
               overflow: hidden;
             }

             .info-factura-no {
               float: left;
               margin-right: 2rem;
             }

             .info-factura-numero {
               float: left;
             }

             .ruc-info {
               overflow: hidden;
               font-weight: 700;
               font-size: 23px;
             }

             .ruc {
               float: left;
               margin-right: 4rem;
             }

             .factura-info {
               font-weight: 700;
               font-size: 23px;
             }

             .fecha-info {
               overflow: hidden;
             }

             .fecha {
               float: left;
               margin-right: 2rem;
             }

             .ambiente-info {
               overflow: hidden;
             }

             .ambiente {
               float: left;
               margin-right: 2rem;
             }

             .emision-info {
               overflow: hidden;
             }

             .emision {
               float: left;
               margin-right: 2rem;
             }

             .direccion-matriz-info {
               overflow: hidden;
               font-size: 16px;
             }

             .direccion-matriz {
               float: left;
               margin-right: 2rem;
             }

             .direccion-sucursal-info {
               overflow: hidden;
               font-size: 16px;
             }

             .direccion-sucursal {
               float: left;
               margin-right: 2rem;
             }

             .fin-info {
               font-size: 16px;
               margin-top: 3%;
             }

             .nombres-info {
               overflow: hidden;
               font-size: 16px;
             }

             .nombres {
               float: left;
               margin-right: 5rem;
               font-weight: bold;
             }

             .identificacion-info {
               overflow: hidden;
               font-size: 16px;
             }

             .identificacion {
               float: left;
               margin-right: 3rem;
             }

             .fecha-nombres-info {
               overflow: hidden;
               font-size: 16px;
             }

             .fecha-nombres {
               float: left;
               margin-right: 3rem;
             }

             .direccion-nombres-info {
               overflow: hidden;
               font-size: 16px;
             }

             .direccion-nombres {
               float: left;
               margin-right: 3rem;
             }

             .resumen-factura {
               width: 100%;
               overflow: hidden;
             }

             .forma-pago {
               margin-top: 5%;
               width: 58%;
               float: left;
             }

             .clear {
               clear: both;
             }

             .numero-autorizacion{
               font-size: 12px;
               text-align: center;
             }

        """;
    StringBuilder html = new StringBuilder();
    html.append(
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
    html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"es\">\n");
    html.append("<head>\n");
    html.append("  <meta charset=\"utf-8\" />\n");
    html.append("  <title>Factura</title>\n");
    html.append("  <style>\n").append(styles).append("  </style>\n");
    html.append("</head>\n");
    html.append("<body>\n");
    html.append("  <!-- HEADER -->\n");
    html.append("  <div class=\"header\" style=\"height: 100%\">\n");
    html.append("    <div class=\"izquierda\" style=\"margin-top:23%;\">\n");
    html.append("      <div class=\"info-box\">\n");
    html.append("        <div>").append(HtmlUtils.htmlEscape(enterprise.getEnterpriseOwnerName())).append("</div>\n");
    html.append("        <div style=\"margin-top: 3%;\">").append(HtmlUtils.htmlEscape(enterprise.getEnterpriseName()))
        .append("</div>\n");
    html.append("        <div class=\"direccion-matriz-info\" style=\"margin-top: 3%;\">\n");
    html.append("          <div class=\"direccion-matriz\"><strong>Dirección <br /> Matriz:</strong></div>\n");
    html.append("          <div style=\"margin-top: 3%;\">N/A</div>\n");
    html.append("        </div>\n");
    html.append("        <div class=\"direccion-sucursal-info\" style=\"margin-top: 3%;\">\n");
    html.append("          <div class=\"direccion-sucursal\"><strong>Dirección <br />Sucursal:</strong></div>\n");
    html.append("          <div style=\"margin-top:3%;\">N/A</div>\n");
    html.append("        </div>\n");
    html.append(
        "        <div class=\"fin-info\" style=\"margin-bottom:8%;\"> <strong>OBLIGADO A LLEVAR CONTABILIDAD:</strong> NO</div>\n");
    html.append("      </div>\n");
    html.append("    </div>\n");
    html.append("    <div class=\"derecha\">\n");
    html.append("      <div class=\" info-box\">\n");
    html.append("        <div class=\"ruc-info\">\n");
    html.append("          <div class=\"ruc\"><strong>R.U.C.:</strong></div>\n");
    html.append("          <div><span class=\"bold\">").append(facturaDto.rucEmpresa).append("</span></div>\n");
    html.append("        </div>\n");
    html.append("        <div class=\"factura-info\"><strong>FACTURA</strong></div>\n");
    html.append("        <div class=\"info-factura\" style=\"margin-top:3%;\">\n");
    html.append("          <div class=\"info-factura-no\">No.</div>\n");
    html.append("          <div class=\"info-factura-numero\">").append(invoice.getEstablishment()).append("-")
        .append(invoice.getIssuePoint()).append("-")
        .append(String.format("%09d", Integer.parseInt(invoice.getSequential()))).append("</div>\n");
    html.append("        </div>\n");
    html.append("        <div style=\"margin-top:3%;\">NÚMERO DE AUTORIZACIÓN</div>\n");
    html.append("        <div class=\"numero-autorizacion\" style=\"margin-top:3%;\">").append(numeroAutorizacion)
        .append("</div>\n");
    html.append("        <div class=\"fecha-info\">\n");
    html.append(
        "          <div class=\"fecha\" style=\"margin-top:3%;\"><strong>FECHA Y HORA DE <br />AUTORIZACIÓN:</strong></div>\n");
    html.append("          <div style=\"margin-top:7%;\">").append(fechaAutorizacion).append("</div>\n");
    html.append("        </div>\n");
    html.append("        <div class=\"ambiente-info\">\n");
    html.append("          <div class=\"ambiente\" style=\"margin-top:3%;\"><strong>AMBIENTE:</strong></div>\n");
    html.append("          <div style=\"margin-top:3%;\">PRUEBAS</div>\n");
    html.append("        </div>\n");
    html.append("        <div class=\"emision-info\">\n");
    html.append("          <div class=\"emision\" style=\"margin-top: 3%;\"><strong>EMISIÓN:</strong></div>\n");
    html.append("          <div style=\"margin-top: 3%;\">NORMAL</div>\n");
    html.append("        </div>\n");
    html.append("        <div style=\"margin-top:3%;\"><strong>CLAVE DE ACCESO</strong></div>\n");
    html.append("        <div class=\"access-key-info\">\n");
    html.append("          <div class=\"access-key\"><img src='data:image/png;base64,")
        .append(barcodeSvg)
        .append("' alt='barcode' style='width: 300px; height: auto;' /></div>\n");
    html.append("        <div class=\"numero-autorizacion\" style=\"margin-top:3%;\">").append(numeroAutorizacion)
        .append("</div>\n");
    html.append("        </div>\n");
    html.append("      </div>\n");
    html.append("    </div>\n");
    html.append("  </div>\n");
    // abierto
    html.append("  <div  style=\"display: block; margin-top: 65% \">\n");
    html.append("  <div class=\"info-box-social\" style=\"margin-top:1%;  \">\n");
    html.append("    <div class=\"nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("      <div class=\"nombres\" style=\"margin-top:1%;\">Razón Social / Nombres y Apellidos:</div>\n");
    html.append("      <div style=\"margin-top:1%;\">").append(HtmlUtils.htmlEscape(client.getClientFullName()))
        .append("</div>\n");
    html.append("    </div>\n");
    html.append("    <div class=\"identificacion-info\" style=\"margin-top:1%;\">\n");
    html.append(
        "      <div class=\"identificacion\" style=\"margin-top:3%;\"><strong>Identificación:</strong></div>\n");
    html.append("      <div style=\"margin-top:3%;\">").append(HtmlUtils.htmlEscape(client.getClientRuc()))
        .append("</div>\n");
    html.append("    </div>\n");
    html.append("    <div class=\"fecha-nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("      <div class=\"fecha-nombres\" style=\"margin-top:3%;\"><strong>Fecha:</strong> </div>\n");
    html.append("      <div style=\"margin-top:3%;\">\n");
    html.append("        ").append(invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        .append("\n");
    html.append("      </div>\n");
    html.append("    </div>\n");
    html.append("    <div class=\"direccion-nombres-info\" style=\"margin-top:1%;\">\n");
    html.append(
        "      <div class=\"direccion-nombres\" style=\"margin-top: 0%;\"><strong>Dirección:</strong> </div>\n");
    html.append("      <div style=\"margin-top: 0%;\">")
        .append(HtmlUtils.htmlEscape(client.getClientAddress() != null ? client.getClientAddress() : "N/A"))
        .append("</div>\n");
    html.append("    </div>\n");
    html.append("  </div>\n");

    html.append("  <!-- DETALLE DE ITEMS -->\n");
    html.append("  <div style=\"margin-top:10px;\">\n");
    html.append("    <table>\n");
    html.append("      <thead>\n");
    html.append("        <tr>\n");
    html.append("          <th>Cod. Principal</th>\n");
    html.append("          <th>Descripción</th>\n");
    html.append("          <th>Cantidad</th>\n");
    html.append("          <th>Precio Unitario</th>\n");
    html.append("          <th>Descuento</th>\n");
    html.append("          <th>Precio Total</th>\n");
    html.append("        </tr>\n");
    html.append("      </thead>\n");
    html.append("      <tbody>\n");

    // Add invoice details
    if (facturaDto.getDetalles() != null && facturaDto.getDetalles().getDetalle() != null) {
      facturaDto.getDetalles().getDetalle().forEach(detalle -> {
        html.append("        <tr>\n");
        html.append("          <td>").append(HtmlUtils.htmlEscape(detalle.getCodigoPrincipal())).append("</td>\n");
        html.append("          <td>").append(HtmlUtils.htmlEscape(detalle.getDescripcion())).append("</td>\n");
        html.append("          <td class=\"center\">").append(detalle.getCantidad().intValue()).append("</td>\n");
        html.append("          <td class=\"right-align\">").append(String.format("%.2f", detalle.getPrecioUnitario()))
            .append("</td>\n");
        html.append("          <td class=\"right-align\">0.00</td>\n");
        html.append("          <td class=\"right-align\">")
            .append(String.format("%.2f", detalle.getPrecioTotalSinImpuesto())).append("</td>\n");
        html.append("        </tr>\n");
      });
    }

    html.append("      </tbody>\n");
    html.append("    </table>\n");
    html.append("  </div>\n");
    html.append("  <div class=\"resumen-factura\">\n");
    html.append("    <div class=\"forma-pago\">\n");
    html.append("      <table>\n");
    html.append("        <tr>\n");
    html.append("          <th>Forma de pago</th>\n");
    html.append("          <th>Valor</th>\n");
    html.append("        </tr>\n");
    html.append("        <tr>\n");
    html.append("          <td>20 - OTROS CON UTILIZACION DEL SISTEMA FINANCIERO</td>\n");
    html.append("          <td class=\"right-align\">").append(String.format("%.2f", invoice.getInvoiceTotal()))
        .append("</td>\n");
    html.append("        </tr>\n");
    html.append("      </table>\n");
    html.append("    </div>\n");
    html.append("    <div class=\"totals\">\n");
    html.append("      <table>\n");
    html.append("        <tr>\n");
    html.append("          <td class=\"left-align small\">SUBTOTAL 15%</td>\n");
    html.append("          <td class=\"left-align bold\">").append(String.format("%.2f", invoice.getInvoiceSubtotal()))
        .append("</td>\n");
    html.append("        </tr>\n");
    html.append("        <tr>\n");
    html.append("          <td class=\"left-align small\">SUBTOTAL NO OBJETO DE IVA</td>\n");
    html.append("          <td class=\"left-align\">0.00</td>\n");
    html.append("        </tr>\n");
    html.append("        <tr>\n");
    html.append("          <td class=\"left-align small\">SUBTOTAL EXENTO DE IVA</td>\n");
    html.append("          <td class=\"left-align\">0.00</td>\n");
    html.append("        </tr>\n");
    html.append("        <tr>\n");
    html.append("          <td class=\"left-align small\">SUBTOTAL SIN IMPUESTOS</td>\n");
    html.append("          <td class=\"left-align\">").append(String.format("%.2f", invoice.getInvoiceSubtotal()))
        .append("</td>\n");
    html.append("        </tr>\n");
    html.append("        <tr>\n");
    html.append("          <td class=\"left-align small\">TOTAL DESCUENTO</td>\n");
    html.append("          <td class=\"left-align\">").append(String.format("%.2f", invoice.getInvoiceDiscount()))
        .append("</td>\n");
    html.append("        </tr>\n");
    html.append("        <tr>\n");
    html.append("          <td class=\"left-align small\">ICE</td>\n");
    html.append("          <td class=\"left-align\">0.00</td>\n");
    html.append("        </tr>\n");
    html.append("        <tr>\n");
    html.append("          <td class=\"left-align small\">IVA 15%</td>\n");
    html.append("          <td class=\"left-align\">")
        .append(String.format("%.2f", invoice.getInvoiceTotal().subtract(invoice.getInvoiceSubtotal()).doubleValue()))
        .append("</td>\n");
    html.append("        </tr>\n");
    html.append("        <tr>\n");
    html.append("          <td class=\"left-align bold\">VALOR TOTAL</td>\n");
    html.append("          <td class=\"left-align bold\">").append(String.format("%.2f", invoice.getInvoiceTotal()))
        .append("</td>\n");
    html.append("        </tr>\n");
    html.append("      </table>\n");
    html.append("    </div>\n");
    html.append("  </div>\n");
    html.append("  <!-- RESUMEN / TOTALES -->\n");
    html.append("  <div class=\"content-info-box-square\">\n");
    html.append("    <div class=\"info-box-square\">\n");
    html.append("      <div class=\"valor-square-info\">\n");
    html.append("        <div><strong>VALOR TORAL SIN SUBSIDIO:</strong> </div>\n");
    html.append("        <div>0.00</div>\n");
    html.append("      </div>\n");
    html.append("      <div class=\"ahorro-square-info\">\n");
    html.append("        <div><strong>AHORRO POR SUBSIDIO:</strong> <br />(Incluye IVA cuando corresponda)</div>\n");
    html.append("        <div>0.00</div>\n");
    html.append("      </div>\n");
    html.append("    </div>\n");
    html.append("  </div>\n");
    html.append("  </div>\n");// cierra
    html.append("</body>\n");
    html.append("</html>");

    return html.toString();
  }

  public String generarBillingHtml(PdfDocumentDTO data) throws Exception {
    // Generate barcode SVG
    String barcodeSvg = (data.getNautorizacion() != null) ? generarBarcodeBase64(data.getNautorizacion()) : "";
    String styles = """
             @page {
               size: A4;
               margin: 10mm;
             }

             body {
               font-family: Arial, sans-serif;
               font-size: 18px;
               color: #111;
               margin: 0;
               padding: 0;
               height: 100vh;

             }

             .header {
               width: 100%;
               display: block;
             }

             .izquierda {
               width: 40%;
               float: left;
               height: 20%;
             }

             .derecha {
               width: 50%;
              float: right;
              margin-right: 18px;
             }

             .right {
               width: 38%;
               overflow: hidden;
               text-align: right;
             }

             h1 {
               margin: 0;
               font-size: 20px;
               letter-spacing: 2px;
             }

             .small {
               font-size: 10px;
               color: #333;
             }

             .company {
               font-weight: 700;
               font-size: 14px;
             }

             .info-box {
               border: 1px solid #222;
               padding: 8px;
               margin-bottom: 8px;
               border-radius: 10px;
               width: 100%;
             }
             .info-box-social {
               margin-left: 0px ;
               margin-right: 0px;
               border-radius: 10px;
               border: 1px solid #222;
               padding: 8px;

             }


             .content-info-box-square {
               text-align: right;
             }

             .info-box-square {
               border: 1px solid #222;
               padding: 8px;
               width: 38.5%;
               margin-bottom: 8px;
               font-size: 10px;
               display: inline-block;
             }

             .valor-square-info {
               overflow: hidden;
             }

             .valor-square-info div:first-child {
               float: left;
             }

             .valor-square-info div:last-child {
               float: right;
             }

             .ahorro-square-info {
               overflow: hidden;
               margin-top: 5px;
             }

             .ahorro-square-info div:first-child {
               float: left;
               width: 70%;
             }

             .ahorro-square-info div:last-child {
               float: right;
               width: 30%;
               text-align: right;
             }

             .two-cols {
               width: 100%;
               overflow: hidden;
             }

             .col {
               float: left;
               width: 48%;
             }

             .col + .col {
               margin-left: 4%;
             }

             table {
               width: 100%;
               border-collapse: collapse;
               margin-top: 6px;
             }

             th,
             td {
               border: 1px solid #000000;
               padding: 6px;
               text-align: left;
               font-size: 13px;
             }

             th {
               font-weight: 700;
               font-size: 13px;
             }

             .right-align {
               text-align: right;
             }

             .left-align {
               text-align: left;
             }

             .totals {
               width: 40%;
               float: right;
             }

             .totals table td {
               border-collapse: collapse;
             }

             .bold {
               font-weight: 700;
             }

             .center {
               text-align: center;
             }

             .small-note {
               font-size: 10px;
               color: #555;
               margin-top: 16px;
             }

             .access-key-info {
               text-align: center;
             }

             .access-key {
               width: 30%;
               display: inline-block;
             }

             .info-factura {
               overflow: hidden;
             }

             .info-factura-no {
               float: left;
               margin-right: 2rem;
             }

             .info-factura-numero {
               float: left;
             }

             .ruc-info {
               overflow: hidden;
               font-weight: 700;
               font-size: 23px;
             }

             .ruc {
               float: left;
               margin-right: 4rem;
             }

             .factura-info {
               font-weight: 700;
               font-size: 23px;
             }

             .fecha-info {
               overflow: hidden;
             }

             .fecha {
               float: left;
               margin-right: 2rem;
             }

             .ambiente-info {
               overflow: hidden;
             }

             .ambiente {
               float: left;
               margin-right: 2rem;
             }

             .emision-info {
               overflow: hidden;
             }

             .emision {
               float: left;
               margin-right: 2rem;
             }

             .direccion-matriz-info {
               overflow: hidden;
               font-size: 16px;
             }

             .direccion-matriz {
               float: left;
               margin-right: 2rem;
             }

             .direccion-sucursal-info {
               overflow: hidden;
               font-size: 16px;
             }

             .direccion-sucursal {
               float: left;
               margin-right: 2rem;
             }

             .fin-info {
               font-size: 16px;
               margin-top: 3%;
             }

             .nombres-info {
               overflow: hidden;
               font-size: 16px;
             }

             .nombres {
               float: left;
               margin-right: 5rem;
               font-weight: bold;
             }

             .identificacion-info {
               overflow: hidden;
               font-size: 16px;
             }

             .identificacion {
               float: left;
               margin-right: 3rem;
             }

             .fecha-nombres-info {
               overflow: hidden;
               font-size: 16px;
             }

             .fecha-nombres {
               float: left;
               margin-right: 3rem;
             }

             .direccion-nombres-info {
               overflow: hidden;
               font-size: 16px;
             }

             .direccion-nombres {
               float: left;
               margin-right: 3rem;
             }

             .resumen-factura {
               width: 100%;
               overflow: hidden;
             }

             .forma-pago {
               margin-top: 5%;
               width: 58%;
               float: left;
             }

             .clear {
               clear: both;
             }

             .numero-autorizacion{
               font-size: 12px;
               text-align: center;
             }

        """;
    StringBuilder html = new StringBuilder();
    html.append(
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
    html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"es\">\n");
    html.append("<head>\n");
    html.append("  <meta charset=\"utf-8\" />\n");
    html.append("  <title>Factura</title>\n");
    html.append("  <style>\n").append(styles).append("  </style>\n");
    html.append("</head>\n");
    html.append("<body>\n");
    html.append("  <!-- HEADER -->\n");
    html.append("  <div class=\"header\" style=\"height: 100%\">\n");
    html.append("    <div class=\"izquierda\" style=\"margin-top:23%;\">\n");
    html.append("      <div class=\"info-box\">\n");
    html.append("        <div>").append(HtmlUtils.htmlEscape(data.getNombrePrincipal())).append("</div>\n");
    html.append("        <div style=\"margin-top: 3%;\">").append(HtmlUtils.htmlEscape(data.getNombreEmpresa()))
        .append("</div>\n");
    html.append("        <div class=\"direccion-matriz-info\" style=\"margin-top: 3%;\">\n");
    html.append("          <div class=\"direccion-matriz\"><strong>Dirección <br /> Matriz:</strong></div>\n");
    html.append("          <div style=\"margin-top: 3%;\">")
        .append(HtmlUtils.htmlEscape(data.getDireccionMatriz() != null ? data.getDireccionMatriz() : "N/A"))
        .append("</div>\n");
    html.append("        </div>\n");
    html.append("        <div class=\"direccion-sucursal-info\" style=\"margin-top: 3%;\">\n");
    html.append("          <div class=\"direccion-sucursal\"><strong>Dirección <br />Sucursal:</strong></div>\n");
    html.append("          <div style=\"margin-top:3%;\">")
        .append(HtmlUtils.htmlEscape(data.getDireccionSucursal() != null ? data.getDireccionSucursal() : "N/A"))
        .append("</div>\n");
    html.append("        </div>\n");
    html.append(
        "        <div class=\"fin-info\" style=\"margin-bottom:8%;\"> <strong>OBLIGADO A LLEVAR CONTABILIDAD:</strong> ")
        .append(HtmlUtils.htmlEscape(data.getContabilidad())).append("</div>\n");
    html.append("      </div>\n");
    html.append("    </div>\n");
    html.append("    <div class=\"derecha\">\n");
    html.append("      <div class=\" info-box\">\n");
    html.append("        <div class=\"ruc-info\">\n");
    html.append("          <div class=\"ruc\"><strong>R.U.C.:</strong></div>\n");
    html.append("          <div><span class=\"bold\">").append(data.getRuc()).append("</span></div>\n");
    html.append("        </div>\n");
    html.append("        <div class=\"factura-info\"><strong>FACTURA</strong></div>\n");
    html.append("        <div class=\"info-factura\" style=\"margin-top:3%;\">\n");
    html.append("          <div class=\"info-factura-no\">No.</div>\n");
    html.append("          <div class=\"info-factura-numero\">").append(data.getNfactura()).append("</div>\n");
    html.append("        </div>\n");
    html.append("        <div style=\"margin-top:3%;\">NÚMERO DE AUTORIZACIÓN</div>\n");
    html.append("        <div class=\"numero-autorizacion\" style=\"margin-top:3%;\">").append(data.getNautorizacion())
        .append("</div>\n");
    html.append("        <div class=\"fecha-info\">\n");
    html.append(
        "          <div class=\"fecha\" style=\"margin-top:3%;\"><strong>FECHA Y HORA DE <br />AUTORIZACIÓN:</strong></div>\n");
    html.append("          <div style=\"margin-top:7%;\">").append(data.getFechaautorizacion()).append("</div>\n");
    html.append("        </div>\n");
    html.append("        <div class=\"ambiente-info\">\n");
    html.append("          <div class=\"ambiente\" style=\"margin-top:3%;\"><strong>AMBIENTE:</strong></div>\n");
    html.append("          <div style=\"margin-top:3%;\">").append(data.getAmbiente()).append("</div>\n");
    html.append("        </div>\n");
    html.append("        <div class=\"emision-info\">\n");
    html.append("          <div class=\"emision\" style=\"margin-top: 3%;\"><strong>EMISIÓN:</strong></div>\n");
    html.append("          <div style=\"margin-top: 3%;\">").append(data.getEmision()).append("</div>\n");
    html.append("        </div>\n");
    html.append("        <div style=\"margin-top:3%;\"><strong>CLAVE DE ACCESO</strong></div>\n");
    html.append("        <div class=\"access-key-info\">\n");
    html.append("          <div class=\"access-key\"><img src='data:image/png;base64,")
        .append(barcodeSvg)
        .append("' alt='barcode' style='width: 300px; height: auto;' /></div>\n");
    html.append("        <div class=\"numero-autorizacion\" style=\"margin-top:3%;\">").append(data.getNautorizacion())
        .append("</div>\n");
    html.append("        </div>\n");
    html.append("      </div>\n");
    html.append("    </div>\n");
    html.append("  </div>\n");
    // abierto
    html.append("  <div  style=\"display: block; margin-top: 65% \">\n");
    html.append("  <div class=\"info-box-social\" style=\"margin-top:1%;  \">\n");
    html.append("    <div class=\"nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("      <div class=\"nombres\" style=\"margin-top:1%;\">Razón Social / Nombres y Apellidos:</div>\n");
    html.append("      <div style=\"margin-top:1%;\">").append(HtmlUtils.htmlEscape(data.getNombreApellidos()))
        .append("</div>\n");
    html.append("    </div>\n");
    html.append("    <div class=\"identificacion-info\" style=\"margin-top:1%;\">\n");
    html.append(
        "      <div class=\"identificacion\" style=\"margin-top:3%;\"><strong>Identificación:</strong></div>\n");
    html.append("      <div style=\"margin-top:3%;\">").append(HtmlUtils.htmlEscape(data.getIdentificacion()))
        .append("</div>\n");
    html.append("    </div>\n");
    html.append("    <div class=\"fecha-nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("      <div class=\"fecha-nombres\" style=\"margin-top:3%;\"><strong>Fecha:</strong> </div>\n");
    html.append("      <div style=\"margin-top:3%;\">\n");
    html.append("        ").append(data.getFecha()).append("\n");
    html.append("      </div>\n");
    html.append("    </div>\n");
    html.append("    <div class=\"direccion-nombres-info\" style=\"margin-top:1%;\">\n");
    html.append(
        "      <div class=\"direccion-nombres\" style=\"margin-top: 0%;\"><strong>Dirección:</strong> </div>\n");
    html.append("      <div style=\"margin-top: 0%;\">")
        .append(HtmlUtils.htmlEscape(data.getDireccion() != null ? data.getDireccion() : "N/A")).append("</div>\n");
    html.append("    </div>\n");
    html.append("  </div>\n");

    html.append("  <!-- DETALLE DE ITEMS -->\n");
    html.append("  <div style=\"margin-top:10px;\">\n");
    html.append("    <table>\n");
    html.append("      <thead>\n");
    html.append("        <tr>\n");
    html.append("          <th>Cod. Principal</th>\n");
    html.append("          <th>Descripción</th>\n");
    html.append("          <th>Cantidad</th>\n");
    html.append("          <th>Precio Unitario</th>\n");
    html.append("          <th>Descuento</th>\n");
    html.append("          <th>Precio Total</th>\n");
    html.append("        </tr>\n");
    html.append("      </thead>\n");
    html.append("      <tbody>\n");

    // Add invoice details
    if (data.getDatosFactura() != null) {
      data.getDatosFactura().forEach(detalle -> {
        html.append("        <tr>\n");
        html.append("          <td>").append(HtmlUtils.htmlEscape(detalle.getCodPrincipal())).append("</td>\n");
        html.append("          <td>").append(HtmlUtils.htmlEscape(detalle.getDescripcion())).append("</td>\n");
        html.append("          <td class=\"center\">").append(detalle.getCantidad()).append("</td>\n");
        html.append("          <td class=\"right-align\">").append(detalle.getPrecioUnit()).append("</td>\n");
        html.append("          <td class=\"right-align\">").append(detalle.getDescuento()).append("</td>\n");
        html.append("          <td class=\"right-align\">").append(detalle.getPrecioTotal()).append("</td>\n");
        html.append("        </tr>\n");
      });
    }

    html.append("      </tbody>\n");
    html.append("    </table>\n");
    html.append("  </div>\n");
    html.append("  <div class=\"resumen-factura\">\n");
    html.append("    <div class=\"forma-pago\">\n");
    html.append("      <table>\n");
    html.append("        <tr>\n");
    html.append("          <th>Forma de pago</th>\n");
    html.append("          <th>Valor</th>\n");
    html.append("        </tr>\n");
    if (data.getFormaPagoDTOS() != null) {
      data.getFormaPagoDTOS().forEach(pago -> {
        html.append("        <tr>\n");
        html.append("          <td>").append(HtmlUtils.htmlEscape(pago.getFormaPago())).append("</td>\n");
        html.append("          <td class=\"right-align\">").append(pago.getValorpago()).append("</td>\n");
        html.append("        </tr>\n");
      });
    }
    html.append("      </table>\n");
    html.append("    </div>\n");
    html.append("    <div class=\"totals\">\n");
    html.append("      <table>\n");
    if (data.getDetalleDTOS() != null && !data.getDetalleDTOS().isEmpty()) {
      PdfSubDetallesDTO detalle = data.getDetalleDTOS().get(0);
      html.append("        <tr>\n");
      html.append("          <td class=\"left-align small\">SUBTOTAL 15%</td>\n");
      html.append("          <td class=\"left-align bold\">").append(detalle.getSubtotal15porciento())
          .append("</td>\n");
      html.append("        </tr>\n");
      html.append("        <tr>\n");
      html.append("          <td class=\"left-align small\">SUBTOTAL NO OBJETO DE IVA</td>\n");
      html.append("          <td class=\"left-align\">").append(detalle.getNoobjiva()).append("</td>\n");
      html.append("        </tr>\n");
      html.append("        <tr>\n");
      html.append("          <td class=\"left-align small\">SUBTOTAL EXENTO DE IVA</td>\n");
      html.append("          <td class=\"left-align\">").append(detalle.getNoextiva()).append("</td>\n");
      html.append("        </tr>\n");
      html.append("        <tr>\n");
      html.append("          <td class=\"left-align small\">SUBTOTAL SIN IMPUESTOS</td>\n");
      html.append("          <td class=\"left-align\">").append(detalle.getSinimpuesto()).append("</td>\n");
      html.append("        </tr>\n");
      html.append("        <tr>\n");
      html.append("          <td class=\"left-align small\">TOTAL DESCUENTO</td>\n");
      html.append("          <td class=\"left-align\">").append(detalle.getTotaldesc()).append("</td>\n");
      html.append("        </tr>\n");
      html.append("        <tr>\n");
      html.append("          <td class=\"left-align small\">ICE</td>\n");
      html.append("          <td class=\"left-align\">").append(detalle.getIce()).append("</td>\n");
      html.append("        </tr>\n");
      html.append("        <tr>\n");
      html.append("          <td class=\"left-align small\">IVA 15%</td>\n");
      html.append("          <td class=\"left-align\">").append(detalle.getIva15()).append("</td>\n");
      html.append("        </tr>\n");
      html.append("        <tr>\n");
      html.append("          <td class=\"left-align bold\">VALOR TOTAL</td>\n");
      html.append("          <td class=\"left-align bold\">").append(detalle.getValortotal()).append("</td>\n");
      html.append("        </tr>\n");
    }
    html.append("      </table>\n");
    html.append("    </div>\n");
    html.append("  </div>\n");
    html.append("  <!-- RESUMEN / TOTALES -->\n");
    html.append("  <div class=\"content-info-box-square\">\n");
    html.append("    <div class=\"info-box-square\">\n");
    html.append("      <div class=\"valor-square-info\">\n");
    html.append("        <div><strong>VALOR TORAL SIN SUBSIDIO:</strong> </div>\n");
    html.append("        <div>0.00</div>\n");
    html.append("      </div>\n");
    html.append("      <div class=\"ahorro-square-info\">\n");
    html.append("        <div><strong>AHORRO POR SUBSIDIO:</strong> <br />(Incluye IVA cuando corresponda)</div>\n");
    html.append("        <div>0.00</div>\n");
    html.append("      </div>\n");
    html.append("    </div>\n");
    html.append("  </div>\n");
    html.append("  </div>\n");// cierra
    html.append("</body>\n");
    html.append("</html>");

    return html.toString();
  }

  public byte[] generatePdfFromBillingHtml(PdfDocumentDTO data) throws Exception {
    String htmlContent = generarBillingHtml(data);
    return generatePdfFromHtml(htmlContent);
  }

  public byte[] re_generatePdfTicketHtml(InvoiceHeaderDTO invoiceHeaderDTO) throws Exception {

    InvoiceModel invoice = invoiceRepository.findById(invoiceHeaderDTO.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));

    String htmlContent = " ";
    if (invoice.getInvoiceType().equals("FACTURA")) {

      htmlContent = re_generateTicketHtml(invoiceHeaderDTO);
    } else if (invoice.getInvoiceType().equals("VOUCHER")) {

      htmlContent = re_generateTicketComprobanteHtml(invoiceHeaderDTO);
    }
    return generatePdfFromHtml(htmlContent);
  }

  public String generateTicketHtml(InvoiceModel invoice, FacturaSRIDTO facturaDto, String numeroAutorizacion,
      String fechaAutorizacion, String claveAcceso, Long enterpriseId) throws Exception {

    // Get enterprise info
    Optional<EnterpriseModel> enterpriseOpt = enterpriseService.getEnterpriseByRuc(facturaDto.rucEmpresa);
    EnterpriseModel enterprise = enterpriseOpt.orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

    // Get client info
    ClientModel client = clientService.getClientById(invoice.getClientId().getId());

    String styles = """
                          @page {
                size: auto;
        margin: 10mm;
              }

                  body {
                    font-family: Arial, sans-serif;
                    margin: 0;
                    font-size: 12px;
                    padding: 0;
                  }

                  .header {
                    width: 100%;
                    display: block;
                    align-items: center;
                    font-size: 12px;
                  }

                  .valor-square-info {
                    overflow: hidden;
                  }

                  .valor-square-info div:first-child {
                    float: left;
                  }

                  .valor-square-info div:last-child {
                    float: right;
                  }

                  .ahorro-square-info {
                    overflow: hidden;
                    margin-top: 3px;
                  }

                  .ahorro-square-info div:first-child {
                    float: left;
                    width: 70%;
                  }

                  .ahorro-square-info div:last-child {
                    float: right;
                    width: 30%;
                    text-align: right;
                  }

                  th,
                  td {
                    padding: 3px;
                    text-align: left;
                    font-size: 10px;
                  }

                  th {
                    font-weight: 700;
                    font-size: 12px;
                  }

                  .right-align {
                    text-align: right;
                  }

                  .center-align {
                    text-align: center;
                  }

                  .left-align {
                    text-align: left;
                  }

                  .totals {
                    width: 40%;
                    float: left;
                  }

                  .totals table td {
                    border-collapse: collapse;
                  }

                  .bold {
                    font-weight: 700;
                  }

                  .center {
                    text-align: center;
                  }

                  .small-note {
                    font-size: 8px;
                    color: #555;
                    margin-top: 10px;
                  }

                  .access-key-info {
                    text-align: center;
                  }

                  .access-key {
                    width: 30%;
                    display: inline-block;
                  }

                  .info-factura {
                    overflow: hidden;
                  }

                  .info-factura-no {
                    float: left;
                    margin-right: 1rem;
                    font-size: 15px;
                  }

                  .info-factura-numero {
                    font-size: 15px;
                    float: left;
                  }

                  .ruc-info {
                    overflow: hidden;
                    font-weight: 700;
                    font-size: 15px;
                  }

                  .ruc {
                    float: left;
                    margin-right: 2rem;
                  }

                  .factura-info {
                    font-weight: 700;
                    font-size: 14px;
                  }

                  .fecha-info {
                    overflow: hidden;
                  }

                  .fecha {
                    float: left;
                    margin-right: 1rem;
                  }

                  .ambiente-info {
                    overflow: hidden;
                  }

                  .ambiente {
                    float: left;
                    margin-right: 1rem;
                  }

                  .emision-info {
                    overflow: hidden;
                  }

                  .emision {
                    float: left;
                    margin-right: 1rem;
                  }

                  .direccion-matriz-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .direccion-matriz {
                    float: left;
                    margin-right: 1rem;
                  }

                  .direccion-sucursal-info {
                    overflow: hidden;
                    font-size: 10px;
                  }

                  .direccion-sucursal {
                    float: left;
                    margin-right: 1rem;
                  }

                  .fin-info {
                    font-size: 10px;
                    margin-top: 2%;
                  }

                  .nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .nombres {
                    float: left;
                    margin-right: 3rem;
                    font-weight: bold;
                  }

                  .identificacion-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .identificacion {
                    float: left;
                    margin-right: 2rem;
                  }

                  .fecha-nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .fecha-nombres {
                    float: left;
                    margin-right: 2rem;
                  }

                  .direccion-nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .direccion-nombres {
                    float: left;
                    margin-right: 2rem;
                  }

                  .resumen-factura {
                    width: 100%;
                    overflow: hidden;
                  }

                  .forma-pago {
                    margin-top: 3%;
                    width: 58%;
                    float: left;
                  }

                  .clear {
                    clear: both;
                  }

                  .numero-autorizacion {
                    font-size: 10px;
                  }

                  .invoice {
                    margin: 0;
                    padding: 0;
                    width: 100%
                  }

                  .nombreempresa{
                    font-size: 18px;
                  }
                          """;
    StringBuilder html = new StringBuilder();
    html.append(
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
    html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"es\">\n");
    html.append("<head>\n");
    html.append("  <meta charset=\"utf-8\" />\n");
    html.append("  <title>Ticket</title>\n");
    html.append("  <style>\n").append(styles).append("  </style>\n");
    html.append("</head>\n");
    html.append("<body>\n");
    html.append("<div class=\"invoice\">\n");

    // HEADER
    html.append("  <!-- HEADER -->\n");
    html.append("  <div class=\"header\" style=\"height: 100%;\">\n");
    html.append("  \n");
    html.append("        <div class=\"nombreempresa\"  style=\"margin-top: 3%;\">")
        .append(HtmlUtils.htmlEscape(enterprise.getEnterpriseName())).append("</div>\n");

    html.append("        <div class=\"direccion-matriz-info\">\n");
    html.append("          <div class=\"direccion-matriz\"><strong>Dirección Matriz:</strong></div>\n");
    html.append("          <div>" +
        (facturaDto.infoFactura.getDirEstablecimiento() != null ? facturaDto.infoFactura.getDirEstablecimiento()
            : "N/A")
        + "</div>\n");
    html.append("        </div>\n");

    html.append("        <div class=\"ruc-info\">\n");
    html.append("          <div class=\"ruc\"><strong>R.U.C.:</strong></div>\n");
    html.append("          <div><span class=\"bold\">").append(facturaDto.rucEmpresa).append("</span></div>\n");
    html.append("        </div>\n");

    html.append("        <div class=\"factura-info\"><strong>FACTURA</strong></div>\n");

    html.append("        <div class=\"info-factura\" style=\"margin-top:3%;\">\n");
    html.append("          <div class=\"info-factura-no\">No.</div>\n");
    html.append("          <div class=\"info-factura-numero\">").append(invoice.getEstablishment()).append("-")
        .append(invoice.getRemissionGuide()).append("-")
        .append(String.format("%09d", Integer.parseInt(invoice.getSequential()))).append("</div>\n");
    html.append("        </div>\n");

    html.append("        <div style=\"margin-top:3%;\">CLAVE DE AUTORIZACIÓN</div>\n");
    html.append("        <div class=\"numero-autorizacion\" style=\"margin-top:3%;\">").append(numeroAutorizacion)
        .append("</div>\n");

    html.append("        <div class=\"fecha-info\">\n");
    html.append("          <div class=\"fecha\" style=\"margin-top:3%;\"><strong>FECHA: </strong>")
        .append(fechaAutorizacion).append("</div>\n");
    html.append("        </div>\n");

    html.append("        <div class=\"ambiente-info\">\n");
    html.append("          <div class=\"ambiente\" style=\"margin-top:3%;\"><strong>AMBIENTE:</strong></div>\n");
    html.append("          <div style=\"margin-top:3%;\">PRODUCCION</div>\n");
    html.append("        </div>\n");

    html.append("        <div class=\"emision-info\">\n");
    html.append("          <div class=\"emision\" style=\"margin-top: 3%;\"><strong>EMISIÓN:</strong></div>\n");
    html.append("          <div style=\"margin-top: 3%;\">NORMAL</div>\n");
    html.append("        </div>\n");
    html.append("  </div>\n");
    html.append("  <hr style=\"border: 1px solid #000; margin: 10px 0;\"/>\n");
    // INFORMACIÓN DEL CLIENTE
    html.append("  <div>\n");

    html.append("      <div class=\"nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("        <div class=\"nombres\">Nombres y Apellidos:</div>\n");
    html.append("        <div>").append(HtmlUtils.htmlEscape(client.getClientFullName())).append("</div>\n");
    html.append("      </div>\n");

    html.append("      <div class=\"identificacion-info\" style=\"margin-top:1%;\">\n");
    html.append("        <div class=\"identificacion\"><strong>Identificación:</strong></div>\n");
    html.append("        <div>").append(HtmlUtils.htmlEscape(client.getClientRuc())).append("</div>\n");
    html.append("      </div>\n");

    html.append("      <div class=\"fecha-nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("        <div class=\"fecha-nombres\"><strong>Fecha:</strong></div>\n");
    html.append("        <div>").append(invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        .append("</div>\n");
    html.append("      </div>\n");

    html.append("      <div class=\"direccion-nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("        <div class=\"direccion-nombres\"><strong>Dirección:</strong></div>\n");
    html.append("        <div>")
        .append(HtmlUtils.htmlEscape(client.getClientAddress() != null ? client.getClientAddress() : "N/A"))
        .append("</div>\n");
    html.append("      </div>\n");
    html.append("  <hr style=\"border: 1px solid #000; margin: 10px 0;\"/>\n");

    // DETALLE
    html.append("    <div>\n");
    html.append("      <table>\n");
    html.append("        <thead>\n");
    html.append("        <tr>\n");
    html.append("          <th>Descrip</th>\n");
    html.append("          <th>Cant</th>\n");
    html.append("          <th>P.Unit</th>\n");
    html.append("          <th>P.Total</th>\n");
    html.append("        </tr>\n");
    html.append("        </thead>\n");
    html.append("        <tbody>\n");

    // Add invoice details
    if (facturaDto.getDetalles() != null && facturaDto.getDetalles().getDetalle() != null) {
      facturaDto.getDetalles().getDetalle().forEach(detalle -> {
        html.append("          <tr>\n");
        html.append("            <td>").append(HtmlUtils.htmlEscape(detalle.getDescripcion())).append("</td>\n");
        html.append("            <td >").append(detalle.getCantidad().intValue()).append("</td>\n");
        html.append("            <td>").append(String.format("%.2f", detalle.getPrecioUnitario())).append("</td>\n");
        html.append("            <td >").append(String.format("%.2f", detalle.getPrecioTotalSinImpuesto()))
            .append("</td>\n");
        html.append("          </tr>\n");
      });
    }

    html.append("        </tbody>\n");
    html.append("      </table>\n");
    html.append("    </div>\n");

    // RESUMEN
    html.append("    <div class=\"resumen-factura\">\n");

    html.append("      <div class=\"totals\">\n");
    html.append("        <table>\n");
    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL 15%</td>\n");
    html.append("            <td class=\"left-align bold\">")
        .append(String.format("%.2f", invoice.getInvoiceSubtotal())).append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL NO OBJETO DE IVA</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL EXENTO DE IVA</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL SIN IMPUESTOS</td>\n");
    html.append("            <td class=\"left-align\">").append(String.format("%.2f", invoice.getInvoiceSubtotal()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">TOTAL DESCUENTO</td>\n");
    html.append("            <td class=\"left-align\">").append(String.format("%.2f", invoice.getInvoiceDiscount()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">ICE</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">IVA 15%</td>\n");
    html.append("            <td class=\"left-align\">")
        .append(String.format("%.2f", invoice.getInvoiceTotal().subtract(invoice.getInvoiceSubtotal()).doubleValue()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align bold\">VALOR TOTAL</td>\n");
    html.append("            <td class=\"left-align bold\">").append(String.format("%.2f", invoice.getInvoiceTotal()))
        .append("</td>\n");
    html.append("          </tr>\n");
    html.append("        </table>\n");
    html.append("      </div>\n");

    html.append("    </div>\n");

    html.append("  </div>\n");

    html.append("</div>\n");
    html.append("</body>\n");
    html.append("</html>");

    return html.toString();
  }

  public String re_generateTicketHtml(InvoiceHeaderDTO invoice) throws Exception {

    // Get enterprise info
    String styles = """
                          @page {
                size: auto;
        margin: 10mm;
              }

                  body {
                    font-family: Arial, sans-serif;
                    margin: 0;
                    font-size: 12px;
                    padding: 0;
                  }

                  .header {
                    width: 100%;
                    display: block;
                    align-items: center;
                    font-size: 12px;
                  }

                  .valor-square-info {
                    overflow: hidden;
                  }

                  .valor-square-info div:first-child {
                    float: left;
                  }

                  .valor-square-info div:last-child {
                    float: right;
                  }

                  .ahorro-square-info {
                    overflow: hidden;
                    margin-top: 3px;
                  }

                  .ahorro-square-info div:first-child {
                    float: left;
                    width: 70%;
                  }

                  .ahorro-square-info div:last-child {
                    float: right;
                    width: 30%;
                    text-align: right;
                  }

                  th,
                  td {
                    padding: 3px;
                    text-align: left;
                    font-size: 10px;
                  }

                  th {
                    font-weight: 700;
                    font-size: 12px;
                  }

                  .right-align {
                    text-align: right;
                  }

                  .center-align {
                    text-align: center;
                  }

                  .left-align {
                    text-align: left;
                  }

                  .totals {
                    width: 40%;
                    float: left;
                  }

                  .totals table td {
                    border-collapse: collapse;
                  }

                  .bold {
                    font-weight: 700;
                  }

                  .center {
                    text-align: center;
                  }

                  .small-note {
                    font-size: 8px;
                    color: #555;
                    margin-top: 10px;
                  }

                  .access-key-info {
                    text-align: center;
                  }

                  .access-key {
                    width: 30%;
                    display: inline-block;
                  }

                  .info-factura {
                    overflow: hidden;
                  }

                  .info-factura-no {
                    float: left;
                    margin-right: 1rem;
                    font-size: 15px;
                  }

                  .info-factura-numero {
                    font-size: 15px;
                    float: left;
                  }

                  .ruc-info {
                    overflow: hidden;
                    font-weight: 700;
                    font-size: 15px;
                  }

                  .ruc {
                    float: left;
                    margin-right: 2rem;
                  }

                  .factura-info {
                    font-weight: 700;
                    font-size: 14px;
                  }

                  .fecha-info {
                    overflow: hidden;
                  }

                  .fecha {
                    float: left;
                    margin-right: 1rem;
                  }

                  .ambiente-info {
                    overflow: hidden;
                  }

                  .ambiente {
                    float: left;
                    margin-right: 1rem;
                  }

                  .emision-info {
                    overflow: hidden;
                  }

                  .emision {
                    float: left;
                    margin-right: 1rem;
                  }

                  .direccion-matriz-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .direccion-matriz {
                    float: left;
                    margin-right: 1rem;
                  }

                  .direccion-sucursal-info {
                    overflow: hidden;
                    font-size: 10px;
                  }

                  .direccion-sucursal {
                    float: left;
                    margin-right: 1rem;
                  }

                  .fin-info {
                    font-size: 10px;
                    margin-top: 2%;
                  }

                  .nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .nombres {
                    float: left;
                    margin-right: 3rem;
                    font-weight: bold;
                  }

                  .identificacion-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .identificacion {
                    float: left;
                    margin-right: 2rem;
                  }

                  .fecha-nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .fecha-nombres {
                    float: left;
                    margin-right: 2rem;
                  }

                  .direccion-nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .direccion-nombres {
                    float: left;
                    margin-right: 2rem;
                  }

                  .resumen-factura {
                    width: 100%;
                    overflow: hidden;
                  }

                  .forma-pago {
                    margin-top: 3%;
                    width: 58%;
                    float: left;
                  }

                  .clear {
                    clear: both;
                  }

                  .numero-autorizacion {
                    font-size: 10px;
                  }

                  .invoice {
                    margin: 0;
                    padding: 0;
                    width: 100%
                  }

                  .nombreempresa{
                    font-size: 18px;
                  }
                          """;
    StringBuilder html = new StringBuilder();
    html.append(
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
    html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"es\">\n");
    html.append("<head>\n");
    html.append("  <meta charset=\"utf-8\" />\n");
    html.append("  <title>Ticket</title>\n");
    html.append("  <style>\n").append(styles).append("  </style>\n");
    html.append("</head>\n");
    html.append("<body>\n");
    html.append("<div class=\"invoice\">\n");

    // HEADER
    html.append("  <!-- HEADER -->\n");
    html.append("  <div class=\"header\" style=\"height: 100%;\">\n");
    html.append("  \n");
    html.append("        <div class=\"nombreempresa\"  style=\"margin-top: 3%;\">")
        .append(HtmlUtils.htmlEscape(invoice.getEnterpriseName())).append("</div>\n");

    html.append("        <div class=\"direccion-matriz-info\">\n");
    html.append("          <div class=\"direccion-matriz\"><strong>Dirección Matriz:</strong></div>\n");
    html.append("          <div>" +
        (invoice.getEstablishmentAddress() != null ? invoice.getEstablishmentAddress()
            : "N/A")
        + "</div>\n");
    html.append("        </div>\n");

    html.append("        <div class=\"ruc-info\">\n");
    html.append("          <div class=\"ruc\"><strong>R.U.C.:</strong></div>\n");
    html.append("          <div><span class=\"bold\">").append(invoice.getRucEnterprise()).append("</span></div>\n");
    html.append("        </div>\n");

    html.append("        <div class=\"factura-info\"><strong>FACTURA</strong></div>\n");

    html.append("        <div class=\"info-factura\" style=\"margin-top:3%;\">\n");
    html.append("          <div class=\"info-factura-no\">No.</div>\n");
    html.append("          <div class=\"info-factura-numero\">").append(invoice.getEstablishment()).append("-")
        .append(invoice.getRemissionGuide()).append("-")
        .append(String.format("%09d", Integer.parseInt(invoice.getSequential()))).append("</div>\n");
    html.append("        </div>\n");

    html.append("        <div style=\"margin-top:3%;\">CLAVE DE AUTORIZACIÓN</div>\n");
    html.append("        <div class=\"numero-autorizacion\" style=\"margin-top:3%;\">").append(invoice.getAccessKey())
        .append("</div>\n");

    html.append("        <div class=\"fecha-info\">\n");
    html.append("          <div class=\"fecha\" style=\"margin-top:3%;\"><strong>FECHA: </strong>")
        .append(invoice.getFechaAutorizacion()).append("</div>\n");
    html.append("        </div>\n");

    html.append("        <div class=\"ambiente-info\">\n");
    html.append("          <div class=\"ambiente\" style=\"margin-top:3%;\"><strong>AMBIENTE:</strong></div>\n");
    html.append("          <div style=\"margin-top:3%;\">PRODUCCION</div>\n");
    html.append("        </div>\n");

    html.append("        <div class=\"emision-info\">\n");
    html.append("          <div class=\"emision\" style=\"margin-top: 3%;\"><strong>EMISIÓN:</strong></div>\n");
    html.append("          <div style=\"margin-top: 3%;\">NORMAL</div>\n");
    html.append("        </div>\n");
    html.append("  </div>\n");
    html.append("  <hr style=\"border: 1px solid #000; margin: 10px 0;\"/>\n");
    // INFORMACIÓN DEL CLIENTE
    html.append("  <div>\n");

    html.append("      <div class=\"nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("        <div class=\"nombres\">Nombres y Apellidos:</div>\n");
    html.append("        <div>").append(HtmlUtils.htmlEscape(invoice.getClientFullName())).append("</div>\n");
    html.append("      </div>\n");

    html.append("      <div class=\"identificacion-info\" style=\"margin-top:1%;\">\n");
    html.append("        <div class=\"identificacion\"><strong>Identificación:</strong></div>\n");
    html.append("        <div>").append(HtmlUtils.htmlEscape(invoice.getClientRuc())).append("</div>\n");
    html.append("      </div>\n");

    html.append("      <div class=\"fecha-nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("        <div class=\"fecha-nombres\"><strong>Fecha:</strong></div>\n");
    html.append("        <div>").append(invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        .append("</div>\n");
    html.append("      </div>\n");

    html.append("      <div class=\"direccion-nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("        <div class=\"direccion-nombres\"><strong>Dirección:</strong></div>\n");
    html.append("        <div>")
        .append(HtmlUtils.htmlEscape(invoice.getClientAddress() != null ? invoice.getClientAddress() : "N/A"))
        .append("</div>\n");
    html.append("      </div>\n");
    html.append("  <hr style=\"border: 1px solid #000; margin: 10px 0;\"/>\n");

    // DETALLE
    html.append("    <div>\n");
    html.append("      <table>\n");
    html.append("        <thead>\n");
    html.append("        <tr>\n");
    html.append("          <th>Descrip</th>\n");
    html.append("          <th>Cant</th>\n");
    html.append("          <th>P.Unit</th>\n");
    html.append("          <th>P.Total</th>\n");
    html.append("        </tr>\n");
    html.append("        </thead>\n");
    html.append("        <tbody>\n");

    // Add invoice details
    if (invoice.getDetalles() != null && invoice.getDetalles() != null) {
      invoice.getDetalles().forEach(detalle -> {
        html.append("          <tr>\n");
        html.append("            <td>").append(HtmlUtils.htmlEscape(detalle.getDescripcion())).append("</td>\n");
        html.append("            <td >").append(detalle.getCantidad().intValue()).append("</td>\n");
        html.append("            <td>").append(String.format("%.2f", detalle.getPrecioUnitario())).append("</td>\n");
        html.append("            <td >")
            .append(String.format("%.2f", detalle.getPrecioTotalSinImpuestoalueWithoutTax()))
            .append("</td>\n");
        html.append("          </tr>\n");
      });
    }

    html.append("        </tbody>\n");
    html.append("      </table>\n");
    html.append("    </div>\n");

    // RESUMEN
    html.append("    <div class=\"resumen-factura\">\n");

    html.append("      <div class=\"totals\">\n");
    html.append("        <table>\n");
    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL 15%</td>\n");
    html.append("            <td class=\"left-align bold\">")
        .append(String.format("%.2f", invoice.getInvoiceSubtotal())).append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL NO OBJETO DE IVA</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL EXENTO DE IVA</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL SIN IMPUESTOS</td>\n");
    html.append("            <td class=\"left-align\">").append(String.format("%.2f", invoice.getInvoiceSubtotal()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">TOTAL DESCUENTO</td>\n");
    html.append("            <td class=\"left-align\">").append(String.format("%.2f", invoice.getInvoiceDiscount()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">ICE</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">IVA 15%</td>\n");
    html.append("            <td class=\"left-align\">")
        .append(String.format("%.2f", invoice.getInvoiceTotal().subtract(invoice.getInvoiceSubtotal()).doubleValue()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align bold\">VALOR TOTAL</td>\n");
    html.append("            <td class=\"left-align bold\">").append(String.format("%.2f", invoice.getInvoiceTotal()))
        .append("</td>\n");
    html.append("          </tr>\n");
    html.append("        </table>\n");
    html.append("      </div>\n");

    html.append("    </div>\n");

    html.append("  </div>\n");

    html.append("</div>\n");
    html.append("</body>\n");
    html.append("</html>");

    return html.toString();
  }

  public String generateTicketComprobanteHtml(InvoiceModel invoice, FacturaSRIDTO facturaDto,
      String fechaAutorizacion) throws Exception {

    // Get enterprise info
    Optional<EnterpriseModel> enterpriseOpt = enterpriseService.getEnterpriseByRuc(facturaDto.rucEmpresa);
    EnterpriseModel enterprise = enterpriseOpt.orElseThrow(() -> new ResourceNotFoundException("Enterprise not found"));

    // Get client info
    // ClientModel client =
    // clientService.getClientById(invoice.getClientId().getId());

    String styles = """
                          @page {
                size: auto;
        margin: 10mm;
              }

                  body {
                    font-family: Arial, sans-serif;
                    margin: 0;
                    font-size: 12px;
                    padding: 0;
                  }

                  .header {
                    width: 100%;
                    display: block;
                    align-items: center;
                    font-size: 12px;
                  }

                  .valor-square-info {
                    overflow: hidden;
                  }

                  .valor-square-info div:first-child {
                    float: left;
                  }

                  .valor-square-info div:last-child {
                    float: right;
                  }

                  .ahorro-square-info {
                    overflow: hidden;
                    margin-top: 3px;
                  }

                  .ahorro-square-info div:first-child {
                    float: left;
                    width: 70%;
                  }

                  .ahorro-square-info div:last-child {
                    float: right;
                    width: 30%;
                    text-align: right;
                  }

                  th,
                  td {
                    padding: 3px;
                    text-align: left;
                    font-size: 10px;
                  }

                  th {
                    font-weight: 700;
                    font-size: 12px;
                  }

                  .right-align {
                    text-align: right;
                  }

                  .center-align {
                    text-align: center;
                  }

                  .left-align {
                    text-align: left;
                  }

                  .totals {
                    width: 40%;
                    float: left;
                  }

                  .totals table td {
                    border-collapse: collapse;
                  }

                  .bold {
                    font-weight: 700;
                  }

                  .center {
                    text-align: center;
                  }

                  .small-note {
                    font-size: 8px;
                    color: #555;
                    margin-top: 10px;
                  }

                  .access-key-info {
                    text-align: center;
                  }

                  .access-key {
                    width: 30%;
                    display: inline-block;
                  }

                  .info-factura {
                    overflow: hidden;
                  }

                  .info-factura-no {
                    float: left;
                    margin-right: 1rem;
                    font-size: 15px;
                  }

                  .info-factura-numero {
                    font-size: 15px;
                    float: left;
                  }

                  .ruc-info {
                    overflow: hidden;
                    font-weight: 700;
                    font-size: 15px;
                  }

                  .ruc {
                    float: left;
                    margin-right: 2rem;
                  }

                  .factura-info {
                    font-weight: 700;
                    font-size: 14px;
                  }

                  .fecha-info {
                    overflow: hidden;
                  }

                  .fecha {
                    float: left;
                    margin-right: 1rem;
                  }

                  .ambiente-info {
                    overflow: hidden;
                  }

                  .ambiente {
                    float: left;
                    margin-right: 1rem;
                  }

                  .emision-info {
                    overflow: hidden;
                  }

                  .emision {
                    float: left;
                    margin-right: 1rem;
                  }

                  .direccion-matriz-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .direccion-matriz {
                    float: left;
                    margin-right: 1rem;
                  }

                  .direccion-sucursal-info {
                    overflow: hidden;
                    font-size: 10px;
                  }

                  .direccion-sucursal {
                    float: left;
                    margin-right: 1rem;
                  }

                  .fin-info {
                    font-size: 10px;
                    margin-top: 2%;
                  }

                  .nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .nombres {
                    float: left;
                    margin-right: 3rem;
                    font-weight: bold;
                  }

                  .identificacion-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .identificacion {
                    float: left;
                    margin-right: 2rem;
                  }

                  .fecha-nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .fecha-nombres {
                    float: left;
                    margin-right: 2rem;
                  }

                  .direccion-nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .direccion-nombres {
                    float: left;
                    margin-right: 2rem;
                  }

                  .resumen-factura {
                    width: 100%;
                    overflow: hidden;
                  }

                  .forma-pago {
                    margin-top: 3%;
                    width: 58%;
                    float: left;
                  }

                  .clear {
                    clear: both;
                  }

                  .numero-autorizacion {
                    font-size: 10px;
                  }

                  .invoice {
                    margin: 0;
                    padding: 0;
                    width: 100%
                  }

                  .nombreempresa{
                    font-size: 18px;
                  }
                          """;
    StringBuilder html = new StringBuilder();
    html.append(
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
    html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"es\">\n");
    html.append("<head>\n");
    html.append("  <meta charset=\"utf-8\" />\n");
    html.append("  <title>Ticket</title>\n");
    html.append("  <style>\n").append(styles).append("  </style>\n");
    html.append("</head>\n");
    html.append("<body>\n");
    html.append("<div class=\"invoice\">\n");

    // HEADER
    html.append("  <!-- HEADER -->\n");
    html.append("  <div class=\"header\" style=\"height: 100%;\">\n");
    html.append("  \n");
    html.append("        <div class=\"nombreempresa\"  style=\"margin-top: 3%;\">")
        .append(HtmlUtils.htmlEscape(enterprise.getEnterpriseName())).append("</div>\n");

    html.append("        <div class=\"factura-info\"><strong>COMPROBANTE N# </strong>\"").append(invoice.getId())
        .append("</div>\n");
    html.append("        <div class=\"factura-info\"><strong>*** GRACIAS POR SU COMPRA ***</strong></div>\n");
    html.append("        <div class=\"factura-info\"><strong>DOCUMENTO SIN VALOR TRIBUTARIO</strong></div>\n");
    html.append("  </div>\n");
    html.append("  <hr style=\"border: 1px solid #000; margin: 10px 0;\"/>\n");
    // INFORMACIÓN DEL CLIENTE
    html.append("  <div>\n");

    html.append("      <div class=\"fecha-nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("        <div class=\"fecha-nombres\"><strong>Fecha:</strong></div>\n");
    html.append("        <div>")
        .append(invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        .append("</div>\n");
    html.append("      </div>\n");
    html.append("        <div class=\"factura-info\"><strong>*** COMPROBANTE DE VENTA ***</strong></div>\n");
    html.append("  </div>\n");
    html.append("  <hr style=\"border: 1px solid #000; margin: 10px 0;\"/>\n");

    // DETALLE
    html.append("    <div>\n");
    html.append("      <table>\n");
    html.append("        <thead>\n");
    html.append("        <tr>\n");
    html.append("          <th>Descrip</th>\n");
    html.append("          <th>Cant</th>\n");
    html.append("          <th>P.Unit</th>\n");
    html.append("          <th>P.Total</th>\n");
    html.append("        </tr>\n");
    html.append("        </thead>\n");
    html.append("        <tbody>\n");

    // Add invoice details
    if (facturaDto.getDetalles() != null && facturaDto.getDetalles().getDetalle() != null) {
      facturaDto.getDetalles().getDetalle().forEach(detalle -> {
        html.append("          <tr>\n");
        html.append("            <td>").append(HtmlUtils.htmlEscape(detalle.getDescripcion())).append("</td>\n");
        html.append("            <td >").append(detalle.getCantidad().intValue()).append("</td>\n");
        html.append("            <td>").append(String.format("%.2f", detalle.getPrecioUnitario())).append("</td>\n");
        html.append("            <td >").append(String.format("%.2f", detalle.getPrecioTotalSinImpuesto()))
            .append("</td>\n");
        html.append("          </tr>\n");
      });
    }

    html.append("        </tbody>\n");
    html.append("      </table>\n");
    html.append("    </div>\n");

    // RESUMEN
    html.append("    <div class=\"resumen-factura\">\n");

    html.append("      <div class=\"totals\">\n");
    html.append("        <table>\n");
    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL 15%</td>\n");
    html.append("            <td class=\"left-align bold\">")
        .append(String.format("%.2f", invoice.getInvoiceSubtotal())).append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL NO OBJETO DE IVA</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL EXENTO DE IVA</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL SIN IMPUESTOS</td>\n");
    html.append("            <td class=\"left-align\">").append(String.format("%.2f", invoice.getInvoiceSubtotal()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">TOTAL DESCUENTO</td>\n");
    html.append("            <td class=\"left-align\">").append(String.format("%.2f", invoice.getInvoiceDiscount()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">ICE</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">IVA 15%</td>\n");
    html.append("            <td class=\"left-align\">")
        .append(String.format("%.2f", invoice.getInvoiceTotal().subtract(invoice.getInvoiceSubtotal()).doubleValue()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align bold\">VALOR TOTAL</td>\n");
    html.append("            <td class=\"left-align bold\">").append(String.format("%.2f", invoice.getInvoiceTotal()))
        .append("</td>\n");
    html.append("          </tr>\n");
    html.append("        </table>\n");
    html.append("      </div>\n");

    html.append("    </div>\n");

    html.append("  </div>\n");
    html.append("</body>\n");
    html.append("</html>");

    // System.out.println(html.toString());

    return html.toString();
  }

  public String re_generateTicketComprobanteHtml(InvoiceHeaderDTO invoice) throws Exception {

    String styles = """
                        @page {
                size: auto;
        margin: 10mm;
              }

                  body {
                    font-family: Arial, sans-serif;
                    margin: 0;
                    font-size: 12px;
                    padding: 0;
                  }

                  .header {
                    width: 100%;
                    display: block;
                    align-items: center;
                    font-size: 12px;
                  }

                  .valor-square-info {
                    overflow: hidden;
                  }

                  .valor-square-info div:first-child {
                    float: left;
                  }

                  .valor-square-info div:last-child {
                    float: right;
                  }

                  .ahorro-square-info {
                    overflow: hidden;
                    margin-top: 3px;
                  }

                  .ahorro-square-info div:first-child {
                    float: left;
                    width: 70%;
                  }

                  .ahorro-square-info div:last-child {
                    float: right;
                    width: 30%;
                    text-align: right;
                  }

                  th,
                  td {
                    padding: 3px;
                    text-align: left;
                    font-size: 10px;
                  }

                  th {
                    font-weight: 700;
                    font-size: 12px;
                  }

                  .right-align {
                    text-align: right;
                  }

                  .center-align {
                    text-align: center;
                  }

                  .left-align {
                    text-align: left;
                  }

                  .totals {
                    width: 40%;
                    float: left;
                  }

                  .totals table td {
                    border-collapse: collapse;
                  }

                  .bold {
                    font-weight: 700;
                  }

                  .center {
                    text-align: center;
                  }

                  .small-note {
                    font-size: 8px;
                    color: #555;
                    margin-top: 10px;
                  }

                  .access-key-info {
                    text-align: center;
                  }

                  .access-key {
                    width: 30%;
                    display: inline-block;
                  }

                  .info-factura {
                    overflow: hidden;
                  }

                  .info-factura-no {
                    float: left;
                    margin-right: 1rem;
                    font-size: 15px;
                  }

                  .info-factura-numero {
                    font-size: 15px;
                    float: left;
                  }

                  .ruc-info {
                    overflow: hidden;
                    font-weight: 700;
                    font-size: 15px;
                  }

                  .ruc {
                    float: left;
                    margin-right: 2rem;
                  }

                  .factura-info {
                    font-weight: 700;
                    font-size: 14px;
                  }

                  .fecha-info {
                    overflow: hidden;
                  }

                  .fecha {
                    float: left;
                    margin-right: 1rem;
                  }

                  .ambiente-info {
                    overflow: hidden;
                  }

                  .ambiente {
                    float: left;
                    margin-right: 1rem;
                  }

                  .emision-info {
                    overflow: hidden;
                  }

                  .emision {
                    float: left;
                    margin-right: 1rem;
                  }

                  .direccion-matriz-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .direccion-matriz {
                    float: left;
                    margin-right: 1rem;
                  }

                  .direccion-sucursal-info {
                    overflow: hidden;
                    font-size: 10px;
                  }

                  .direccion-sucursal {
                    float: left;
                    margin-right: 1rem;
                  }

                  .fin-info {
                    font-size: 10px;
                    margin-top: 2%;
                  }

                  .nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .nombres {
                    float: left;
                    margin-right: 3rem;
                    font-weight: bold;
                  }

                  .identificacion-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .identificacion {
                    float: left;
                    margin-right: 2rem;
                  }

                  .fecha-nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .fecha-nombres {
                    float: left;
                    margin-right: 2rem;
                  }

                  .direccion-nombres-info {
                    overflow: hidden;
                    font-size: 15px;
                  }

                  .direccion-nombres {
                    float: left;
                    margin-right: 2rem;
                  }

                  .resumen-factura {
                    width: 100%;
                    overflow: hidden;
                  }

                  .forma-pago {
                    margin-top: 3%;
                    width: 58%;
                    float: left;
                  }

                  .clear {
                    clear: both;
                  }

                  .numero-autorizacion {
                    font-size: 10px;
                  }

                  .invoice {
                    margin: 0;
                    padding: 0;
                    width: 100%
                  }

                  .nombreempresa{
                    font-size: 18px;
                  }
                        """;
    StringBuilder html = new StringBuilder();
    html.append(
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
    html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"es\">\n");
    html.append("<head>\n");
    html.append("  <meta charset=\"utf-8\" />\n");
    html.append("  <title>Ticket</title>\n");
    html.append("  <style>\n").append(styles).append("  </style>\n");
    html.append("</head>\n");
    html.append("<body>\n");
    html.append("<div class=\"invoice\">\n");

    // HEADER
    html.append("  <!-- HEADER -->\n");
    html.append("  <div class=\"header\" style=\"height: 100%;\">\n");
    html.append("  \n");
    html.append("        <div class=\"nombreempresa\"  style=\"margin-top: 3%;\">")
        .append(HtmlUtils.htmlEscape(invoice.getEnterpriseName())).append("</div>\n");

    html.append("        <div class=\"factura-info\"><strong>COMPROBANTE N# </strong>\"").append(invoice.getId())
        .append("</div>\n");
    html.append("        <div class=\"factura-info\"><strong>*** GRACIAS POR SU COMPRA ***</strong></div>\n");
    html.append("        <div class=\"factura-info\"><strong>DOCUMENTO SIN VALOR TRIBUTARIO</strong></div>\n");
    html.append("  </div>\n");
    html.append("  <hr style=\"border: 1px solid #000; margin: 10px 0;\"/>\n");
    // INFORMACIÓN DEL CLIENTE
    html.append("  <div>\n");

    html.append("      <div class=\"fecha-nombres-info\" style=\"margin-top:1%;\">\n");
    html.append("        <div class=\"fecha-nombres\"><strong>Fecha:</strong></div>\n");
    html.append("        <div>")
        .append(invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
        .append("</div>\n");
    html.append("      </div>\n");
    html.append("        <div class=\"factura-info\"><strong>*** COMPROBANTE DE VENTA ***</strong></div>\n");
    html.append("  </div>\n");
    html.append("  <hr style=\"border: 1px solid #000; margin: 10px 0;\"/>\n");

    // DETALLE
    html.append("    <div>\n");
    html.append("      <table>\n");
    html.append("        <thead>\n");
    html.append("        <tr>\n");
    html.append("          <th>Descrip</th>\n");
    html.append("          <th>Cant</th>\n");
    html.append("          <th>P.Unit</th>\n");
    html.append("          <th>P.Total</th>\n");
    html.append("        </tr>\n");
    html.append("        </thead>\n");
    html.append("        <tbody>\n");

    // Add invoice details
    if (invoice.getDetalles() != null) {
      invoice.getDetalles().forEach(detalle -> {
        html.append("          <tr>\n");
        html.append("            <td>").append(HtmlUtils.htmlEscape(detalle.getDescripcion())).append("</td>\n");
        html.append("            <td >").append(detalle.getCantidad().intValue()).append("</td>\n");
        html.append("            <td>").append(String.format("%.2f", detalle.getPrecioUnitario())).append("</td>\n");
        html.append("            <td >")
            .append(String.format("%.2f", detalle.getPrecioTotalSinImpuestoalueWithoutTax()))
            .append("</td>\n");
        html.append("          </tr>\n");
      });
    }

    html.append("        </tbody>\n");
    html.append("      </table>\n");
    html.append("    </div>\n");

    // RESUMEN
    html.append("    <div class=\"resumen-factura\">\n");

    html.append("      <div class=\"totals\">\n");
    html.append("        <table>\n");
    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL 15%</td>\n");
    html.append("            <td class=\"left-align bold\">")
        .append(String.format("%.2f", invoice.getInvoiceSubtotal())).append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL NO OBJETO DE IVA</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL EXENTO DE IVA</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">SUBTOTAL SIN IMPUESTOS</td>\n");
    html.append("            <td class=\"left-align\">").append(String.format("%.2f", invoice.getInvoiceSubtotal()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">TOTAL DESCUENTO</td>\n");
    html.append("            <td class=\"left-align\">").append(String.format("%.2f", invoice.getInvoiceDiscount()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">ICE</td>\n");
    html.append("            <td class=\"left-align\">0.00</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align small\">IVA 15%</td>\n");
    html.append("            <td class=\"left-align\">")
        .append(String.format("%.2f", invoice.getInvoiceTotal().subtract(invoice.getInvoiceSubtotal()).doubleValue()))
        .append("</td>\n");
    html.append("          </tr>\n");

    html.append("          <tr>\n");
    html.append("            <td class=\"left-align bold\">VALOR TOTAL</td>\n");
    html.append("            <td class=\"left-align bold\">").append(String.format("%.2f", invoice.getInvoiceTotal()))
        .append("</td>\n");
    html.append("          </tr>\n");
    html.append("        </table>\n");
    html.append("      </div>\n");

    html.append("    </div>\n");

    html.append("  </div>\n");
    html.append("</body>\n");
    html.append("</html>");

    // System.out.println(html.toString());

    return html.toString();
  }

  public byte[] exportReport(PdfDocumentDTO dto) throws Exception {
    // 1. Cargar el reporte
    InputStream reportStream = getClass().getResourceAsStream("/reports/FacturaGessa.jrxml");
    JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

    // 2. Preparar los parámetros (Las listas internas van aquí)
    Map<String, Object> parameters = new HashMap<>();

    // Convertimos las listas a JRBeanCollectionDataSource para que Jasper las
    // entienda
    parameters.put("datosFactura", new JRBeanCollectionDataSource(dto.getDatosFactura()));
    parameters.put("detalleFactura", new JRBeanCollectionDataSource(dto.getDetalleDTOS()));
    parameters.put("formaPago", new JRBeanCollectionDataSource(dto.getFormaPagoDTOS()));

    // 3. El DTO principal se pasa como una colección de un solo elemento
    // Esto permite que campos como "nombrePrincipal" o "ruc" funcionen como Fields
    // ($F{...})
    List<PdfDocumentDTO> dataSourceList = Collections.singletonList(dto);
    JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataSourceList);

    // 4. Llenar el reporte
    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

    // 5. Exportar a PDF
    return JasperExportManager.exportReportToPdf(jasperPrint);
  }
}
