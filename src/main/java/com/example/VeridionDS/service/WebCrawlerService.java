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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.example.VeridionDS.util.HtmlUtil.*;
import static java.util.Objects.isNull;

@Service
@Slf4j
@AllArgsConstructor
@Getter
public class WebCrawlerService {
    private static final int MAX_PAGES_PER_WEBSITE = 10;
    private static final int CONCURRENT_THREADS = 10; // Adjust as needed
    private static final SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
    private final int MAX_DEPTH = 2; // Or whatever limit you'd like to set

    //    private final ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_THREADS);
    private final CompanyRepo companyRepo;
    private final RabbitTemplate rabbitTemplate;
    private final CsvUtil csvUtil;
    // Caching robots.txt content for a specified duration to reduce redundant calls.
    private final Map<String, String> robotTxtCache = new HashMap<>();
    private final long ROBOT_CACHE_EXPIRY_DURATION = TimeUnit.HOURS.toMillis(1); // cache duration of 1 hour
    private final Map<String, Long> robotTxtCacheTimestamps = new HashMap<>();
    //TODO @Transactional/@Async on this set useful/better or not?
    private Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());

//  private static LinkedHashSet<String> extractUrls(final Document document, final String website) {
//    final LinkedHashSet<String> urls = new LinkedHashSet<>();
//    final Elements linkElements = document.select("a[href]");
//    //TODO change logic to account for all types of a href <a class="design-element-link-part no-decorations internal-link" href="https://timent.com/services">
//
//
//    for (Element linkElement : linkElements) {
//      if (urls.size() >= MAX_PAGES_PER_WEBSITE) {
//        break;
//      }
//      String url = linkElement.attr("abs:href");
//
//      if (isValidHttpUrl(url) && isSameDomain(website, url)) {
//        urls.add(url);
//      }
//    }
//
//    return urls;
//  }

    public void crawlWebsitesFromCSV() {
        List<String> websites = csvUtil.readDomainsFromInputStream();
        if (websites == null || websites.isEmpty()) {
            log.error("No websites found in the CSV or the list is null");
            return;
        }

        for (String website : websites) {
            //TODO remove hardcoded websiteQueue
            rabbitTemplate.convertAndSend("websiteQueue", website);
        }
        log.info("Finished crawling websites: {}", websites);
    }

    // This method will be triggered by a RabbitMQ listener
    public void handleWebsite(String website) {
        if (isNull(website) || website.isBlank()) {
            log.error("Received NULL website");
            return;
        }
        try {
//            // adaptive rate-limiting
//            backOffIfNeeded(website);

            if (!canCrawlWebsite(website)) {
                log.warn("Crawling not allowed for URL: " + website);
            } else {
                crawlWebsite(website, MAX_DEPTH);
                log.info("crawling website: {}", website);
            }
        } catch (Exception e) {
            log.error("Error crawling website: " + website, e);
        }
    }

    public void crawlWebsite(final String website, final int depth) {
//        respectRateLimit();
//        String normalizedWebsite = urlService.normalizeUrl(website); // Normalize the URL
//
//        if (depth > MAX_DEPTH || urlService.hasUrlBeenVisited(normalizedWebsite)) {
//            return;
//        }
//        urlService.markUrlAsVisited(normalizedWebsite); // Use URLService to mark the URL as visited
        if (depth > MAX_DEPTH || visitedUrls.contains(website)) {
            return;
        }
        visitedUrls.add(website);

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


                    // Recursive call to crawl linked URLs
                    crawlWebsite(url, depth + 1);
//todo use rabbitTemplate and take into consideration the depth
//                    rabbitTemplate.convertAndSend("websiteQueue", url);
                }
            }
            storeData(website, phoneNumbers, socialMediaLinks, addresses);

            // Pagination handling
            Elements paginationLinks = initialDocument.select("a.pagination-next, a.next-page");
            for (Element link : paginationLinks) {
                String nextPageUrl = link.attr("abs:href");
                if (isSameDomain(website, nextPageUrl)) {
                    crawlWebsite(nextPageUrl, depth + 1);
                }
            }

        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    //Todo check if transaction is necessary and if so refactor to a new class and interface
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
// todo save the content from robots txt in a var and pass it to extractLinks method to take it into consideration
//            String robotFileContent = robotTxtCache.get(website);
//            if (robotFileContent == null || (System.currentTimeMillis() - robotTxtCacheTimestamps.getOrDefault(website, 0L)) > ROBOT_CACHE_EXPIRY_DURATION) {
//                robotFileContent = fetchContentFromUrl(robotFileUrl);
//                robotTxtCache.put(website, robotFileContent);
//                robotTxtCacheTimestamps.put(website, System.currentTimeMillis());
//            }

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

