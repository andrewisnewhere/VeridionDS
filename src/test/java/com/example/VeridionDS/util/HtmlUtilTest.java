package com.example.VeridionDS.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
          "<a href=\"https://reddit.com/r/example\"></a>" +
          "<a href=\"tel:+1-814-954-5812\"><span>Call now</span></a>" +
          "<a itemprop=\"telephone\" href=\"tel:4146284474\">(414) 628-4474</a>" +
          "<p>Phone number in text: (123) 456-7890</p>" +
          "<p>Another phone number in text: +1 987-654-3210</p>" +
          "<a target=\"_blank\" id=\"action-list-1\" class=\"dtKbfb  oYxtQd\" href=\"tel:+1-814-954-5812\" itemprop=\"telephone\"<span>Call now</span></a>" +
          "<a itemprop=\"streetAddress\" href=\"https://goo.gl/maps/dEBbEXk7fSPTCRUr9\" target=\"_blank\"><i class=\"fas fa-map-marker\" aria-hidden=\"true\"></i> 230 Bayshore Blvd.<br>San Francisco, CA 94124, United States</a>" +
          "<div class=\"address\">123 Main St, Springfield, IL 62704</div>" +
          "<p class=\"location\">456 Elm St, Springfield, IL 62704</p>" +
          "<span itemprop=\"address\">789 Maple Ave, Springfield, IL 62704</span>" +
          "<p>Not an address: 123 Main St, Springfield, IL, April 62704</p>" +
          "<p>Random text that is not an address.</p>" +
          "<p>Address in text: 101 Oak Blvd, Springfield, IL 62704. More text.</p>" +
          "<p>Another address in text: 202 Pine Drive, Springfield, IL 62704</p>" +
          "</body></html>";
  private final Document document = Jsoup.parse(html);


  @Test
  void extractLinks() {
    LinkedHashSet<String> expectedLinks = new LinkedHashSet<>();
    expectedLinks.add("http://example.com/page1");
    expectedLinks.add("http://example.com/page2");
    expectedLinks.add("http://example.com/page3");
    expectedLinks.add("http://example.com/page4");
    expectedLinks.add("http://example.com/contact.html");

    String website = "http://example.com";
    LinkedHashSet<String> actualLinks = HtmlUtil.extractLinks(document, website);

    assertEquals(expectedLinks, actualLinks);
  }

  @Test
  public void extractPhoneNumbers() {
    LinkedHashSet<String> expectedPhoneNumbers = new LinkedHashSet<>();
    expectedPhoneNumbers.add("(414) 628-4474");
    expectedPhoneNumbers.add("4146284474");
    expectedPhoneNumbers.add("+1-814-954-5812");

    LinkedHashSet<String> actualPhoneNumbers = HtmlUtil.extractPhoneNumbers(document);

    assertEquals(expectedPhoneNumbers, actualPhoneNumbers);
  }

  @Test
  void extractAddresses() {
    LinkedHashSet<String> expectedAddresses = new LinkedHashSet<>();
    expectedAddresses.add("123 Main St, Springfield, IL 62704");
    expectedAddresses.add("456 Elm St, Springfield, IL 62704");
    expectedAddresses.add("789 Maple Ave, Springfield, IL 62704");

    LinkedHashSet<String> actualAddresses = HtmlUtil.extractAddresses(document);

    assertEquals(expectedAddresses, actualAddresses);
  }

  @Test
  void extractSocialMediaLinks() {
    LinkedHashSet<String> expectedLinks = new LinkedHashSet<>();
    expectedLinks.add("https://www.facebook.com/zuck/?fref=mentions");
    expectedLinks.add("https://www.instagram.com/example/?hl=en");
    expectedLinks.add("https://twitter.com/example");
    expectedLinks.add("https://www.linkedin.com/company/google/");
    expectedLinks.add("https://reddit.com/r/example");

    LinkedHashSet<String> actualLinks = HtmlUtil.extractSocialMediaLinks(document);

    assertEquals(expectedLinks, actualLinks);
  }
}