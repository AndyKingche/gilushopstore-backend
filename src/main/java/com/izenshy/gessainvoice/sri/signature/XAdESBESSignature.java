package com.izenshy.gessainvoice.sri.signature;

import es.mityc.firmaJava.libreria.xades.DataToSign;
import es.mityc.firmaJava.libreria.xades.XAdESSchemas;
import es.mityc.javasign.EnumFormatoFirma;
import es.mityc.javasign.xml.refs.InternObjectToSign;
import es.mityc.javasign.xml.refs.ObjectToSign;
import java.io.IOException;

import com.izenshy.gessainvoice.common.exception.BadRequestException;


public class XAdESBESSignature extends GenericXMLSignature{
    private final String fileToSign;
    private final String signatureFileName;
    private final String pathOut;

    public XAdESBESSignature(String fileToSign, String signatureFileName, String pathOut) {
        this.fileToSign = fileToSign;
        this.signatureFileName = signatureFileName;
        this.pathOut = pathOut;
    }

    @Override
    protected DataToSign createDataToSign() {
        DataToSign dataToSign = new DataToSign();
        dataToSign.setXadesFormat(EnumFormatoFirma.XAdES_BES);
        dataToSign.setEsquema(XAdESSchemas.XAdES_132);
        dataToSign.setXMLEncoding("UTF-8");
        dataToSign.setEnveloped(true);
        dataToSign.setParentSignNode("comprobante");
        dataToSign.addObject(new ObjectToSign(
                new InternObjectToSign("comprobante"),
                "contenido comprobante", null, "text/xml", null
        ));

        try {
            dataToSign.setDocument(getDocument(fileToSign));
        } catch (IOException e) {
            throw new BadRequestException("Error leyendo el XML a firmar: "+ e.getMessage());
        }

        return dataToSign;
    }

    @Override
    protected String getSignatureFileName() {
        return signatureFileName;
    }

    @Override
    protected String getPathOut() {
        return pathOut;
    }
}
