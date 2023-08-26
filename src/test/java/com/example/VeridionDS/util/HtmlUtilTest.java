package com.example.VeridionDS.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

class HtmlUtilTest {
  private final String html = "<html><body><a href=\"http://example.com/page1\">Page 1</a>" +
      "<a class=\"btn\" href=\"/page1/\">Page 1</a>" +
      "<a class=\"btn\" href=\"/page1#someFragment/\">Page 1 someFragment</a>" +
      "<a class=\"btn\" href=\"/page2/\">Page 2</a>" +
      "<a href=\"http://www.example.com/page3#someFragment\">Fragment</a>" +
      "<a href=\"#fragment\">Fragment</a>" +
      "<a itemprop=\"telephone\" href=\"tel:4146284474\">(414) 628-4474</a>" +
      "<a class=\"first-child wsite-social-item wsite-social-facebook\" href=\"https://www.facebook.com/zuck/?fref=mentions\"></a>" +
      "<a class=\"wsite-social-item wsite-social-instagram\" href=\"https://www.instagram.com/example/?hl=en\"></a>" +
      "<a class=\"wsite-social-item wsite-social-twitter\" href=\"https://twitter.com/example\"></span></a>" +
      "<a class=\"wsite-social-item wsite-social-linkedin\" href=\"https://www.linkedin.com/company/google/\"></a>" +
      "<a class=\"last-child wsite-social-item wsite-social-mail\" href=\"mailto:examplemail@gmail.com\" alt=\"Mail\"></a>" +
      "<a itemprop=\"telephone\" href=\"tel:4146284474\">(414) 628-4474</a>" +
      "<a itemprop=\"streetAddress\" href=\"https://goo.gl/maps/dEBbEXk7fSPTCRUr9\" target=\"_blank\"><i class=\"fas fa-map-marker\" aria-hidden=\"true\"></i> 230 Bayshore Blvd.<br>San Francisco, CA 94124, United States</a>" +
      "</body></html>";
  private final Document document = Jsoup.parse(html);
  private final String website = "http://example.com";


  @Test
  void extractLinks() {
    LinkedHashSet<String> expectedLinks = new LinkedHashSet<>();
    expectedLinks.add("http://example.com/page1");
    expectedLinks.add("http://example.com/page2");
    expectedLinks.add("http://example.com/page3");

    LinkedHashSet<String> actualLinks = HtmlUtil.extractLinks(document, website);

    assertEquals(expectedLinks, actualLinks);
  }

  @Test
  void isSameDomain() {
  }

  @Test
  void getDocument() {
  }

  @Test
  void extractPhoneNumbers() {
  }

  @Test
  void extractAddresses() {
  }

  @Test
  void extractSocialMediaLinks() {
  }

  @Test
  void fetchContentFromUrl() {
  }

  @Test
  void extractDomainFromURL() {
  }
}