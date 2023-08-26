package com.example.VeridionDS.service;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.repository.CompanyRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CompanyService {
    private final CompanyRepo companyRepo;

    public Iterable<Company> getCompanies() {
        return companyRepo.findAll();
    }

    public Company insertCompany(final Company company) {
        return companyRepo.save(company);
    }

    public void deleteAll() {
        this.companyRepo.deleteAll();
    }

    // If you want to fetch by domain, you can add:
    public Company findByDomain(final String domain) {
        return companyRepo.findByDomain(domain);
    }

    public Optional<Company> getCompanyById(final int id) {
        return companyRepo.findById(id);
    }

    public void saveCompany(final Company updatedCompany) {
        companyRepo.save(updatedCompany);
    }

    public void deleteCompanyById(final int id) {
        companyRepo.deleteById(id);
    }

    public Optional<Company> getCompanyByDomainOrPhoneNumber(final String domainOrPhone) {
        Company byDomain = companyRepo.findByDomain(domainOrPhone);
        if (byDomain != null) {
            return Optional.of(byDomain);
        }

        List<Company> byPhone = companyRepo.findByPhoneNumbersContaining(domainOrPhone);
        if (!byPhone.isEmpty()) {
            return Optional.of(byPhone.get(0)); // Taking the first match if multiple found
        }

        return Optional.empty();
    }
}

