package com.example.VeridionDS.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class URLService {
    private static final String VISITED_URLS_SET = "visitedUrls";
    private StringRedisTemplate redisTemplate;

    public boolean markUrlAsVisited(String url) {
        Long result = redisTemplate.opsForSet().add(VISITED_URLS_SET, url);
        if (result == null) {
            log.error("Redis error when adding URL to set: {}", url);
            return false;
        }
        return result > 0;
    }

    public boolean hasUrlBeenVisited(String url) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(VISITED_URLS_SET, url));
    }
}
