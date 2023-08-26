package com.example.VeridionDS;

import com.example.VeridionDS.controller.CompanyController;
import com.example.VeridionDS.service.CompanyService;
import com.example.VeridionDS.model.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class CompanyControllerTests {
//
//    @InjectMocks
//    private CompanyController companyController;
//
//    @Mock
//    private CompanyService companyService;
//
//    @BeforeEach
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testSearchByDomainOrPhone_Found() {
//        Company company = new Company(); // assuming default constructor or you can set attributes
//        when(companyService.getCompanyByDomainOrPhoneNumber("test.com"))
//                .thenReturn(Optional.of(company));
//
//        ResponseEntity<Company> response = companyController.searchByDomainOrPhone("test.com");
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(company, response.getBody());
//    }
//
//    @Test
//    public void testSearchByDomainOrPhone_NotFound() {
//        when(companyService.getCompanyByDomainOrPhoneNumber("notfound.com"))
//                .thenReturn(Optional.empty());
//
//        ResponseEntity<Company> response = companyController.searchByDomainOrPhone("notfound.com");
//
//        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//    }
}
