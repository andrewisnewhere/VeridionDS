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

    //Ensured atomicity for elasticsearch since @Transactional does not work properly
    public boolean markUrlAsVisited(String url) {
        try {
            // Start a Redis transaction
            redisTemplate.multi();

            Long result = redisTemplate.opsForSet().add(VISITED_URLS_SET, url);

            // Execute the transaction
            redisTemplate.exec();

            if (result == null) {
                log.error("Redis error when adding URL to set: {}", url);
                return false;
            }
            return result > 0;
        } catch (Exception e){
            // Log and discard the transaction if there was an exception
            log.error("Error making URL as visited: {}", url, e);
            redisTemplate.discard();
            return false;
        }
    }

    public boolean hasUrlBeenVisited(String url) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(VISITED_URLS_SET, url));
    }
}
