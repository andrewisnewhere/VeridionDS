package com.example.VeridionDS;

import com.example.VeridionDS.service.CompanyDataService;
import com.example.VeridionDS.service.WebCrawlerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VeridionDsApplication {
    public static void main(String[] args) {
        SpringApplication.run(VeridionDsApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(WebCrawlerService webCrawlerService, CompanyDataService dataService) {
        return args -> {
            dataService.storeInitialData();
            webCrawlerService.crawlWebsitesFromCSV();
        };
    }
}
