package com.example.VeridionDS.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@AllArgsConstructor
@Slf4j
public class URLService {
    private static final String VISITED_URL_PREFIX = "visited:";
    private StringRedisTemplate redisTemplate;

//    public boolean shouldProcessURL(final String url){
//        final String isVisited = redisTemplate.opsForValue().get(url);
//
//        if(isVisited == null){
//            redisTemplate.opsForValue().set(url, "true");
//            return true;
//        }
//        return false;
//    }

    public boolean shouldProcessURL(String url) {
        if (Boolean.FALSE.equals(redisTemplate.opsForSet().isMember("visitedUrls", url))) {
            redisTemplate.opsForSet().add("visitedUrls", url);
            return true;
        }
        return false;
    }

    public void markAsProcessed(String url) {
        redisTemplate.opsForSet().add("visitedUrls", url);
    }

    public String normalizeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null) {
                scheme = "http";
            }
            if (host == null) {
                // Handle the case where the host is null.
                // You might want to log this and return the original URL,
                // or throw a custom exception.
                log.warn("Host is null for URL: {}", url);
                return url;
            }
            // Removes fragment, ensures path starts with a '/', lowercase scheme/host, etc.
            URI normalized = new URI(scheme.toLowerCase(), uri.getUserInfo(), host.toLowerCase(), uri.getPort(), uri.getPath() != null && !uri.getPath().isEmpty() ? uri.getPath() : "/", uri.getQuery(), null);
            return normalized.toString();
        } catch (URISyntaxException e) {
            // Handle exception - maybe log and return the original URL or throw a custom exception
            log.warn("Normalization of URL failed: {}", e.getMessage());
            return url;
        }
    }


    /**
     * Use Redis to mark this URL as visited.
     */
    public void markUrlAsVisited(String url) {
        String normalizedUrl = normalizeUrl(url);
        redisTemplate.opsForValue().set(VISITED_URL_PREFIX + normalizedUrl, "true");
    }

    /**
     * Check Redis to see if this URL has been visited.
     */
    public boolean hasUrlBeenVisited(String url) {
        String normalizedUrl = normalizeUrl(url);
        return redisTemplate.opsForValue().get(VISITED_URL_PREFIX + normalizedUrl) != null;
    }

}
