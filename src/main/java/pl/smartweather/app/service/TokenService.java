package pl.smartweather.app.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
    private final Map<String, Instant> validTokens = new ConcurrentHashMap<>();

    public String generateToken() {
        String token = UUID.randomUUID().toString();
        validTokens.put(token, Instant.now().plus(Duration.ofMinutes(5)));
        return token;
    }
    public void validateToken(String token) {
        Instant expiry = validTokens.get(token);
        if(expiry != null && Instant.now().isBefore(expiry)){
            invalidateToken(token);
        }
        else{
            throw new SecurityException("Token for request is invalid");
        }
    }
    private void invalidateToken(String token){
        validTokens.remove(token);
    }
}
