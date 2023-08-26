package com.example.VeridionDS.service;

import com.example.VeridionDS.model.Company;
import com.example.VeridionDS.repository.CompanyRepo;
import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
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
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@AllArgsConstructor
@Getter
public class WebCrawlerService {
    private static final int MAX_PAGES_PER_WEBSITE = 10;
    private static final String PHONE_REGEX = "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b";
    private static final int CONCURRENT_THREADS = 10; // Adjust as needed
    private static final SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
    private final int MAX_DEPTH = 2; // Or whatever limit you'd like to set
    private final CompanyDataService companyDataService;
    //    private final ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_THREADS);
    private final CompanyRepo companyRepo;
    //TODO @Transactional/@Async on this set useful/better or not?
//    private Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
    private final ResourceLoader resourceLoader;
    private final RabbitTemplate rabbitTemplate;
    private final URLService urlService;
    // Caching robots.txt content for a specified duration to reduce redundant calls.
    private final Map<String, String> robotTxtCache = new HashMap<>();
    private final long ROBOT_CACHE_EXPIRY_DURATION = TimeUnit.HOURS.toMillis(1); // cache duration of 1 hour
    private final Map<String, Long> robotTxtCacheTimestamps = new HashMap<>();

    private static LinkedHashSet<String> extractUrls(final Document document, final String website) {
        final LinkedHashSet<String> urls = new LinkedHashSet<>();
        final Elements linkElements = document.select("a[href]");
        //TODO change logic to account for all types of a href <a class="design-element-link-part no-decorations internal-link" href="https://timent.com/services">


        for (Element linkElement : linkElements) {
            if (urls.size() >= MAX_PAGES_PER_WEBSITE) {
                break;
            }
            String url = linkElement.attr("abs:href");

            if (isValidHttpUrl(url) && isSameDomain(website, url)) {
                urls.add(url);
            }
        }

        return urls;
    }

    private static boolean isValidHttpUrl(final String url) {
        try {
            new URL(url);
            return url.toLowerCase().startsWith("http");
        } catch (MalformedURLException e) {
            log.error("Invalid URL: " + url, e);
            return false;
        }
    }

    // This method will be triggered by a RabbitMQ listener

    private static boolean isSameDomain(final String rootUrl, final String url) {
        try {
            URI rootUri = new URI(rootUrl);
            URI uri = new URI(url);
            return rootUri.getHost().equalsIgnoreCase(uri.getHost());
        } catch (URISyntaxException e) {
            log.error("Error parsing URI: " + url, e);
            return false;
        }
    }

    public void crawlWebsitesFromCSV() {
        List<String> websites;
        try (InputStream inputStream = resourceLoader.getResource("classpath:static/sample-websites-company-names-test.csv").getInputStream()) {
            websites = companyDataService.readDomainsFromInputStream(inputStream);
        } catch (IOException e) {
            log.error("Error reading from CSV file", e);
            return;
        }

        if (websites == null || websites.isEmpty()) {
            log.error("No websites found in the CSV or the list is null");
            return;
        }

        for (String website : websites) {
            rabbitTemplate.convertAndSend("websiteQueue", website);
        }
        log.info("Finished crawling websites: {}", websites);
    }

    @Transactional
    public void storeData(final String domain, final LinkedHashSet<String> phoneNumbers, final LinkedHashSet<String> socialMediaLinks, final LinkedHashSet<String> addresses) {
        LinkedHashSet<String> phoneNumbersClean = deduplicateData(phoneNumbers);
        LinkedHashSet<String> socialMediaLinksClean = deduplicateData(socialMediaLinks);
        LinkedHashSet<String> addressesClean = deduplicateData(addresses);

        final Company existingCompany = companyRepo.findByDomain(domain);

        if (existingCompany != null) {
            existingCompany.setPhoneNumbers(phoneNumbersClean);
            existingCompany.setSocialMediaLinks(socialMediaLinksClean);
            existingCompany.setAddresses(addressesClean);
            companyRepo.save(existingCompany);
        } else {
            log.warn("Warning: Company not found for domain: {}", domain);

        }
    }

    public void handleWebsite(String website) {
        String domain = extractDomainFromURL(website);
        if (domain == null) {
            log.error("Error extracting domain from website URL: {}", website);
            return;
        }

        try {
            // adaptive rate-limiting
            backOffIfNeeded(website);

            crawlWebsite(domain, MAX_DEPTH);
            log.info("crawling website: {}", domain);
        } catch (Exception e) {
            log.error("Error crawling website: " + domain, e);
        }
    }

    public void crawlWebsite(final String website, final int depth) {
        respectRateLimit();
        String normalizedWebsite = urlService.normalizeUrl(website); // Normalize the URL

        if (depth > MAX_DEPTH || urlService.hasUrlBeenVisited(normalizedWebsite)) {
            return;
        }
        urlService.markUrlAsVisited(normalizedWebsite); // Use URLService to mark the URL as visited

        try {
            final Document initialDocument = Jsoup.connect(website).userAgent("ScrapperBot/1.0 (+https://google.com/ScrapperBot; ScrapperBot@google.com)").get();

            final LinkedHashSet<String> urls = extractUrls(initialDocument, website);

            for (String url : urls) {
                if (!canCrawlWebsite(url)) {
                    log.warn("Crawling not allowed for URL: " + url);
                    continue;
                }
                log.info("Extracting data from {}, link found on {} ", url, website);
                Document document = getDocument(url);
                if (document != null) {
                    final LinkedHashSet<String> phoneNumbers = extractPhoneNumbers(document);
                    final LinkedHashSet<String> socialMediaLinks = extractSocialMediaLinks(document);
                    final LinkedHashSet<String> addresses = extractAddresses(document);

                    storeData(website, phoneNumbers, socialMediaLinks, addresses);
                }
                rabbitTemplate.convertAndSend("websiteQueue", url);
            }

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

    public String extractDomainFromURL(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();

            if (host != null && host.startsWith("www.")) {
                // rest of your logic
                return host.substring(4);
            } else {
                // handle the case where the host is null or doesn't meet your criteria
                if (host != null) {
                    return host;
                } else {
                    log.warn("URL does not have a valid host: " + url);
                    return null;
                }
            }
        } catch (URISyntaxException e) {
            log.error("Error parsing URI to extract domain: " + url, e);
            return null;
        }
    }

    private boolean canCrawlWebsite(final String website) {
        try {
            URL url = new URL(website);
            String robotFileUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";
            String robotFileContent = robotTxtCache.get(website);
            if (robotFileContent == null || (System.currentTimeMillis() - robotTxtCacheTimestamps.getOrDefault(website, 0L)) > ROBOT_CACHE_EXPIRY_DURATION) {
                robotFileContent = fetchContentFromUrl(robotFileUrl);
                robotTxtCache.put(website, robotFileContent);
                robotTxtCacheTimestamps.put(website, System.currentTimeMillis());
            }

            BaseRobotRules rules = robotParser.parseContent(url.toString(), robotFileContent.getBytes(), "text/plain", "ScrapperBot/1.0 (+https://google.com/ScrapperBot; ScrapperBot@google.com)");

            return rules.isAllowed(url.toString());

        } catch (Exception e) {
            log.error("Error checking robots.txt for website: " + website, e);
            return false;
        }
    }

    private String fetchContentFromUrl(final String targetUrl) throws IOException {
        final int MAX_RETRIES = 3;
        int retries = 0;

        while (retries < MAX_RETRIES) {
            try {
                URL url = new URL(targetUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000); // set timeout to 5 seconds
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 404) {
                    return null;
                } else if (responseCode != 200) {
                    throw new IOException("Failed to fetch content. HTTP error code: " + responseCode);
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                conn.disconnect();

                return content.toString();

            } catch (IOException e) {
                log.error("Attempt {} to fetch content from URL {} failed", retries + 1, targetUrl, e);
                retries++;

                if (retries == MAX_RETRIES) {
                    throw e;
                }

                respectRateLimit(); // sleep between retries
            }
        }

        return null; // should not reach here due to the throw in the catch block above
    }

    private Document getDocument(final String url) {
        try {
            return Jsoup.connect(url).userAgent("ScrapperBot/1.0 (+https://google.com/ScrapperBot; ScrapperBot@google.com)").get();

        } catch (IOException e) {
            log.info(e.getMessage());
            return null;
        }
    }

    private LinkedHashSet<String> extractPhoneNumbers(final Document document) {
        final LinkedHashSet<String> phoneNumbers = new LinkedHashSet<>();

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String text = document.text(); // get all the text from the document

        Iterable<PhoneNumberMatch> matches = phoneUtil.findNumbers(text, null);

        for (PhoneNumberMatch match : matches) {
            phoneNumbers.add(match.rawString());
        }
        //TODO estabilish the need to use the regex verification as it might add some unwanted overhead
        final Pattern pattern = Pattern.compile(PHONE_REGEX);
        final Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            phoneNumbers.add(matcher.group());
        }

        //TODO the same as above
        Elements phoneElements = document.select("a[href^=tel]");
        for (Element phoneElement : phoneElements) {
            String phoneNumber = phoneElement.attr("href").replace("tel:", "");
            phoneNumbers.add(phoneNumber);
        }

        return phoneNumbers;
    }

    private LinkedHashSet<String> extractAddresses(final Document document) {
        final LinkedHashSet<String> addresses = new LinkedHashSet<>();

        // 1. Extract addresses using broader CSS selectors
        Elements addressElements = document.select(".address, .addr, .location, [itemprop=address]");
        for (Element addressElement : addressElements) {
            addresses.add(addressElement.text());
        }

        // 2. Refine the regular expression to be more specific
        Pattern addressPattern = Pattern.compile("\\b\\d+\\s[A-Za-z0-9'.\\-\\s,]+(Avenue|Boulevard|Drive|Lane|Road|Boulevard|Street|Ave|Blvd|Rd|Drive|St)\\.?\\s*(\\w+\\s*,)?\\s*\\w+\\s*,\\s*[A-Z]{2}\\s*\\d{5}\\b");

        Elements allElements = document.getAllElements();
        for (Element element : allElements) {
            String text = element.text();
            Matcher matcher = addressPattern.matcher(text);
            while (matcher.find()) {
                addresses.add(matcher.group());
            }
        }

        // 3. Filter out noisy results
        List<String> unwantedTerms = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
        addresses.removeIf(address -> unwantedTerms.stream().anyMatch(address::contains));

        return addresses;
    }


    private LinkedHashSet<String> extractSocialMediaLinks(final Document document) {
        final LinkedHashSet<String> socialMediaLinks = new LinkedHashSet<>();
        final HashSet<String> seenBaseUrls = new HashSet<>();

        final Elements links = document.select("a[href]");

        // List of popular social media platforms (you can expand this list as needed)
        List<String> socialMediaKeywords = Arrays.asList("facebook.com", "twitter.com", "linkedin.com", "instagram.com", "pinterest.com", "snapchat.com", "reddit.com", "tumblr.com", "youtube.com", "whatsapp.com", "telegram.org", "tiktok.com");

        for (Element link : links) {
            final String href = link.attr("abs:href"); // Use abs:href to get absolute URLs

            for (String keyword : socialMediaKeywords) {
                if (href.contains(keyword)) {
                    // Optional: Filtering based on specific patterns (e.g., ensuring 'profile' is in LinkedIn URLs)
                    if (keyword.equals("linkedin.com") && !href.contains("/in/")) {
                        continue;
                    }

                    // Deduplication: Create a base URL by removing 'www.' and 'm.' prefixes for comparison
                    String baseUrl = href.replace("https://www.", "").replace("https://m.", "");

                    if (!seenBaseUrls.contains(baseUrl)) {
                        seenBaseUrls.add(baseUrl);
                        socialMediaLinks.add(href);
                    }
                    break;  // No need to check other keywords for this link
                }
            }
        }
        return socialMediaLinks;
    }

    private void respectRateLimit() {
        try {
            Thread.sleep(1000); // sleep for 1 second
        } catch (InterruptedException e) {
            log.error("Sleep interrupted", e);
        }
    }

    private LinkedHashSet<String> deduplicateData(final LinkedHashSet<String> data) {
        final LinkedHashSet<String> uniqueData = new LinkedHashSet<>();
        final HashSet<String> seenData = new HashSet<>();

        for (String item : data) {
            if (!seenData.contains(item)) {
                seenData.add(item);
                uniqueData.add(item);
            }
        }
        return uniqueData;
    }
}

