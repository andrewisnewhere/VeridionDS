package com.example.VeridionDS.util;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

class HtmlUtilTest {
  private final String html = "<html><body><a href=\"http://example.com/page1\">Page 1</a>" +
      "<a class=\"btn\" href=\"/page1/\">Page 1</a>" +
      "<a class=\"btn\" href=\"/page2#someFragment/\">Page 2 someFragment</a>" +
      "<a class=\"btn\" href=\"/page2/\">Page 2</a>" +
      "<a href=\"http://www.example.com/page3#someFragment\">Fragment</a>" +
      "<a href=\"//www.example.com/page4#someFragment\">Page 4</a>" +
      "<a href=\"//maps.google.com/maps/contrib/105585136510441463246\" class=\"X3D5od\">Maps</a>" +
      "<a href=\"//www.someOtherDomain.com/pageExternal#someFragment\">Page External</a>" +
      "<a href=\"//https://www.someOtherDomain.com/pageExternal#someFragment\">Page External</a>" +
      "<a href=\"contact.html\">Contact Page</a>" +
      "<a href=\"#fragment\">Fragment</a>" +
      "<a itemprop=\"telephone\" href=\"tel:4146284474\">(414) 628-4474</a>" +
      "<a class=\"first-child wsite-social-item wsite-social-facebook\" href=\"https://www.facebook.com/zuck/?fref=mentions\"></a>" +
      "<a class=\"wsite-social-item wsite-social-instagram\" href=\"https://www.instagram.com/example/?hl=en\"></a>" +
      "<a class=\"wsite-social-item wsite-social-twitter\" href=\"https://twitter.com/example\"></span></a>" +
      "<a class=\"wsite-social-item wsite-social-linkedin\" href=\"https://www.linkedin.com/company/google/\"></a>" +
      "<a class=\"last-child wsite-social-item wsite-social-mail\" href=\"mailto:examplemail@gmail.com\" alt=\"Mail\"></a>" +
      "<a itemprop=\"telephone\" href=\"tel:4146284474\">(414) 628-4474</a>" +
      "<a target=\"_blank\" id=\"action-list-1\" class=\"dtKbfb  oYxtQd\" href=\"tel:+1-814-954-5812\" itemprop=\"telephone\"<span>Call now</span></a>" +
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
    expectedLinks.add("http://example.com/page4");
    expectedLinks.add("http://example.com/contact.html");

    LinkedHashSet<String> actualLinks = HtmlUtil.extractLinks(document, website);

    assertEquals(expectedLinks, actualLinks);
//    try {
//      assertEquals((new URL("www.domain.com")).toString(), "www.domain.com");
//    } catch (MalformedURLException e) {
//      System.out.println(e);
//    }
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
    LinkedHashSet<String> expectedLinks = new LinkedHashSet<>();
    expectedLinks.add("https://www.facebook.com/zuck/?fref=mentions");
    expectedLinks.add("https://www.instagram.com/example/?hl=en");
    expectedLinks.add("https://twitter.com/example");
    expectedLinks.add("https://www.linkedin.com/company/google/");

    LinkedHashSet<String> actualLinks = HtmlUtil.extractSocialMediaLinks(document);

    assertEquals(expectedLinks, actualLinks);
  }

  @Test
  void fetchContentFromUrl() {
  }

  @Test
  void extractDomainFromURL() {
  }
}