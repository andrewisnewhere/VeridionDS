package com.example.VeridionDS.service;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.repository.CompanyRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CompanyFinderService {
    private final CompanyRepo companyRepo;


    public Optional<Company> getCompanyByDomainOrPhoneNumber(final String domainOrPhone) {
        Company byDomain = companyRepo.findByDomain(domainOrPhone);
        if (byDomain != null) {
            return Optional.of(byDomain);
        }

        List<Company> byPhone = companyRepo.findByPhoneNumbers(domainOrPhone);
        if (!byPhone.isEmpty()) {
            return Optional.of(byPhone.get(0)); // Taking the first match if multiple found
        }

        return Optional.empty();
    }
}

