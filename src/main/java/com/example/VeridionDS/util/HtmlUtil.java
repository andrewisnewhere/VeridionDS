package com.example.VeridionDS.util;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HtmlUtil {
    private static final int MAX_PAGES_PER_WEBSITE = 10;
    private static final String PHONE_REGEX = "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b";


    // Method to extract links from HTML content TODO: transactional?
    public static LinkedHashSet<String> extractLinks(final Document document, final String website) {
        LinkedHashSet<String> links = new LinkedHashSet<>();
//    Elements linkElements = document.select("a[href]");
        // select anchor tags with an href attribute that does not start with #.
        Elements linkElements = document.select("a[href]:not([href^=#],[href^=tel],[href^=mailto], [href^=javascript])");

        for (Element linkElement : linkElements) {
            if (links.size() >= MAX_PAGES_PER_WEBSITE) {
                break;
            }
            String link = StringUtils.removeEnd(linkElement.attr("href"), "/"); //remove trailing slash
            link = link.split("#")[0]; //remove fragment from link
            if (link.isBlank()) {
                continue;
            }
            if (link.startsWith("//")) {
                link = StringUtils.removeStart(link, "//");
                link = "http://" + link;
            }
            try {
                if (!isRelativePathUrl(link) && !isSameDomain(website, link)) {
                    continue;
                }

                if (link.startsWith("www.") || link.startsWith("http") || isRelativePathUrl(link)) {
                    String relativePath = isRelativePathUrl(link) ? link : extractRelativePath(link);
                    URL baseUrl = new URL(website);
                    URL resolvedUrl = new URL(baseUrl, relativePath);
                    link = resolvedUrl.toString();

                    if (isSameDomain(website, link)) {
                        links.add(link);
                    }
                }
            } catch (MalformedURLException e) {
                // Ignore malformed URLs
                log.error("Invalid URL: " + link, e);
            }

        }

        return links;
    }

    private static boolean isRelativePathUrl(String link) {
        return link.startsWith("/") || !link.contains("/");
    }

    //Todo check utility
    private static String extractDomain(String href) {
        Pattern urlPattern = Pattern.compile("(https?:\\/\\/)?(www\\.)?(m\\.)?([^\\/]+)");
        Matcher matcher = urlPattern.matcher(href);
        return matcher.group(4);
    }

    private static String extractRelativePath(String url) throws MalformedURLException {
        return (new URL(url)).getPath();
    }

    public static boolean isSameDomain(final String rootUrl, final String url) {
        String rootDomainName = extractDomainFromURL(rootUrl);
        String urlDomainName = extractDomainFromURL(url);
        return rootDomainName.equalsIgnoreCase(urlDomainName);
    }

    public static Document getDocument(final String url) {
        try {
            return Jsoup.connect(url).userAgent("ScrapperBot/1.0 (+https://google.com/ScrapperBot; ScrapperBot@google.com)").get();
        } catch (IOException e) {
            log.info(e.getMessage());
            return null;
        }
    }

    // todo improve phone number saving by adding a normalization
    public static LinkedHashSet<String> extractPhoneNumbers(final Document document) {
        final LinkedHashSet<String> phoneNumbers = new LinkedHashSet<>();

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String text = document.text(); // get all the text from the document

        Iterable<PhoneNumberMatch> matches = phoneUtil.findNumbers(text, "US");

        for (PhoneNumberMatch match : matches) {
            phoneNumbers.add(match.rawString());
            log.debug("***** !!!! PhoneNumberUtil really find a number: " + match);
        }
        //TODO estabilish the need to use the regex verification as it might add some unwanted overhead
//        final Pattern pattern = Pattern.compile(PHONE_REGEX);
//        final Matcher matcher = pattern.matcher(text);
//        while (matcher.find()) {
//            phoneNumbers.add(matcher.group());
//            log.debug("***** !!!! Phone Number found with pattern use: " + matcher.group());
//        }

        //TODO the same as above
        Elements phoneElements = document.select("a[href^=tel]");
        for (Element phoneElement : phoneElements) {
            String phoneNumber = phoneElement.attr("href").replace("tel:", "");
            phoneNumbers.add(phoneNumber);
            log.debug("***** !!!! Phone Number found with phoneElements: " + phoneNumber);
        }

        return phoneNumbers;
    }

    //todo check address from https://top-salon-hair-salon.business.site/#testimonials
    public static LinkedHashSet<String> extractAddresses(final Document document) {
        final LinkedHashSet<String> addresses = new LinkedHashSet<>();

        // 1. Extract addresses using broader CSS selectors
        Elements addressElements = document.select(".address, .addr, .location, [itemprop=address]");
        for (Element addressElement : addressElements) {
            addresses.add(addressElement.text());
        }

        // 2. Refine the regular expression to be more specific
        Pattern addressPattern = Pattern.compile("\\b\\d+\\s[A-Za-z0-9'.\\-\\s,]+(Avenue|Boulevard|Drive|Lane|Road|Street|Ave|Blvd|Rd|St)\\.?\\s*(\\w+\\s*,)?\\s*\\w+\\s*,\\s*[A-Z]{2}\\s*\\d{5}\\b");

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

    //todo check why not linkedin for https://garrettwietholter.com/
    public static LinkedHashSet<String> extractSocialMediaLinks(final Document document) {
        final Set<String> socialMediaDomains = new HashSet<>(Arrays.asList("facebook.com", "twitter.com", "linkedin.com", "instagram.com", "pinterest.com", "snapchat.com", "reddit.com", "tumblr.com", "youtube.com", "whatsapp.com", "telegram.org", "tiktok.com"));
        final LinkedHashSet<String> socialMediaLinks = new LinkedHashSet<>();
        final Set<String> seenBaseUrls = new HashSet<>();

        // select anchor tags with an href attribute that does not start with #.
        final Elements links = document.select("a[href]:not([href^=#])");

        Pattern urlPattern = Pattern.compile("(https?:\\/\\/)?(www\\.)?(m\\.)?([^\\/]+)");

        for (Element link : links) {
            final String href = link.attr("abs:href");

            Matcher matcher = urlPattern.matcher(href);
            if (matcher.find()) {
                String domain = matcher.group(4);

                if (socialMediaDomains.contains(domain)) {
                    if (!seenBaseUrls.contains(domain)) {
                        seenBaseUrls.add(domain);
                        socialMediaLinks.add(href);
                    }
                }
            }
        }
        return socialMediaLinks;
    }

    public static String fetchContentFromUrl(final String targetUrl) throws IOException {
        URL url = new URL(targetUrl);

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // set timeout to 5 seconds
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 404 || responseCode == 403) {
                return null;
            } else if (responseCode == 301) {
                String newUrl = conn.getHeaderField("Location");
                return isSameDomain(targetUrl, newUrl) ? fetchContentFromUrl(newUrl) : null;
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
            throw e; //the site does not exist
        } catch (IOException e) {
            log.error("Attempt to fetch content from URL {} failed", targetUrl, e);
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String extractDomainFromURL(String url) {
        return url.replaceAll("http(s)?://|www\\.|/.*", "");
    }

}