package com.example.VeridionDS;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.erhlc.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.client.erhlc.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.VeridionDS.repository")
public class ElasticsearchConfig {

    @Bean
    public RestHighLevelClient client() {
//        HttpHeaders defaultHeaders = new HttpHeaders();
//        defaultHeaders.add("Accept", "application/vnd.elasticsearch+json;compatible-with=7");
//        defaultHeaders.add("Content-Type", "application/vnd.elasticsearch+json;compatible-with=7");
        ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo("elasticsearch-service:9200").build();

        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(client());
    }

}