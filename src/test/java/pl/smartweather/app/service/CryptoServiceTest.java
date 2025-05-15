package pl.smartweather.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoServiceTest {

    private CryptoService cryptoService;

    @BeforeEach
    void initTests() {
        cryptoService = new CryptoService();
    }

    @Test
    void shouldThrowExceptionForInvalidKeyLength() {
        String shortKey = "Key-To-Short";
        ReflectionTestUtils.setField(cryptoService, "cryptoKey", shortKey);

        assertThrows(SecurityException.class, cryptoService::init);
    }

    @Test
    void shouldInitializeSecretKeyForAlgorithms() {
        String key = "ThisIs32ByteSecretCodeToTestThis";
        ReflectionTestUtils.setField(cryptoService, "cryptoKey", key);

        cryptoService.init();

        SecretKey secretKey = (SecretKey) ReflectionTestUtils.getField(cryptoService, "secretKey");
        assertNotNull(secretKey);
        assertEquals("AES", secretKey.getAlgorithm());
    }

    @Test
    void shouldEncryptDataAndGiveDifferentResultDueToVi() {
        String cryptoKey = "ThisIs32ByteSecretCodeToTestThis";
        ReflectionTestUtils.setField(cryptoService, "cryptoKey", cryptoKey);

        String userPassword = "Password";
        String theSamePassword = "Password";

        cryptoService.init();

        String encryptedPassword = cryptoService.encrypt(userPassword);
        String encryptedSecondInput = cryptoService.encrypt(theSamePassword);

        assertNotNull(encryptedPassword);
        assertNotNull(encryptedSecondInput);
        assertNotEquals(encryptedPassword, encryptedSecondInput,
                "Due to random vi vector result should be different");
        assertNotEquals(userPassword, encryptedPassword, "Input changed");
        assertTrue(userPassword.length() < encryptedPassword.length());
        System.out.println(encryptedPassword);
    }

    @Test
    void shouldThrowExceptionWhenEncryptingDueToMissingKey() {
        String userPassword = "Password";

        assertThrows(SecurityException.class, () -> cryptoService.encrypt(userPassword));
    }

    @Test
    void shouldDecryptData() {
        String testPlainEncryptedPassword = "5eYrSLvy8PZp5RgKLJ7IWO/2zc3OjRiJpWhqEMdrXso6RyyG";
        String testCryptoKey = "ThisIs32ByteSecretCodeToTestThis";
        ReflectionTestUtils.setField(cryptoService, "cryptoKey", testCryptoKey);
        cryptoService.init();

        String decrypt = cryptoService.decrypt(testPlainEncryptedPassword);

        assertNotNull(decrypt);
        assertEquals("Password", decrypt);
    }

    @Test
    void shouldThrowExceptionDueToInvalidDecryptedInput() {
        String notValidText = "invalidTextToDecrypt";
        String testCryptoKey = "ThisIs32ByteSecretCodeToTestThis";
        ReflectionTestUtils.setField(cryptoService, "cryptoKey", testCryptoKey);
        cryptoService.init();

        assertThrows(SecurityException.class, () -> cryptoService.decrypt(notValidText));


    }

}
