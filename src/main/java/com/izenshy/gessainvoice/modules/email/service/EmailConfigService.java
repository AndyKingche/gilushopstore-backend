package com.izenshy.gessainvoice.modules.email.service;


import com.izenshy.gessainvoice.modules.email.model.EmailConfigModel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;

public interface EmailConfigService {
    EmailConfigModel getConfigByUser(String email);
    void sendEmailWithAttachment(Long enterpriseId, String to, String subject, String body, byte[] pdfBytes) throws MessagingException;
    void sendEmailWithAttachmentInvoice(Long enterpriseId, String to, byte[] pdfBytes) throws MessagingException;
    EmailConfigModel getConfigByEnterprise(Long enterpriseId);
}
