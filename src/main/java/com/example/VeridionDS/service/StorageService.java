package com.example.VeridionDS.service;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.repository.CompanyRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;

@Service
@AllArgsConstructor
@Slf4j
public class StorageService {
    private final CompanyRepo companyRepo;

        public void storeData(String domain, LinkedHashSet<String> phoneNumbers, LinkedHashSet<String> socialMediaLinks, LinkedHashSet<String> addresses) {
        final Company existingCompany = companyRepo.findByDomain(domain);
        if (existingCompany != null) {
            existingCompany.setPhoneNumbers(phoneNumbers);
            existingCompany.setSocialMediaLinks(socialMediaLinks);
            existingCompany.setAddresses(addresses);

            long version = existingCompany.getVersion();

            existingCompany.setVersion(version + 1);

            companyRepo.save(existingCompany);
        } else {
            log.info("Company not found for domain: {}", domain);
        }
    }
}
