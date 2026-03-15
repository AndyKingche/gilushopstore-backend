package com.izenshy.gessainvoice.sri.signature.service;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.DigitalCertificateModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.service.DigitalCertificateService;
import com.izenshy.gessainvoice.modules.enterprises.certificate.service.EnterpriseService;
import com.izenshy.gessainvoice.modules.enterprises.certificate.service.impl.PasswordGenerateService;
import com.izenshy.gessainvoice.modules.enterprises.emitter.dto.EmitterDTO;
import com.izenshy.gessainvoice.modules.enterprises.emitter.service.EmitterService;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceService;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import com.izenshy.gessainvoice.modules.person.user.service.UserService;
import com.izenshy.gessainvoice.sri.dto.single.InfoTributariaDTO;
import com.izenshy.gessainvoice.sri.invoice.FacturaSRI;
import com.izenshy.gessainvoice.sri.invoice.FacturaSRIDTO;
import com.izenshy.gessainvoice.sri.signature.XAdESBESSignature;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static java.nio.file.Files.readAllBytes;


@Service
public class XmlSignatureService {
    @Autowired
    private EmitterService emitterService;

    @Autowired
    private UserService userService;

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private DigitalCertificateService digitalCertificateService;

    @Autowired
    private PasswordGenerateService passwordGenerateService;

    @Autowired
    private InvoiceService invoiceService;

    public XmlSignatureService(EmitterService emitterService,
                               UserService userService,
                               DigitalCertificateService digitalCertificateService,
                               PasswordGenerateService passwordGenerateService,
                               InvoiceService invoiceService) {
        this.emitterService = emitterService;
        this.userService = userService;
        this.digitalCertificateService = digitalCertificateService;
        this.passwordGenerateService = passwordGenerateService;
        this.invoiceService = invoiceService;
    }

    public byte[] signerInvoice(FacturaSRIDTO facturaDto) throws IOException, CertificateException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, JAXBException {
        String fileName = UUID.randomUUID() + ".xml";
        String outputName = "firmado-" + fileName;
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File inputFile = new File(tempDir, fileName);

        Long enterpriseId = enterpriseService.getEnterpriseByRuc(facturaDto.rucEmpresa)
                .map(EnterpriseModel::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Usuario no encontrado para RUC: " + facturaDto.rucEmpresa));

        String enterpriseSequential = invoiceService.getLastInvoiceByEnterpriseId(enterpriseId).getSequential();

        int nextSeq = Integer.parseInt(enterpriseSequential) + 1;
        String newSequential = String.format("%09d", nextSeq);

        DigitalCertificateModel cert = digitalCertificateService.getCertificate(
                        enterpriseId, LocalDate.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No hay certificado habilitado o expirado para el usuario"));

        byte[] certificado = cert.getDigCertificate();

        String claveCert = passwordGenerateService.decrypt(cert.getDigCertPassword(), enterpriseId);

            Optional<EmitterDTO> emmiterFactura = emitterService.getEmitterByRucStatus(facturaDto.rucEmpresa);

            FacturaSRI nuevaFactura = new FacturaSRI();
            nuevaFactura.setInfoTributaria(new InfoTributariaDTO());


            emmiterFactura.ifPresent(emitterDTO ->{

                        var addInfoTributaria = nuevaFactura.getInfoTributaria();

                        addInfoTributaria.setRazonSocial(emitterDTO.emitterRazonSocial);
                        addInfoTributaria.setAmbiente(emitterDTO.emitterAmbiente);
                        addInfoTributaria.setNombreComercial(emitterDTO.emitterNombreComercial);
                        addInfoTributaria.setRuc(emitterDTO.emitterRuc);
                        addInfoTributaria.setClaveAcceso(calcularClaveAcceso
                                (emitterDTO.emitterRuc,
                                        emitterDTO.emitterAmbiente,
                                        emitterDTO.emitterCodEstb,
                                        emitterDTO.emitterPtoEmision,
                                        newSequential));
                        addInfoTributaria.setCodDoc("01");
                        addInfoTributaria.setEstab(emitterDTO.emitterCodEstb);
                        addInfoTributaria.setPtoEmi(emitterDTO.emitterPtoEmision);
                        addInfoTributaria.setSecuencial(newSequential);
                        addInfoTributaria.setDirMatriz(emitterDTO.emitterDirMatriz);

                        nuevaFactura.setInfoFactura(facturaDto.infoFactura);
                        nuevaFactura.setDetalles(facturaDto.detalles);

                    }
            );

            JAXBContext context = JAXBContext.newInstance(FacturaSRI.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            StringWriter sw = new StringWriter();
            marshaller.marshal(nuevaFactura, sw);
            String xmlFactura = sw.toString();

            try (FileWriter writer = new FileWriter(inputFile)) {
                writer.write(xmlFactura);
            }

        if(digitalCertificateService.estaCertificadoVigente(certificado, claveCert)) {
            XAdESBESSignature firma = new XAdESBESSignature(
                    inputFile.getAbsolutePath(),
                    outputName,
                    tempDir.getAbsolutePath()
            );
            firma.setCertBytes(certificado);
            firma.setCertPassword(claveCert);
            System.out.println("Firmando XML con certificado: " + certificado.length + " bytes");
            firma.execute();

            File firmado = new File(tempDir, outputName);
            return readAllBytes(firmado.toPath());
        }
        System.out.println("Certificado no vigente");
        return readAllBytes(inputFile.toPath());
    }

    public byte[] signerInvoiceDeluxe(FacturaSRIDTO facturaDto, String pointOutlet) throws IOException, CertificateException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, JAXBException {
        String fileName = UUID.randomUUID() + ".xml";
        String outputName = "firmado-" + fileName;
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File inputFile = new File(tempDir, fileName);

        Long enterpriseId = enterpriseService.getEnterpriseByRuc(facturaDto.rucEmpresa)
                .map(EnterpriseModel::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Usuario no encontrado para RUC: " + facturaDto.rucEmpresa));

        String enterpriseSequential = invoiceService.getLastInvoiceByEnterpriseIdandFactura(enterpriseId, pointOutlet ).getSequential();

        int nextSeq = Integer.parseInt(enterpriseSequential) + 1;
        String newSequential = String.format("%09d", nextSeq);

        DigitalCertificateModel cert = digitalCertificateService.getCertificate(
                        enterpriseId, LocalDate.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No hay certificado habilitado o expirado para el usuario"));

        byte[] certificado = cert.getDigCertificate();

        String claveCert = passwordGenerateService.decrypt(cert.getDigCertPassword(), enterpriseId);

        Optional<EmitterDTO> emmiterFactura = emitterService.getEmitterByRucStatus(facturaDto.rucEmpresa);

        FacturaSRI nuevaFactura = new FacturaSRI();
        nuevaFactura.setInfoTributaria(new InfoTributariaDTO());


        emmiterFactura.ifPresent(emitterDTO ->{

                    var addInfoTributaria = nuevaFactura.getInfoTributaria();

                    addInfoTributaria.setRazonSocial(emitterDTO.emitterRazonSocial);
                    addInfoTributaria.setAmbiente(emitterDTO.emitterAmbiente);
                    addInfoTributaria.setNombreComercial(emitterDTO.emitterNombreComercial);
                    addInfoTributaria.setRuc(emitterDTO.emitterRuc);
                    addInfoTributaria.setClaveAcceso(calcularClaveAcceso
                            (emitterDTO.emitterRuc,
                                    emitterDTO.emitterAmbiente,
                                    emitterDTO.emitterCodEstb,
                                    emitterDTO.emitterPtoEmision,
                                    newSequential));
                    addInfoTributaria.setCodDoc("01");
                    addInfoTributaria.setEstab(emitterDTO.emitterCodEstb);
                    addInfoTributaria.setPtoEmi(emitterDTO.emitterPtoEmision);
                    addInfoTributaria.setSecuencial(newSequential);
                    addInfoTributaria.setDirMatriz(emitterDTO.emitterDirMatriz);

                    nuevaFactura.setInfoFactura(facturaDto.infoFactura);
                    nuevaFactura.setDetalles(facturaDto.detalles);

                }
        );

        JAXBContext context = JAXBContext.newInstance(FacturaSRI.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter sw = new StringWriter();
        marshaller.marshal(nuevaFactura, sw);
        String xmlFactura = sw.toString();

        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write(xmlFactura);
        }

        if(digitalCertificateService.estaCertificadoVigente(certificado, claveCert)) {
            XAdESBESSignature firma = new XAdESBESSignature(
                    inputFile.getAbsolutePath(),
                    outputName,
                    tempDir.getAbsolutePath()
            );
            firma.setCertBytes(certificado);
            firma.setCertPassword(claveCert);
            System.out.println("Firmando XML con certificado: " + certificado.length + " bytes");
            firma.execute();

            File firmado = new File(tempDir, outputName);
            return readAllBytes(firmado.toPath());
        }
        System.out.println("Certificado no vigente");
        return readAllBytes(inputFile.toPath());
    }

    public String calcularClaveAcceso(String rucEmpresa, String ambiente, String codEstb, String ptoEmision, String codSecuensial){
        Date fecha = new Date();
        SimpleDateFormat fechaFormat = new SimpleDateFormat("ddMMyyyy");
        String fechaFactura = fechaFormat.format(fecha); //8

        String tipoComprobante = "01"; //2 digitod "01 Factura"
        String ruc = rucEmpresa;//13 digitos
        // 1 Pruebas 2 //Produccion
        //String ambiente = ambiente; //1 --ya viene de base de datos
        String serie = codEstb + ptoEmision; //digitos
        //String secuencial = "000000009";//9 digitos
        String secuencial = codSecuensial;
        String codNumerico = "32972546"; //8 digitos
        String tipoEmision = "1"; //normal (1) digto

        String claveIncompleta = fechaFactura + tipoComprobante + ruc + ambiente + serie + secuencial + codNumerico + tipoEmision;

        int codigoVerificador = calcularDigitoVerificador(claveIncompleta);

        System.out.println(codigoVerificador);

        return claveIncompleta + codigoVerificador;
    }

    private static int calcularDigitoVerificador(String clave48) {
        int factor = 2;
        int suma = 0;
        for (int i = clave48.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(clave48.charAt(i));
            suma += digito * factor;
            factor++;
            if (factor > 7) factor = 2;
        }

        int modulo = suma % 11;
        int verificador = 11 - modulo;

        if (verificador == 11) return 0;
        if (verificador == 10) return 1;
        return verificador;
    }

}