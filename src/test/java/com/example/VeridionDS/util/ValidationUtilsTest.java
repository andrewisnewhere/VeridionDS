package com.example.VeridionDS.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidationUtilsTest {

  @Test
  public void isValidDomainOrPhone() {
    // Test cases with valid domain
    assertTrue(ValidationUtils.isValidDomainOrPhone("example.com"));
    assertTrue(ValidationUtils.isValidDomainOrPhone("www.example.com"));
    assertTrue(ValidationUtils.isValidDomainOrPhone("subdomain.example.com"));

    // Test cases with valid phone number
    assertTrue(ValidationUtils.isValidDomainOrPhone("+14155552671"));
    assertTrue(ValidationUtils.isValidDomainOrPhone("14155552671"));
    assertTrue(ValidationUtils.isValidDomainOrPhone("4155552671"));

    // Test cases with invalid domain
    assertFalse(ValidationUtils.isValidDomainOrPhone("example"));
    assertFalse(ValidationUtils.isValidDomainOrPhone("example."));
    assertFalse(ValidationUtils.isValidDomainOrPhone(".com"));

    // Test cases with invalid phone number
    assertFalse(ValidationUtils.isValidDomainOrPhone("+123456789"));
    assertFalse(ValidationUtils.isValidDomainOrPhone("1234"));
    assertFalse(ValidationUtils.isValidDomainOrPhone("abcdefghij"));
  }

}