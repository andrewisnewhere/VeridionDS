package com.example.VeridionDS;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.repository.CompanyRepo;
import com.example.VeridionDS.service.CompanyDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CompanyDataServiceTests {

//    @InjectMocks
//    private CompanyDataService companyDataService;
//
//    @Mock
//    private CompanyRepo companyRepo;
//
//    @Mock
//    private ResourceLoader resourceLoader;
//
//    @Mock
//    private Resource resource;
//
//    @BeforeEach
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testStoreInitialData() throws Exception {
//        // Mock CSV content
//        String csvContent = "domain,commercialName,legalName,allNames\n" +
//                "test.com,Test Inc,Test Incorporated,Test Corp";
//        InputStream mockInputStream = new ByteArrayInputStream(csvContent.getBytes());
//
//        when(resourceLoader.getResource(anyString())).thenReturn(resource);
//        when(resource.getInputStream()).thenReturn(mockInputStream);
//        when(companyRepo.findById(anyInt())).thenReturn(Optional.empty());
//
//        companyDataService.storeInitialData();
//
//        verify(companyRepo, times(1)).save(any(Company.class));
//    }
//
//    @Test
//    public void testReadDomainsFromInputStream() {
//        String domains = "domain\n" +
//                "google.com\n" +
//                "test.com";
//
//        InputStream mockInputStream = new ByteArrayInputStream(domains.getBytes());
//        List<String> extractedDomains = companyDataService.readDomainsFromInputStream(mockInputStream);
//
//        assertEquals(2, extractedDomains.size());
//        assertTrue(extractedDomains.contains("http://google.com"));
//        assertTrue(extractedDomains.contains("http://test.com"));
//    }

}
