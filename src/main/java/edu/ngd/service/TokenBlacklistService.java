package edu.ngd.service;

import edu.ngd.config.JwtConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_TOKENS_PREFIX = "token:user:";
    
    private final StringRedisTemplate redisTemplate;
    private final JwtConfig jwtConfig;

    public void addToBlacklist(String token, Long userId) {
        String blacklistKey = BLACKLIST_PREFIX + token;
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        
        long expireSeconds = jwtConfig.getAccessTokenExpireMinutes() * 60;
        
        redisTemplate.opsForValue().set(blacklistKey, "true", expireSeconds, TimeUnit.SECONDS);
        
        redisTemplate.opsForSet().add(userTokensKey, token);
        redisTemplate.expire(userTokensKey, expireSeconds, TimeUnit.SECONDS);
        
        log.info("Token added to blacklist: {}, userId: {}", token.substring(0, Math.min(20, token.length())), userId);
    }

    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Redis connection failed in isBlacklisted: {}", e.getMessage());
            return false;
        }
    }

    public void invalidateUserTokens(Long userId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);
        
        if (tokens != null && !tokens.isEmpty()) {
            for (String token : tokens) {
                String blacklistKey = BLACKLIST_PREFIX + token;
                redisTemplate.delete(blacklistKey);
            }
            redisTemplate.delete(userTokensKey);
            log.info("Invalidated {} tokens for user: {}", tokens.size(), userId);
        }
    }
}