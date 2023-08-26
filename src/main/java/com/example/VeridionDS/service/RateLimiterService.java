package com.example.VeridionDS.service;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class RateLimiterService {
    private static final long TOKEN_RATE = 10;  // Define how many tokens to add per unit time
    private static final String LUA_RATE_LIMITER_SCRIPT = "local current = tonumber(redis.call('get', KEYS[1]) or '0')" + "\n" + "if current + 1 > tonumber(ARGV[1]) then" + "\n" + "  return 0" + "\n" + "end" + "\n" + "redis.call('incr', KEYS[1])" + "\n" + "redis.call('expire', KEYS[1], ARGV[2])" + "\n" + "return 1";
    private StringRedisTemplate redisTemplate;

    public boolean canSendRequest(String key, int maxRequests, int windowSeconds) {
        List<String> keys = Collections.singletonList(key);
        Long result = redisTemplate.execute(new DefaultRedisScript<>(LUA_RATE_LIMITER_SCRIPT, Long.class), keys, String.valueOf(maxRequests), String.valueOf(windowSeconds));
        return result != null && result == 1;
    }
}
