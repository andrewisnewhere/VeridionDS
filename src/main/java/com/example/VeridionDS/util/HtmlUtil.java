package com.example.VeridionDS.util;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HtmlUtil {
    private static final int MAX_PAGES_PER_WEBSITE = 10;
    private static final String PHONE_REGEX = "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b";
    private static final Set<String> socialMediaDomains = new HashSet<>(Arrays.asList("facebook.com", "twitter.com", "linkedin.com", "instagram.com", "pinterest.com", "snapchat.com", "reddit.com", "tumblr.com", "youtube.com", "whatsapp.com", "telegram.org", "tiktok.com"));
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("contact", "about", "address", "info", "social", "media", "email", "phone"));

    public static LinkedHashSet<String> extractLinks(final Document document, final String website) {
        LinkedHashSet<String> links = new LinkedHashSet<>();

        // select anchor tags with a href attribute that does not start with #.
        Elements linkElements = document.select("a[href]:not([href^=#],[href^=tel],[href^=mailto], [href^=javascript])");

        List<String> prioritizedLinks = getAndPrioritizeLinks(linkElements);

        for (String link : prioritizedLinks) {
            if (links.size() >= MAX_PAGES_PER_WEBSITE) {
                break;
            }
            link = StringUtils.removeEnd(link, "/"); //remove trailing slash
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

    public static LinkedHashSet<String> extractPhoneNumbers(final Document document) {
        final LinkedHashSet<String> phoneNumbers = new LinkedHashSet<>();

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String text = document.text(); // get all the text from the document

        Iterable<PhoneNumberMatch> matches = phoneUtil.findNumbers(text, "US");

        for (PhoneNumberMatch match : matches) {
            phoneNumbers.add(match.rawString());
        }
        final Pattern pattern = Pattern.compile(PHONE_REGEX);
        final Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            phoneNumbers.add(matcher.group());
        }

        Elements phoneElements = document.select("a[href^=tel]");
        for (Element phoneElement : phoneElements) {
            String phoneNumber = phoneElement.attr("href").replace("tel:", "");
            phoneNumbers.add(phoneNumber);
        }

        return phoneNumbers;
    }

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

        return addresses;
    }

    public static LinkedHashSet<String> extractSocialMediaLinks(final Document document) {
        final LinkedHashSet<String> socialMediaLinks = new LinkedHashSet<>();
        final Set<String> seenBaseUrls = new HashSet<>();

        // select anchor tags with a href attribute that does not start with #, tel, mailto or javascript
        final Elements links = document.select("a[href]:not([href^=#],[href^=tel],[href^=mailto],[href^=javascript])");

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

    public static String extractDomainFromURL(String url) {
        return url.replaceAll("http(s)?://|www\\.|m\\.|/.*", "");
    }

    private static List<String> getAndPrioritizeLinks(Elements linkElements) {
        Set<String> linksSet = linkElements.stream().map(element -> element.attr("href")).collect(Collectors.toSet());

        List<String> links = new ArrayList<>(linksSet);

        Comparator<String> comparator = (link1, link2) -> {
            int score1 = (int) KEYWORDS.stream().filter(link1::contains).count();
            int score2 = (int) KEYWORDS.stream().filter(link2::contains).count();
            return score2 - score1;
        };

        links.sort(comparator);

        return links;
    }

}