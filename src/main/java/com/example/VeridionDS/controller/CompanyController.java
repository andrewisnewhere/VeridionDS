package com.example.VeridionDS.controller;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.service.CompanyService;
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
    private final CompanyService companyService;

    @GetMapping("/search")
    public ResponseEntity<Company> searchByDomainOrPhone(@RequestParam String domainOrPhone) {
        Optional<Company> company = companyService.getCompanyByDomainOrPhoneNumber(domainOrPhone);
        if (company.isPresent()) {
            return ResponseEntity.ok(company.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companies = (List<Company>) companyService.getCompanies();
        return new ResponseEntity<>(companies, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable int id) {
        Optional<Company> company = companyService.getCompanyById(id);
        return company.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        Company savedCompany = companyService.insertCompany(company);
        return new ResponseEntity<>(savedCompany, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable int id, @RequestBody Company companyDetails) {
        Optional<Company> company = companyService.getCompanyById(id);

        if (company.isPresent()) {
            Company updatedCompany = company.get();
            updatedCompany.setDomain(companyDetails.getDomain());
            // ... (and other fields to be updated)

            companyService.saveCompany(updatedCompany);
            return new ResponseEntity<>(updatedCompany, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable int id) {
        companyService.deleteCompanyById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/findAll")
    public Iterable<Company> forTestingFindAll() {
        return companyService.getCompanies();
    }

    @DeleteMapping("/deleteAll")
    public void forTestingDeleteAll() {
        companyService.deleteAll();
    }

    @PostMapping("/insert")
    public Company forTestingInsertCompany(@RequestBody Company company) {
        return companyService.insertCompany(company);
    }
}


