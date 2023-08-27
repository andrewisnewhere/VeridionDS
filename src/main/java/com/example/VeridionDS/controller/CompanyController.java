package com.example.VeridionDS.controller;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.service.CompanyFinderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/companies")
@AllArgsConstructor
public class CompanyController {
    private final CompanyFinderService companyFinderService;

    @GetMapping("/search")
    public ResponseEntity<Company> searchByDomainOrPhone(@RequestParam String domainOrPhone) {
        Optional<Company> company = companyFinderService.getCompanyByDomainOrPhoneNumber(domainOrPhone);
        if (company.isPresent()) {
            return ResponseEntity.ok(company.get());
        }
        return ResponseEntity.notFound().build();
    }

    //TODO delete all of the below methods

    @GetMapping("/findAll")
    public Iterable<Company> forTestingFindAll() {
        return companyFinderService.getCompanies();
    }

    @DeleteMapping("/deleteAll")
    public void forTestingDeleteAll() {
        companyFinderService.deleteAll();
    }
}


