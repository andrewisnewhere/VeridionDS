package com.example.VeridionDS.service;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.repository.CompanyRepo;
import com.example.VeridionDS.util.CsvUtil;
import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.example.VeridionDS.util.HtmlUtil.*;

@Service
@Slf4j
@AllArgsConstructor
@Getter
public class WebCrawlerService {
    private static final SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
    private final CompanyRepo companyRepo;
    private final RabbitTemplate rabbitTemplate;
    private final CsvUtil csvUtil;
    private final URLService urlService;
    private Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());

    public void crawlWebsitesFromCSV() {
        List<String> websites = csvUtil.readDomainsFromInputStream();
        if (websites == null || websites.isEmpty()) {
            log.error("No websites found in the CSV or the list is null");
            return;
        }

        for (String website : websites) {
            rabbitTemplate.convertAndSend("websiteQueue", website);
        }
    }

    // This method will be triggered by a RabbitMQ listener
    public void handleWebsite(String website) {
        try {
            //TODO decide if it affects performance and should be deleted
//            backOffIfNeeded(website);

            if (!canCrawlWebsite(website)) {
                log.warn("Crawling not allowed for URL: " + website);
            } else {
                crawlWebsite(website);
                log.info("crawling website: {}", website);
            }
        } catch (Exception e) {
            log.error("Error crawling website: " + website, e);
        }
    }

    public void crawlWebsite(final String website) {
        final String normalizedWebsite = urlService.normalizeUrl(website);
        if (visitedUrls.contains(website) || urlService.hasUrlBeenVisited(normalizedWebsite)) {
            //TODO ADDED 2nd condition
            return;
        }

        try {
            final Document initialDocument = Jsoup.connect(website).userAgent("ScrapperBot/1.0 (+https://google.com/ScrapperBot; ScrapperBot@google.com)").get();
            final LinkedHashSet<String> urls = extractLinks(initialDocument, website);
            final LinkedHashSet<String> phoneNumbers = new LinkedHashSet<>();
            final LinkedHashSet<String> socialMediaLinks = new LinkedHashSet<>();
            final LinkedHashSet<String> addresses = new LinkedHashSet<>();

            for (String url : urls) {
                log.info("Extracting data from {}, link found on {} ", url, website);
                Document document = getDocument(url);
                if (document != null) {
                    phoneNumbers.addAll(extractPhoneNumbers(document));
                    socialMediaLinks.addAll(extractSocialMediaLinks(document));
                    addresses.addAll(extractAddresses(document));
                }
            }
            storeData(website, phoneNumbers, socialMediaLinks, addresses);
            //TODO CHANGE PLACE OF THESE 2
            visitedUrls.add(website);
//            urlService.shouldProcessURL(website);
            urlService.markUrlAsVisited(normalizedWebsite);
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

//    //Todo check if transaction is necessary and if so refactor to a new class and interface
    @Transactional
    public void storeData(final String domain, final LinkedHashSet<String> phoneNumbers, final LinkedHashSet<String> socialMediaLinks, final LinkedHashSet<String> addresses) {
        final Company existingCompany = companyRepo.findByDomain(domain);
        if (existingCompany != null) {
            existingCompany.setPhoneNumbers(phoneNumbers);
            existingCompany.setSocialMediaLinks(socialMediaLinks);
            existingCompany.setAddresses(addresses);
            companyRepo.save(existingCompany);
        } else {
            log.warn("Warning: Company not found for domain: {}", domain);
        }
    }
//    @Transactional
//    public void storeData(List<String> domains, List<LinkedHashSet<String>> phoneNumbersList, List<LinkedHashSet<String>> socialMediaLinksList, List<LinkedHashSet<String>> addressesList) {
//        List<Company> companiesToUpdate = new ArrayList<>();
//
//        for (int i = 0; i < domains.size(); i++) {
//            String domain = domains.get(i);
//            LinkedHashSet<String> phoneNumbers = phoneNumbersList.get(i);
//            LinkedHashSet<String> socialMediaLinks = socialMediaLinksList.get(i);
//            LinkedHashSet<String> addresses = addressesList.get(i);
//
//            Company existingCompany = companyRepo.findByDomain(domain);
//            if (existingCompany != null) {
//                existingCompany.setPhoneNumbers(phoneNumbers);
//                existingCompany.setSocialMediaLinks(socialMediaLinks);
//                existingCompany.setAddresses(addresses);
//                companiesToUpdate.add(existingCompany);
//            } else {
//                log.warn("Warning: Company not found for domain: {}", domain);
//            }
//        }
//
//    if (!companiesToUpdate.isEmpty()) {
//        companyRepo.saveAll(companiesToUpdate);
//    }
//}

    public void backOffIfNeeded(final String website) {
        try {
            URL url = new URL(website);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000); // set timeout to 5 seconds

            int responseCode = conn.getResponseCode();

            if (responseCode == 429) {
                int retryAfter = conn.getHeaderFieldInt("Retry-After", 5);
                Thread.sleep(TimeUnit.SECONDS.toMillis(retryAfter)); // Sleep for Retry-After seconds
            }
        } catch (Exception e) {
            log.warn("Could not perform adaptive backoff for website: " + website, e);
        }
    }

    private boolean canCrawlWebsite(final String website) {
        try {
            URL url = new URL(website);
            String robotFileUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";

            String robotFileContent = fetchContentFromUrl(robotFileUrl);
            if (robotFileContent == null || robotFileContent.isEmpty()) {
                return true; // No robots.txt found, assume that crawling is allowed.
            }

            BaseRobotRules rules = robotParser.parseContent(url.toString(), robotFileContent.getBytes(), "text/plain", "ScrapperBot/1.0 (+https://google.com/ScrapperBot; ScrapperBot@google.com)");
            return rules.isAllowed(url.toString()); // Use the crawler-commons logic to decide if the URL is crawlable.
        } catch (Exception e) {
            log.error("Error checking robots.txt for website: " + website, e);
            return false;
        }
    }

}

