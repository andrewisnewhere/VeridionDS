package com.example.VeridionDS.model;

import com.example.VeridionDS.util.DomainConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.LinkedHashSet;

@Data
@Builder
@Document(indexName = "companies")
@AllArgsConstructor
@NoArgsConstructor
public class Company {
    @Id
    private String id;
    @CsvCustomBindByName(column = "domain", converter = DomainConverter.class)
    private String domain;
    @CsvBindByName(column = "company_commercial_name")
    private String companyCommercialName;
    @CsvBindByName(column = "company_legal_name")
    private String companyLegalName;
    @CsvBindByName(column = "company_all_available_names")
    private String companyAllAvailableNames;
    private LinkedHashSet<String> phoneNumbers;
    private LinkedHashSet<String> socialMediaLinks;
    private LinkedHashSet<String> addresses;
    //added for optimistic locking purposes
    @Version
    private Long version;
}
