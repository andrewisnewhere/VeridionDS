package com.example.VeridionDS;

import com.example.VeridionDS.service.URLService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.*;

public class URLServiceTests {
//
//    @InjectMocks
//    private URLService urlService;
//
//    @Mock
//    private StringRedisTemplate redisTemplate;
//
//    @BeforeEach
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testShouldProcessURL_NotProcessed() {
//        String url = "http://example.com";
//
//        when(redisTemplate.opsForSet().isMember("visitedUrls", url)).thenReturn(false);
//
//        boolean shouldProcess = urlService.shouldProcessURL(url);
//
//        assert(shouldProcess);
//        verify(redisTemplate.opsForSet(), times(1)).add("visitedUrls", url);
//    }
//
//    @Test
//    public void testMarkAsProcessed() {
//        String url = "http://example.com";
//
//        urlService.markAsProcessed(url);
//
//        verify(redisTemplate.opsForSet(), times(1)).add("visitedUrls", url);
//    }
//
//    @Test
//    public void testMarkUrlAsVisited() {
//        String url = "http://example.com";
//
//        urlService.markUrlAsVisited(url);
//
//        verify(redisTemplate.opsForValue(), times(1)).set(startsWith("visited:"), eq("true"));
//    }
//
//    @Test
//    public void testHasUrlBeenVisited_Visited() {
//        String url = "http://example.com";
//
//        when(redisTemplate.opsForValue().get(startsWith("visited:"))).thenReturn("true");
//
//        boolean hasBeenVisited = urlService.hasUrlBeenVisited(url);
//
//        assert(hasBeenVisited);
//    }
}
