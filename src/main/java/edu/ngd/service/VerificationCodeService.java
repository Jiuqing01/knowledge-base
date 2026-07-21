package edu.ngd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final StringRedisTemplate redisTemplate;
    private static final String CODE_PREFIX = "verification_code:";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 5;

    public void sendCode(String phone) {
        String code = generateCode();
        String key = CODE_PREFIX + phone;
        
        redisTemplate.opsForValue().set(key, code, EXPIRATION_MINUTES, TimeUnit.MINUTES);
        
        log.info("Verification code sent to phone: {}, code: {}", phone, code);
    }

    public boolean verifyCode(String phone, String code) {
        String key = CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            return false;
        }
        
        boolean isValid = storedCode.equals(code);
        
        if (isValid) {
            redisTemplate.delete(key);
        }
        
        return isValid;
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}