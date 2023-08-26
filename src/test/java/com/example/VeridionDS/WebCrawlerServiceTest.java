package com.example.VeridionDS;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.repository.CompanyRepo;
import com.example.VeridionDS.service.CompanyDataService;
import com.example.VeridionDS.service.URLService;
import com.example.VeridionDS.service.WebCrawlerService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class WebCrawlerServiceTest {
//
//    @Mock
//    private CompanyDataService companyDataService;
//
//    @Mock
//    private CompanyRepo companyRepo;
//
//    @Mock
//    private ResourceLoader resourceLoader;
//
//    @Mock
//    private RabbitTemplate rabbitTemplate;
//
//    @Mock
//    private URLService urlService;
//
//    @InjectMocks
//    private WebCrawlerService webCrawlerService;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @Test
//    public void testCrawlWebsitesFromCSV() throws IOException {
//        List<String> websites = Arrays.asList("http://example1.com", "http://example2.com");
//        when(companyDataService.readDomainsFromInputStream(any())).thenReturn(websites);
//
//        webCrawlerService.crawlWebsitesFromCSV();
//
//        websites.forEach(website -> verify(rabbitTemplate).convertAndSend("websiteQueue", website));
//    }
//
//    @Test
//    public void testHandleWebsiteWithInvalidUrl() {
//        webCrawlerService.handleWebsite("invalidUrl");
//        verify(urlService, never()).normalizeUrl(anyString());
//    }
//
//    @Test
//    public void testHandleWebsiteWithValidUrl() {
//        String validUrl = "http://valid.com";
//        when(urlService.normalizeUrl(validUrl)).thenReturn(validUrl);
//
//        webCrawlerService.handleWebsite(validUrl);
//
//        verify(urlService).normalizeUrl(validUrl);
//    }
//    @Test
//    public void testStoreDataWithExistingCompany() {
//        String domain = "valid.com";
//        Company mockCompany = mock(Company.class);
//        when(companyRepo.findByDomain(domain)).thenReturn(mockCompany);
//
//        LinkedHashSet<String> dummyData = new LinkedHashSet<>();
//        webCrawlerService.storeData(domain, dummyData, dummyData, dummyData);
//
//        verify(companyRepo).save(mockCompany);
//    }

//    @Test
//    public void testStoreDataWithoutExistingCompany() {
//        String domain = "invalid.com";
//        when(companyRepo.findByDomain(domain)).thenReturn(null);
//
//        LinkedHashSet<String> dummyData = new LinkedHashSet<>();
//        webCrawlerService.storeData(domain, dummyData, dummyData, dummyData);
//
//        // Corrected this line to use class reference for static members
//        verify(WebCrawlerService.log, atLeastOnce()).warn(anyString(), eq(domain));
//    }
//@Test
//public void testCrawlDataFailure() {
//    String domain = "failed.com";
//
//    when(webCrawler.crawl(domain)).thenReturn(null); // Assuming that on failure webCrawler.crawl() returns null.
//
//    LinkedHashSet<String> result = webCrawlerService.crawlData(domain);
//
//    assertNull(result); // Expecting a null result due to the failed crawl.
//}

}
