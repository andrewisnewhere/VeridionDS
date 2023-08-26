package com.example.VeridionDS.service;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.repository.CompanyRepo;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
@AllArgsConstructor
public class CompanyDataService {
    private CompanyRepo companyRepo;
    private ResourceLoader resourceLoader;

    public void storeInitialData() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:static/sample-websites-company-names-test.csv");
        try (
                InputStream resourceInputStream = resource.getInputStream();
                Reader reader = new InputStreamReader(resourceInputStream);
                CSVReader csvReader = new CSVReader(reader)
        ) {
            // Skipping header
            csvReader.skip(1);

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                Company company = new Company();
                company.setId(getUniqueID());
                company.setDomain(formatDomain(nextRecord[0]));
                company.setCompanyCommercialName(nextRecord[1]);
                company.setCompanyLegalName(nextRecord[2]);
                company.setCompanyAllAvailableNames(nextRecord[3]);
                companyRepo.save(company);
            }
        } catch (IOException e) {
            System.err.println("Error reading the CSV file: " + e.getMessage());
        }
    }

    private int getUniqueID() {
        int id;
        do {
            id = generateRandomId();
        } while (companyRepo.findById(id).isPresent());
        return id;
    }

    public List<String> readDomainsFromInputStream(InputStream inputStream) {
        List<String> domains = new ArrayList<>();
        try (
                Reader reader = new InputStreamReader(inputStream);
                CSVReader csvReader = new CSVReader(reader)
        ) {
            // Skipping header
            csvReader.skip(1);
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                String domain = nextRecord[0]; // Assuming domain is the first field in the CSV
                String formattedDomain = formatDomain(domain);
                if (isValidHttpUrl(formattedDomain)) {
                    domains.add(formattedDomain);
                }
            }
        } catch (IOException | CsvValidationException e) {
            log.error("Error reading domains", e);
        }
        return domains;
    }


    private String formatDomain(String domain) {
        // Add "http://" protocol if missing
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            domain = "http://" + domain;
        }
        return domain;
    }

    private boolean isValidHttpUrl(String url) {
        try {
            URI uri = new URI(url);
            return "http".equals(uri.getScheme()) || "https".equals(uri.getScheme());
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
    }

    private int generateRandomId() {
        Random random = new Random();
        return random.nextInt(1000000);  // Adjust the upper limit as needed
    }
}
