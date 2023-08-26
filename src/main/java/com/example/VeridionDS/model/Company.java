package com.example.VeridionDS.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.LinkedHashSet;

@Data
@Builder
@Document(indexName = "companies")
@AllArgsConstructor
@NoArgsConstructor
public class Company {
    @Id
    private int id;
    private String domain;
    private String companyCommercialName;
    private String companyLegalName;
    private String companyAllAvailableNames;
    private LinkedHashSet<String> phoneNumbers;
    private LinkedHashSet<String> socialMediaLinks;
    private LinkedHashSet<String> addresses;
}
