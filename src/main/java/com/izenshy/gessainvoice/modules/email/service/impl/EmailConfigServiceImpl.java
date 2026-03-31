package com.izenshy.gessainvoice.modules.email.service.impl;

import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.email.model.EmailConfigModel;
import com.izenshy.gessainvoice.modules.email.repository.EmailConfigRepository;
import com.izenshy.gessainvoice.modules.email.service.EmailConfigService;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailConfigServiceImpl implements EmailConfigService {

    private final EmailConfigRepository repository;
    
    private static final Logger logger = LoggerFactory.getLogger(EmailConfigServiceImpl.class);

    @Autowired
    public EmailConfigServiceImpl(EmailConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public EmailConfigModel getConfigByUser(String email) {
        return repository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de correo no encontrada"));
    }

    @Override
    public void sendEmailWithAttachment(Long enterpriseId, String to, String subject, String body, byte[] pdfBytes) {
        try {
            EmailConfigModel config = getConfigByEnterprise(enterpriseId);
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", config.isTlsEnabled());
            props.put("mail.smtp.ssl.enable", config.isSslEnabled());
            props.put("mail.smtp.host", config.getSmtpHost());
            props.put("mail.smtp.port", config.getSmtpPort());

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getUsername(), config.getPassword());
                }
            });

            //session.setDebug(true);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getUserEmail()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(config.getSubjectEmail());

            //CUERPO DEL EMAIL
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(config.getBodyEmail(), "text/html; charset=UTF-8");

            // Parte del adjunto PDF
            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName("factura.pdf");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);

        } catch (MessagingException e) {
            //throw new RuntimeException("Error al enviar el correo: " + e.getMessage(), e);
            logger.error("Error al enviar correo", e);
            throw new ResourceNotFoundException("Error al enviar el correo: " + e.getMessage());

        }
    }

    @Override
    public void sendEmailWithAttachmentInvoice(Long enterpriseId, String to, byte[] pdfBytes) throws MessagingException {

        try {
            EmailConfigModel config = getConfigByEnterprise(enterpriseId);
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", config.isTlsEnabled());
            props.put("mail.smtp.ssl.enable", config.isSslEnabled());
            props.put("mail.smtp.host", config.getSmtpHost());
            props.put("mail.smtp.port", config.getSmtpPort());

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getUsername(), config.getPassword());
                }
            });

            //session.setDebug(true);



            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getUserEmail()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(config.getSubjectEmail());

            //CUERPO DEL EMAIL
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(config.getBodyEmail(), "text/html; charset=UTF-8");

            // Parte del adjunto PDF
            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName("factura.pdf");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);

        } catch (MessagingException e) {
            //throw new RuntimeException("Error al enviar el correo: " + e.getMessage(), e);
            logger.error("Error al enviar el correo: " + e.getMessage(), e);
            throw new ResourceNotFoundException("Error al enviar el correo: " + e.getMessage());

        }
    }

    private String generarEmailHtml(String body) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head>" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f4f4; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\">" +
                body+
                "</body>" +
                "</html>";
    }

    private String escaparHtmlParaJson(String html) {
        return html
                .replace("\\", "\\\\")  // Escapar backslashes primero
                .replace("\"", "\\\"")   // Escapar comillas dobles
                .replace("\n", "\\n")    // Escapar saltos de línea
                .replace("\r", "\\r")    // Escapar retornos de carro
                .replace("\t", "\\t");   // Escapar tabulaciones
    }

    @Override
    public EmailConfigModel getConfigByEnterprise(Long enterpriseId) {
        return repository.findByEnterpriseId_Id(enterpriseId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de correo no encontrada"));
    }
}
