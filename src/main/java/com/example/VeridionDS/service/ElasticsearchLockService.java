package com.example.VeridionDS.service;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ElasticsearchLockService {

    private final RestHighLevelClient client;

    public ElasticsearchLockService(RestHighLevelClient client) {
        this.client = client;
    }

    public boolean acquireLock(String lockName) {
        try {
            UpdateRequest updateRequest = new UpdateRequest("locks", lockName).doc("held", true).detectNoop(false).retryOnConflict(0);
            client.update(updateRequest, RequestOptions.DEFAULT);
            return true;
        } catch (ElasticsearchStatusException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                // Document not found, create it
                IndexRequest indexRequest = new IndexRequest("locks").id(lockName).source("held", true);
                try {
                    client.index(indexRequest, RequestOptions.DEFAULT);
                    return true;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void releaseLock(String lockName) {
        try {
            UpdateRequest updateRequest = new UpdateRequest("locks", lockName).doc("held", false).detectNoop(false);
            client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

