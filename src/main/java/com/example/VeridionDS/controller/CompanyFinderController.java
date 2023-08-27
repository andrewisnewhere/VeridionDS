package com.example.VeridionDS.controller;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.service.CompanyFinderService;
import com.example.VeridionDS.util.ValidationUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/companies")
@AllArgsConstructor
public class CompanyFinderController {
    private final CompanyFinderService companyFinderService;

    @GetMapping("/search")
    public ResponseEntity<String> searchByDomainOrPhone(@RequestParam String domainOrPhone) {
        if (!ValidationUtils.isValidDomainOrPhone(domainOrPhone)) {
            return ResponseEntity.badRequest().body("Invalid domain or phone number");
        }
        Optional<Company> company = companyFinderService.getCompanyByDomainOrPhoneNumber(domainOrPhone);
        if (company.isPresent()) {
            return ResponseEntity.ok(company.get().toString());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company not found");
    }
}


