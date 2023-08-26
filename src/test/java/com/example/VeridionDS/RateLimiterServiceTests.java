package com.example.VeridionDS;

import com.example.VeridionDS.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.*;

public class RateLimiterServiceTests {

//    @InjectMocks
//    private RateLimiterService rateLimiterService;
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
//    public void testCanSendRequest() {
//        String key = "testKey";
//
//        when(redisTemplate.execute(any(), anyList(), anyString(), anyString())).thenReturn(1L);
//
//        boolean canSend = rateLimiterService.canSendRequest(key, 10, 60);
//
//        assert(canSend);
//        verify(redisTemplate, times(1)).execute(any(), anyList(), anyString(), anyString());
//    }

}
