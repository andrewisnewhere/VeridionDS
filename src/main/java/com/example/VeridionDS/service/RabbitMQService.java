package com.example.VeridionDS.service;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RabbitMQService {
    //The RabbitMQService handles the business
    // logic associated with processing a received URL.
    private RabbitTemplate rabbitTemplate;
    private URLService urlService;
    private WebCrawlerService webCrawlerService;

    public void processReceivedURL(String url) {
        if(urlService.shouldProcessURL(url)) {
            processURL(url);
            urlService.markAsProcessed(url);
        }
    }

    private void processURL(String url) {
        // Your logic to crawl and process the URL goes here.
        // For instance, making an HTTP request, parsing the content, storing relevant data, etc.
        if(urlService.shouldProcessURL(url)) {
            webCrawlerService.crawlWebsite(url, webCrawlerService.getMAX_DEPTH());
            urlService.markAsProcessed(url);
        }
    }
}

