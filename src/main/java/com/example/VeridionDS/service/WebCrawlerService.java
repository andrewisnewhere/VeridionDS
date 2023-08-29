package com.example.VeridionDS.service;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.List;

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
    private final StorageService storageService;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

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
            if (canCrawlWebsite(website)) {
                log.info("crawling website: {}", website);
                crawlWebsite(website);
            } else {
                log.warn("URL has already been visited or can't be crawled: " + website);
            }
        } catch (UnknownHostException e) {
            log.error("This site can’t be reached: " + website);
        } catch (Exception e) {
            log.error("Error crawling website: " + website, e);
        }
    }
    public void crawlWebsite(final String website) throws UnknownHostException {
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
            storageService.storeData(website, phoneNumbers, socialMediaLinks, addresses);
        } catch (UnknownHostException e) {
            log.info("Unknown host encountered");
            throw e;
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    private boolean canCrawlWebsite(final String website) throws UnknownHostException {
        urlService.markUrlAsVisited(website);
        try {
            URL url = new URL(website);
            String robotFileUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";

            String robotFileContent = fetchContentFromWebsite(robotFileUrl);
            if (robotFileContent == null || robotFileContent.isEmpty()) {
                return true; // No robots.txt found, assume that crawling is allowed.
            }

            BaseRobotRules rules = robotParser.parseContent(url.toString(), robotFileContent.getBytes(), "text/plain", "ScrapperBot/1.0 (+https://google.com/ScrapperBot; ScrapperBot@google.com)");
            return rules.isAllowed(url.toString()); // Use the crawler-commons logic to decide if the URL is crawlable.
        } catch (UnknownHostException e) {
            log.info("Unknown host encountered");
            throw e;
        } catch (Exception e) {
            log.error("Error checking robots.txt for website: " + website, e);
            return true;
        }
//        return true; //if the robots.txt check is not enabled
    }

    private static String fetchContentFromWebsite(final String website) throws IOException {
        URL url = new URL(website);

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT); // set timeout to 5 seconds
            conn.setReadTimeout(READ_TIMEOUT);

            int responseCode = conn.getResponseCode();
            if (responseCode == 404 || responseCode == 403) {
                return null;
            } else if (responseCode == 301) {
                String connectionURL = conn.getHeaderField("Location");
                return isSameDomain(website, connectionURL) ? fetchContentFromWebsite(connectionURL) : null;
            } else if (responseCode != 200) {
                throw new IOException("Failed to fetch content. HTTP error code: " + responseCode);
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine).append('\n');  // Add newline for original structure
                }

                return content.toString().trim(); // Trim in case there's an extra newline at the end
            }

        } catch (UnknownHostException e) {
            log.error("The site does not exist");
            throw e; //the site does not exist
        } catch (IOException e) {
            log.error("Attempt to fetch content from URL {} failed", website, e);
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}