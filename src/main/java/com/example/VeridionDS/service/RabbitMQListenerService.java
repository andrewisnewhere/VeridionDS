package com.example.VeridionDS.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class RabbitMQListenerService {
    private final WebCrawlerService webCrawlerService;
    private URLService urlService;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void listenForTask(final String website) {
        log.info("Just a test8 {}", DateTime.now());
        log.info("Received website from RabbitMQ: {}", website);
        try {
            if(urlService.shouldProcessURL(website)) {
                webCrawlerService.handleWebsite(website);
                urlService.markAsProcessed(website);
            }
        } catch (Exception e) {
            log.error("Error handling website: {}", website, e);
        } finally {
            log.info("Finally {}", DateTime.now());
        }
    }
}