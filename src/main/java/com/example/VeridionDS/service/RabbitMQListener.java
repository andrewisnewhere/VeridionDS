package com.example.VeridionDS.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class RabbitMQListener {
    //    just handles the task of listening
    // to the RabbitMQ queue and then delegates the actual processing
// of the URL to RabbitMQService.
    private final WebCrawlerService webCrawlerService;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void listenForTask(String website) {
        log.info("Received website from RabbitMQ: {}", website);
        try {
            webCrawlerService.handleWebsite(website);
        } catch (Exception e) {
            log.error("Error handling website: {}", website, e);
            // Potentially requeue or handle error in another manner
        }
    }
}