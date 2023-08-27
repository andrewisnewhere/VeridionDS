package com.example.VeridionDS;

public class CompanyServiceTests {
//
//    @InjectMocks
//    private CompanyService companyService;
//
//    @Mock
//    private CompanyRepo companyRepo;
//
//    @BeforeEach
//    public void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testGetCompanyById() {
//        Company mockCompany = new Company();
//        mockCompany.setId(1);
//
//        when(companyRepo.findById(1)).thenReturn(Optional.of(mockCompany));
//
//        Optional<Company> result = companyService.getCompanyById(1);
//        assertTrue(result.isPresent());
//        assertEquals(1, result.get().getId());
//    }
//
//    @Test
//    public void testGetCompanyByDomainOrPhoneNumber_withDomain() {
//        Company mockCompany = new Company();
//        mockCompany.setDomain("test.com");
//
//        when(companyRepo.findByDomain("test.com")).thenReturn(mockCompany);
//
//        Optional<Company> result = companyService.getCompanyByDomainOrPhoneNumber("test.com");
//        assertTrue(result.isPresent());
//        assertEquals("test.com", result.get().getDomain());
//    }
//
//    @Test
//    public void testGetCompanyByDomainOrPhoneNumber_withPhoneNumber() {
//        Company mockCompany = new Company();
//        mockCompany.setDomain("test.com");
//
//        when(companyRepo.findByPhoneNumbersContaining("1234567890")).thenReturn(Arrays.asList(mockCompany));
//        when(companyRepo.findByDomain("1234567890")).thenReturn(null);
//
//        Optional<Company> result = companyService.getCompanyByDomainOrPhoneNumber("1234567890");
//        assertTrue(result.isPresent());
//        assertEquals("test.com", result.get().getDomain());
//    }
}
