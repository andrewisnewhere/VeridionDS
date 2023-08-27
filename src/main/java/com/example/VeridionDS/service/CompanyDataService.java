package com.example.VeridionDS.service;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.repository.CompanyRepo;
import com.example.VeridionDS.util.CsvUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@Slf4j
@AllArgsConstructor
public class CompanyDataService {
    private CompanyRepo companyRepo;
    private CsvUtil csvUtil;

    public void storeInitialData() {
        List<Company> companies = csvUtil.readCompaniesFromCSV();
        for (Company company : companies) {
            company.setId(getUniqueID());
            companyRepo.save(company);
        }
    }

    private int getUniqueID() {
        int id;
        do {
            id = generateRandomId();
        } while (companyRepo.findById(id).isPresent());
        return id;
    }

    private int generateRandomId() {
        Random random = new Random();
        return random.nextInt(1000000);
    }
}
