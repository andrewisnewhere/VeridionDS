package com.example.VeridionDS.util;

import static com.example.VeridionDS.util.DomainConverterUtil.formatDomain;

import com.example.VeridionDS.model.Company;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CsvUtil {
  private final ResourceLoader resourceLoader;
  private final String websitesCompanyNamesFilename;
  private final String websitesDomainFilename;

  public CsvUtil(final ResourceLoader resourceLoader,
                 @Value("classpath:/static/${websites.company.names}") final String websitesCompanyNamesFilename,
                 @Value("classpath:/static/${websites.domain}") final String websitesDomainFilename) {
    this.resourceLoader = resourceLoader;
    this.websitesCompanyNamesFilename = websitesCompanyNamesFilename;
    this.websitesDomainFilename = websitesDomainFilename;
  }

  public List<String> readDomainsFromInputStream() {
    Resource resource = resourceLoader.getResource(websitesDomainFilename);
    List<String> websiteDomainList = new ArrayList<>();

    try (InputStreamReader reader = new InputStreamReader(resource.getInputStream());
         CSVReader csvReader = new CSVReader(reader)
    ) {

      csvReader.skip(1); // Skip header

      String[] nextRecord;
      while ((nextRecord = csvReader.readNext()) != null) {
        String domain = nextRecord[0].trim();
        if (!domain.isEmpty()) {
          String formattedDomain = formatDomain(domain);
          websiteDomainList.add(formattedDomain); }
        }
    } catch (IOException | CsvValidationException e) {
      log.error("Error reading domains", e);
    }
    return websiteDomainList;
  }

  public List<Company> readCompaniesFromCSV() {
    Resource resource = resourceLoader.getResource(websitesCompanyNamesFilename);
    try (
            InputStream resourceInputStream = resource.getInputStream();
            InputStreamReader reader = new InputStreamReader(resourceInputStream)
    ) {
      CsvToBean<Company> csvToBean = new CsvToBeanBuilder<Company>(reader)
              .withType(Company.class)
              .withIgnoreLeadingWhiteSpace(true)
              .build();

      return csvToBean.parse();
    } catch (IOException e) {
      log.error("Error reading the CSV file: " + e.getMessage());
      return Collections.emptyList();
    }
  }
}