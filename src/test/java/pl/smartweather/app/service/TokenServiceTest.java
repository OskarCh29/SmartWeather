package pl.smartweather.app.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void clearTokenList() {
        tokenService = new TokenService();
    }

    @Test
    void generateTokenShouldReturnSecurityToken() {
        String token = tokenService.generateToken();
        assertNotNull(token, "Token should not be null");
        assertEquals(36, token.length(), "Token length should be 36 chars as UUID");

    }

    @Test
    void validateTokenShouldNotThrowExceptionTokenValid() throws InterruptedException {
        String token = tokenService.generateToken();

        Thread.sleep(1000);

        assertDoesNotThrow(() -> tokenService.validateToken(token), "Token still valid after init");
    }
    @Test
    void validateTokenShouldThrowExceptionForExpiredToken(){
        String token = UUID.randomUUID().toString();
        Map<String, Instant> tokens = new ConcurrentHashMap<>();
        tokens.put(token,Instant.now().minus(Duration.ofMinutes(1)));

        ReflectionTestUtils.setField(tokenService,"validTokens",tokens);

        assertThrows(SecurityException.class, () -> tokenService.validateToken(token));
    }

    @Test
    void validateTokenShouldThrowExceptionForInvalidToken() {
        String token = "invalid-token";

        assertThrows(SecurityException.class, () -> tokenService.validateToken(token));
    }
}
