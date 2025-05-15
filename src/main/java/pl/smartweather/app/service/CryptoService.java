package pl.smartweather.app.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CryptoService {
    private static final int DECODE_KEY_LENGTH = 32;
    private static final int VECTOR_IV_LENGTH = 12;
    private static final int AUTHENTICATION_TAG = 128;

    @Value("${security.salt}")
    private String cryptoKey;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] decodeKey = cryptoKey.getBytes();
        if (decodeKey.length != DECODE_KEY_LENGTH) {
            throw new SecurityException("Invalid AES key length. Key must be 32 bytes");
        }
        this.secretKey = new SecretKeySpec(decodeKey, "AES");
    }

    public String encrypt(String text) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[VECTOR_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(AUTHENTICATION_TAG, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new SecurityException("Error occurred while encrypting configuration: " + e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);

            byte[] iv = Arrays.copyOfRange(decoded, 0, VECTOR_IV_LENGTH);

            byte[] cipherText = Arrays.copyOfRange(decoded, VECTOR_IV_LENGTH, decoded.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(AUTHENTICATION_TAG, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SecurityException("Error occurred while decrypting configuration: " + e);
        }
    }
}
