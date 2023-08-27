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
    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companies = (List<Company>) companyFinderService.getCompanies();
        return new ResponseEntity<>(companies, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable int id) {
        Optional<Company> company = companyFinderService.getCompanyById(id);
        return company.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        Company savedCompany = companyFinderService.insertCompany(company);
        return new ResponseEntity<>(savedCompany, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable int id, @RequestBody Company companyDetails) {
        Optional<Company> company = companyFinderService.getCompanyById(id);

        if (company.isPresent()) {
            Company updatedCompany = company.get();
            updatedCompany.setDomain(companyDetails.getDomain());
            // ... (and other fields to be updated)

            companyFinderService.saveCompany(updatedCompany);
            return new ResponseEntity<>(updatedCompany, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable int id) {
        companyFinderService.deleteCompanyById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/findAll")
    public Iterable<Company> forTestingFindAll() {
        return companyFinderService.getCompanies();
    }

    @DeleteMapping("/deleteAll")
    public void forTestingDeleteAll() {
        companyFinderService.deleteAll();
    }

    @PostMapping("/insert")
    public Company forTestingInsertCompany(@RequestBody Company company) {
        return companyFinderService.insertCompany(company);
    }
}


