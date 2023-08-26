package com.example.VeridionDS.util;

import com.example.VeridionDS.model.Company;
import com.opencsv.bean.AbstractBeanField;

public class DomainConverter extends AbstractBeanField<Company, String> {
  @Override
  protected Object convert(String value) {
    return formatDomain(value);
  }

  public static String formatDomain(String domain) {
    // Add "http://" protocol if missing
    if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
      domain = "http://" + domain;
    }
    return domain;
  }
}
