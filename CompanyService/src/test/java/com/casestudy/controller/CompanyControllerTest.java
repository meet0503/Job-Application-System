package com.casestudy.controller;

// For jsonPath checks like is() and hasSize()
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
// Static imports from Mockito and Spring Test
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor; // Import ArgumentCaptor
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.casestudy.entities.Company;
import com.casestudy.entities.external.TokenValidationResponse;
import com.casestudy.exception.CompanyNotFoundException;
import com.casestudy.exception.GlobalExceptionHandler; // Import GlobalExceptionHandler
import com.casestudy.feign.AuthServiceClient;
import com.casestudy.security.JwtAuthenticationFilter;
import com.casestudy.security.SecurityConfiguration;
import com.casestudy.service.CompanyServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(CompanyController.class)

@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // Mock the service layer
    private CompanyServiceImpl companyService;

    @MockitoBean // Mock the Feign client used by the security filter
    private AuthServiceClient authServiceClient;

    @Autowired // For converting objects to JSON
    private ObjectMapper objectMapper;

    // Test data
    private Company company1;
    private Company company2;
    private List<Company> companyList;
    private List<Company> companyInputList; // For POST request

    // Constants for readability
    private static final String COMPANY_NOT_FOUND = "No Company found with this id";
    private static final String ADMIN_TOKEN = "Bearer admintoken";
    private static final String USER_TOKEN = "Bearer usertoken";
    private static final String VALID_TOKEN = "Bearer validusertoken"; // Generic valid token (can be user)
    private static final String INVALID_TOKEN = "Bearer invalidtoken";

    @BeforeEach
    void setUp() {
        // Initialize test data
        company1 = new Company("c1", "Tech Corp", "Leading tech company");
        company2 = new Company("c2", "Finance Inc", "Financial services provider");
        companyList = Arrays.asList(company1, company2);

        // Input data for POST (usually without IDs)
        Company companyInput1 = new Company(null, "New Company A", "Description A");
        Company companyInput2 = new Company(null, "New Company B", "Description B");
        companyInputList = Arrays.asList(companyInput1, companyInput2);

        // Configure mock token validation responses for the AuthServiceClient
        when(authServiceClient.validateToken(ADMIN_TOKEN)).thenReturn(new TokenValidationResponse(true, "ADMIN"));
        when(authServiceClient.validateToken(USER_TOKEN)).thenReturn(new TokenValidationResponse(true, "USER"));
        when(authServiceClient.validateToken(VALID_TOKEN)).thenReturn(new TokenValidationResponse(true, "USER")); 
        when(authServiceClient.validateToken(INVALID_TOKEN)).thenReturn(new TokenValidationResponse(false, null));
       
    }

    // ----- GET /companies Tests -----

    @Test
    void testGetAllCompanies_Success() throws Exception {
        // Setup: Mock service response
        when(companyService.findAllCompanies()).thenReturn(companyList);

        // Execute & Verify
        mockMvc.perform(get("/companies")
                .header("Authorization", VALID_TOKEN)) // Use a valid token
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2))) // Check array size
                .andExpect(jsonPath("$[0].id", is("c1")))
                .andExpect(jsonPath("$[0].name", is("Tech Corp")))
                .andExpect(jsonPath("$[1].id", is("c2")))
                .andExpect(jsonPath("$[1].name", is("Finance Inc")));

        verify(companyService, times(1)).findAllCompanies(); // Verify service interaction
    }

    @Test
    void testGetAllCompanies_EmptyList() throws Exception {
        // Setup: Mock service response with empty list
        when(companyService.findAllCompanies()).thenReturn(Collections.emptyList());

        // Execute & Verify
        mockMvc.perform(get("/companies")
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0))); // Check array size is 0

        verify(companyService, times(1)).findAllCompanies();
    }

    @Test
    void testGetAllCompanies_Unauthorized_InvalidToken() throws Exception {
        // Execute & Verify: Use an invalid token
        mockMvc.perform(get("/companies")
                .header("Authorization", INVALID_TOKEN))
                .andExpect(status().isUnauthorized()); // Expect 401 due to filter rejecting token

        verify(companyService, never()).findAllCompanies(); // Service method should not be called
    }

    @Test
    void testGetAllCompanies_Unauthorized_NoToken() throws Exception {
        // Execute & Verify: Perform request without Authorization header
        mockMvc.perform(get("/companies"))
                .andExpect(status().isUnauthorized()); // Expect 401 as filter requires token

        verify(companyService, never()).findAllCompanies();
    }

    // ----- POST /companies Tests ----- (Requires ADMIN Role)

    @Test
    void testCreateCompany_Admin_Success() throws Exception {
        // Setup: Mock the service method (void return)
        // We don't need to capture argument here as it's void, just verify call
        doNothing().when(companyService).addCompany(anyList());

        // Execute & Verify
        mockMvc.perform(post("/companies")
                .header("Authorization", ADMIN_TOKEN) // Use ADMIN token
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(companyInputList))) // Send list of companies without IDs
                .andExpect(status().isCreated()) // Expect 201
                .andExpect(content().string("Company Created Successfully")); // Expect specific success message

        verify(companyService, times(1)).addCompany(anyList()); // Verify service was called
    }

    @Test
    void testCreateCompany_Admin_EmptyBody() throws Exception {
        // Execute & Verify: Send an empty JSON array
        mockMvc.perform(post("/companies")
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]")) // Empty array
                .andExpect(status().isBadRequest());

        verify(companyService, never()).addCompany(anyList()); // Service should not be called for bad input
    }


    @Test
    void testCreateCompany_User_Forbidden() throws Exception {
        // Execute & Verify: Use a non-ADMIN token (USER_TOKEN)
        mockMvc.perform(post("/companies")
                .header("Authorization", USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(companyInputList)))
                .andExpect(status().isForbidden()); // Expect 403 Forbidden due to @PreAuthorize

        verify(companyService, never()).addCompany(anyList()); // Service should not be called
    }

    // ----- GET /companies/{companyId} Tests -----

    @Test
    void testFindCompanyById_Success() throws Exception {
        // Setup: Mock service response for finding a specific company
        String companyId = "c1";
        when(companyService.findCompanyById(companyId)).thenReturn(company1);

        // Execute & Verify
        mockMvc.perform(get("/companies/{companyId}", companyId)
                .header("Authorization", VALID_TOKEN)) // Any valid user can view
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(companyId)))
                .andExpect(jsonPath("$.name", is(company1.getName())))
                .andExpect(jsonPath("$.description", is(company1.getDescription())));

        verify(companyService, times(1)).findCompanyById(companyId);
    }

    @Test
    void testFindCompanyById_NotFound() throws Exception {
        // Setup: Mock service to throw exception for a non-existent ID
        String nonExistentId = "c99";
        when(companyService.findCompanyById(nonExistentId))
                .thenThrow(new CompanyNotFoundException(COMPANY_NOT_FOUND));

        // Execute & Verify
        mockMvc.perform(get("/companies/{companyId}", nonExistentId)
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isNotFound()) // Expect 404
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(false))) // Check error response structure
                .andExpect(jsonPath("$.message", is(COMPANY_NOT_FOUND)));

        verify(companyService, times(1)).findCompanyById(nonExistentId);
    }

    // ----- PUT /companies/{companyId} Tests ----- (Requires ADMIN Role)

    @Test
    void testUpdateCompany_Admin_Success() throws Exception {
        // Setup
        String companyId = "c1";
        // Data sent in the request body for update
        Company updatedCompanyData = new Company(companyId, "Tech Corp Updated", "New Description");
        // Data returned by the service after successful update
        Company returnedCompany = new Company(companyId, "Tech Corp Updated", "New Description"); // Assume service returns updated obj

        // Use ArgumentCaptor to check the object passed to the service method
        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        when(companyService.updateCompany(eq(companyId), companyCaptor.capture())).thenReturn(returnedCompany);

        // Execute & Verify
        mockMvc.perform(put("/companies/{companyId}", companyId)
                .header("Authorization", ADMIN_TOKEN) // Use ADMIN token
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCompanyData)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(companyId)))
                .andExpect(jsonPath("$.name", is(returnedCompany.getName())))
                .andExpect(jsonPath("$.description", is(returnedCompany.getDescription())));

        verify(companyService, times(1)).updateCompany(eq(companyId), any(Company.class));

        // Optional: Verify the data passed to the service
        Company capturedCompany = companyCaptor.getValue();
        // assertEquals(updatedCompanyData.getId(), capturedCompany.getId()); // ID in PUT body might be ignored
        org.junit.jupiter.api.Assertions.assertEquals(updatedCompanyData.getName(), capturedCompany.getName());
        org.junit.jupiter.api.Assertions.assertEquals(updatedCompanyData.getDescription(), capturedCompany.getDescription());
    }

    @Test
    void testUpdateCompany_Admin_NotFound() throws Exception {
        // Setup: Mock service to throw exception when trying to update a non-existent company
        String nonExistentId = "c99";
        Company updateData = new Company(nonExistentId, "Update Attempt", "Desc");
        when(companyService.updateCompany(eq(nonExistentId), any(Company.class)))
                .thenThrow(new CompanyNotFoundException(COMPANY_NOT_FOUND));

        // Execute & Verify
        mockMvc.perform(put("/companies/{companyId}", nonExistentId)
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound()) // Expect 404
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(COMPANY_NOT_FOUND)));

        verify(companyService, times(1)).updateCompany(eq(nonExistentId), any(Company.class));
    }

    @Test
    void testUpdateCompany_User_Forbidden() throws Exception {
        // Setup
        String companyId = "c1";
        Company updatedCompanyData = new Company(companyId, "Tech Corp Updated", "New Description");

        // Execute & Verify: Use USER token for an ADMIN-only endpoint
        mockMvc.perform(put("/companies/{companyId}", companyId)
                .header("Authorization", USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCompanyData)))
                .andExpect(status().isForbidden()); // Expect 403

        verify(companyService, never()).updateCompany(anyString(), any(Company.class)); // Service should not be called
    }

    // ----- DELETE /companies/{companyId} Tests ----- (Requires ADMIN Role)

    @Test
    void testDeleteCompany_Admin_Success() throws Exception {
        // Setup
        String companyId = "c1";
        // Mock service to return the deleted company (used for the success message)
        when(companyService.deleteCompany(companyId)).thenReturn(company1);
        String expectedMessage = "Company with Name " + company1.getName() + " is deleted successfully";

        // Execute & Verify
        mockMvc.perform(delete("/companies/{companyId}", companyId)
                .header("Authorization", ADMIN_TOKEN)) // Use ADMIN token
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMessage)); // Verify the success message

        verify(companyService, times(1)).deleteCompany(companyId);
    }

    @Test
    void testDeleteCompany_Admin_NotFound() throws Exception {
        // Setup: Mock service to throw exception for non-existent ID
        String nonExistentId = "c99";
        when(companyService.deleteCompany(nonExistentId))
                .thenThrow(new CompanyNotFoundException(COMPANY_NOT_FOUND));

        // Execute & Verify
        mockMvc.perform(delete("/companies/{companyId}", nonExistentId)
                .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isNotFound()) // Expect 404
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is(COMPANY_NOT_FOUND)));

        verify(companyService, times(1)).deleteCompany(nonExistentId);
    }

    @Test
    void testDeleteCompany_User_Forbidden() throws Exception {
        // Setup
        String companyId = "c1";

        // Execute & Verify: Use USER token for an ADMIN-only endpoint
        mockMvc.perform(delete("/companies/{companyId}", companyId)
                .header("Authorization", USER_TOKEN))
                .andExpect(status().isForbidden()); // Expect 403

        verify(companyService, never()).deleteCompany(anyString()); // Service should not be called
    }
}