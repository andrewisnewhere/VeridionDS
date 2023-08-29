package com.example.VeridionDS.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtils {
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public static boolean isValidDomainOrPhone(String domainOrPhone) {
        if (domainOrPhone == null) {
            return false;
        }
        domainOrPhone = sanitizeInput(domainOrPhone);
        if (domainOrPhone.isEmpty()) {
            return false;
        }
        return isValidDomain(domainOrPhone) || isValidPhoneNumber(domainOrPhone);
    }

    private static String sanitizeInput(String domainOrPhone) {
        return domainOrPhone.trim().replaceAll("[^a-zA-Z0-9\\.\\-]", "");
    }

    private static boolean isValidDomain(String domain) {
        return DOMAIN_PATTERN.matcher(domain).matches();
    }

    private static boolean isValidPhoneNumber(String phoneNumber) {
        try {
            Phonenumber.PhoneNumber numberProto = phoneNumberUtil.parse(phoneNumber, null);
            return phoneNumberUtil.isValidNumber(numberProto);
        } catch (NumberParseException e) {
            return false;
        }
    }
}
