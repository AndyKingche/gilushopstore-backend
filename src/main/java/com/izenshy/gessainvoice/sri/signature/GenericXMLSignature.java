package com.izenshy.gessainvoice.sri.signature;

import es.mityc.firmaJava.libreria.xades.DataToSign;
import es.mityc.firmaJava.libreria.xades.FirmaXML;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public abstract class GenericXMLSignature {

    private byte[] certBytes;
    private String certPassword;

    public void setCertBytes(byte[] certBytes) {
        this.certBytes = certBytes;
    }

    public void setCertPassword(String certPassword) {
        this.certPassword = certPassword;
    }

    public void execute() throws IOException, CertificateException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        KeyStore keyStore = loadKeyStore();
        String alias = getAlias(keyStore);

        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
        if (certificate == null) {
            throw new IOException("No existe certificado para firmar.");
        }

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, certPassword.toCharArray());
        Provider provider = keyStore.getProvider();

        DataToSign dataToSign = createDataToSign();
        FirmaXML firma = new FirmaXML();

        Document signedDoc;
        try {
            Object[] result = firma.signFile(certificate, dataToSign, privateKey, provider);
            signedDoc = (Document) result[0];
        } catch (Exception e) {
            throw new IOException("Error realizando la firma: " + e.getMessage(), e);
        }

        saveDocumentToDisk(signedDoc, getPathOut() + File.separator + getSignatureFileName());
    }

    protected abstract DataToSign createDataToSign();
    protected abstract String getSignatureFileName();
    protected abstract String getPathOut();

    protected Document getDocument(String path) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new File(path));
        } catch (Exception e) {
            throw new IOException("Error al leer el XML: " + e.getMessage(), e);
        }
    }

    private KeyStore loadKeyStore() throws IOException, CertificateException {
        try (InputStream is = new ByteArrayInputStream(certBytes)) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(is, certPassword.toCharArray());
            return ks;
        } catch (Exception e) {
            throw new IOException("Error cargando el keystore: " + e.getMessage(), e);
        }
    }

    private String getAlias(KeyStore ks) throws IOException {
        try {
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (ks.isKeyEntry(alias)) return alias;
            }
        } catch (KeyStoreException e) {
            throw new IOException("Error accediendo a alias del KeyStore", e);
        }
        return null;
    }

    private void saveDocumentToDisk(Document doc, String path) throws IOException {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(new File(path)));
        } catch (Exception e) {
            throw new IOException("Error guardando el XML firmado", e);
        }
    }
}

