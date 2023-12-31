package com.example.VeridionDS.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
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
        boolean hasBeenVisited = urlService.hasUrlBeenVisited(website);
        log.info("Received website from RabbitMQ {}, which has been been visited: {}", website, hasBeenVisited);
        try {
            if (!hasBeenVisited) {
                webCrawlerService.handleWebsite(website);
            }
        } catch (Exception e) {
            log.error("Error handling website: {}", website, e);

            // this will send the message to the dead-letter queue
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}