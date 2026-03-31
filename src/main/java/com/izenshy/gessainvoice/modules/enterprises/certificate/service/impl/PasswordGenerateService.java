package com.izenshy.gessainvoice.modules.enterprises.certificate.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Service
public class PasswordGenerateService {

    private final int GCM_IV_LENGTH = 12;
    private static final Logger logger = LoggerFactory.getLogger(PasswordGenerateService.class);
    private final int GCM_TAG_LENGTH = 16;


    @Value("${app.name.algorithm}")
    private String ALGORITHM;
    @Value("${app.name.key}")
    private String masterKey;

    

    private SecretKeySpec deriveUserKey(Long userId) {
        try {
            String userSpecificKey = masterKey + "|" + userId;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(userSpecificKey.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            //throw new RuntimeException("Error generando clave de usuario", e);
            logger.error("Error generando clave de usuario", e);
            //return null;
            throw new ResourceNotFoundException("Error generando clave de usuario: "+ e.getMessage());

        }
    }

    public String encrypt(String plainPassword, Long userId) {
        try {
            SecretKeySpec userKey = deriveUserKey(userId);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, userKey);

            byte[] iv = cipher.getIV();
            byte[] encryptedData = cipher.doFinal(plainPassword.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            // throw new RuntimeException("Error al encriptar contraseña", e);
            logger.error("Error al encriptar contraseña", e);
            //return null;
            throw new ResourceNotFoundException("Error al encriptar contraseña: "+e.getMessage());

        }
    }

    public String decrypt(String encryptedPassword, Long userId) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedPassword);

            SecretKeySpec userKey = deriveUserKey(userId);
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
            byte[] encryptedData = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, userKey, gcmSpec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            //throw new RuntimeException("Error al desencriptar contraseña", e);
            logger.error("Error al desencriptar contraseña", e);
            //return null;
            throw new ResourceNotFoundException("Error al desencriptar contraseña: "+e);

        }
    }
}
