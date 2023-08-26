package com.example.VeridionDS.repository;

import com.example.VeridionDS.model.Company;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepo extends ElasticsearchRepository<Company, Integer> {
    Company findByDomain(String domain);

    List<Company> findByPhoneNumbersContaining(String phoneNumber);
}
