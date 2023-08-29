package com.example.VeridionDS.service;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.repository.CompanyRepo;
import com.example.VeridionDS.util.CsvUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class CompanyDataService {
    private CompanyRepo companyRepo;
    private CsvUtil csvUtil;

    public void storeInitialData() {
        List<Company> companies = csvUtil.readCompaniesFromCSV();
        companies.forEach(company -> company.setId(computeId(company)));
        companyRepo.saveAll(companies);
    }

    // Assigns a deterministic ID to each company, so that the same company will always have the same ID.
    private String computeId(Company company) {
        return DigestUtils.md5Hex(company.getDomain());
    }
}

