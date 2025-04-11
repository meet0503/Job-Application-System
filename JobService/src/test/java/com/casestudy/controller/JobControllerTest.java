package com.casestudy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser; // For security testing
import org.springframework.test.web.servlet.MockMvc;

import com.casestudy.config.FeignClientInterceptor; // Need to import security/config beans
import com.casestudy.dto.JobDTO;
import com.casestudy.entities.Job;
import com.casestudy.entities.external.Company;
import com.casestudy.entities.external.Rating;
import com.casestudy.entities.external.TokenValidationResponse;
import com.casestudy.exception.GlobalExceptionHandler; // Import exception handler
import com.casestudy.exception.JobNotFoundException;
import com.casestudy.feign.AuthServiceClient; // Mock Auth Client
import com.casestudy.payload.ApiResponse;
import com.casestudy.security.JwtAuthenticationFilter; // Need security components
import com.casestudy.security.SecurityConfiguration;
import com.casestudy.service.JobServiceImpl; // Mock the service implementation
import com.fasterxml.jackson.databind.ObjectMapper; // For JSON conversion

// Use @WebMvcTest for testing the controller layer specifically
// Specify the controller(s) to test
@WebMvcTest(JobController.class)
// Import Security Configuration, Filter, Interceptor and Exception Handler to apply them in the test context
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, FeignClientInterceptor.class, GlobalExceptionHandler.class})
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc to perform requests

    @MockBean // Creates a Mockito mock and registers it in the ApplicationContext
    private JobServiceImpl jobService;

    @MockBean // Mock the AuthServiceClient used by the JwtAuthenticationFilter
    private AuthServiceClient authServiceClient;

    @Autowired
    private ObjectMapper objectMapper; // Utility to convert objects to JSON

    private JobDTO jobDTO1;
    private Job job1;
    private List<Job> jobList;

    @BeforeEach
    void setUp() {
        // Mock the token validation behaviour for most tests
        // Assume valid token with USER role by default unless testing specific auth cases
        when(authServiceClient.validateToken(anyString()))
            .thenReturn(new TokenValidationResponse(true, "USER"));

        Company company1 = new Company("comp1", "Tech Corp", "Leading tech company");
        List<Rating> ratings1 = Arrays.asList(new Rating("r1", "Great Place", "Good work-life balance", 4.5));
        jobDTO1 = new JobDTO("job1", "Software Engineer", "Develop awesome software", "80000", "120000", "Remote", company1, ratings1);
        
        job1 = new Job("job1", "Software Engineer", "Develop awesome software", "80000", "120000", "Remote", "comp1");
        Job jobInput = new Job(null, "QA Tester", "Test things", "60k", "90k", "Local", "comp1"); // For POST
        jobList = Arrays.asList(jobInput); // List for POST request
    }

    // --- Test GET /jobs ---
    @Test
    @WithMockUser // Simulate an authenticated user (role doesn't matter for this endpoint)
    void testGetAllJobs_Success() throws Exception {
        List<JobDTO> jobDTOs = Arrays.asList(jobDTO1);
        when(jobService.findAllJobs()).thenReturn(jobDTOs);

        mockMvc.perform(get("/jobs")
                .header("Authorization", "Bearer validtoken")) // Need header for filter
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("job1"))
                .andExpect(jsonPath("$[0].title").value("Software Engineer"))
                .andExpect(jsonPath("$[0].company.name").value("Tech Corp"));

        verify(jobService, times(1)).findAllJobs();
    }

    @Test
    // No @WithMockUser - should be blocked by security filter if token isn't passed/valid
    void testGetAllJobs_Unauthorized() throws Exception {
         // Mock auth service to return invalid
         when(authServiceClient.validateToken("Bearer invalidtoken")).thenReturn(new TokenValidationResponse(false, null));

        mockMvc.perform(get("/jobs")
                .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized()); // Expect 401 due to filter failure

         // Or test without any Authorization header
         mockMvc.perform(get("/jobs"))
                .andExpect(status().isUnauthorized()); // Expect 401 as header is missing

        verify(jobService, never()).findAllJobs(); // Service method should not be called
    }

    // --- Test POST /jobs ---
    @Test
    @WithMockUser(authorities = "ADMIN") // Simulate authenticated ADMIN user
    void testCreateJob_Admin_Success() throws Exception {
         // Mock the auth service for ADMIN role specifically for this test
         when(authServiceClient.validateToken("Bearer admintoken")).thenReturn(new TokenValidationResponse(true, "ADMIN"));

        // Mock the service method (void return)
        doNothing().when(jobService).addJob(anyList());

        mockMvc.perform(post("/jobs")
                .header("Authorization", "Bearer admintoken") // Use a token recognized as ADMIN
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jobList))) // Send list of jobs
                .andExpect(status().isCreated())
                .andExpect(content().string("Job Created Successfully"));

        verify(jobService, times(1)).addJob(anyList());
    }

    @Test
    @WithMockUser(authorities = "USER") // Simulate authenticated non-ADMIN user
    void testCreateJob_User_Forbidden() throws Exception {
         // Mock the auth service for USER role
         when(authServiceClient.validateToken("Bearer usertoken")).thenReturn(new TokenValidationResponse(true, "USER"));

        mockMvc.perform(post("/jobs")
                .header("Authorization", "Bearer usertoken") // Use a token recognized as USER
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jobList)))
                .andExpect(status().isForbidden()); // Expect 403 Forbidden

        verify(jobService, never()).addJob(anyList());
    }
    
    @Test
    @WithMockUser(authorities = "ADMIN") 
    void testCreateJob_Admin_BadRequest() throws Exception {
        // Simulate service throwing IllegalArgumentException (e.g., missing companyId)
        doThrow(new IllegalArgumentException("Company ID is required"))
            .when(jobService).addJob(anyList());
        
        when(authServiceClient.validateToken("Bearer admintoken")).thenReturn(new TokenValidationResponse(true, "ADMIN"));

        mockMvc.perform(post("/jobs")
                .header("Authorization", "Bearer admintoken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jobList))) // Send potentially invalid list
                .andExpect(status().isBadRequest()) // Expect 400 Bad Request due to GlobalExceptionHandler
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid input: Company ID is required"));

        verify(jobService, times(1)).addJob(anyList()); // Service method is called but throws exception
    }

    // --- Test GET /jobs/{jobId} ---
    @Test
    @WithMockUser
    void testFindJob_Success() throws Exception {
        when(jobService.findJobById("job1")).thenReturn(jobDTO1);
        when(authServiceClient.validateToken("Bearer validtoken")).thenReturn(new TokenValidationResponse(true, "USER"));


        mockMvc.perform(get("/jobs/{jobId}", "job1")
                .header("Authorization", "Bearer validtoken"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("job1"))
                .andExpect(jsonPath("$.title").value("Software Engineer"));

        verify(jobService, times(1)).findJobById("job1");
    }

    @Test
    @WithMockUser
    void testFindJob_NotFound() throws Exception {
        when(jobService.findJobById("unknownJob")).thenThrow(new JobNotFoundException("No Job found with this id"));
        when(authServiceClient.validateToken("Bearer validtoken")).thenReturn(new TokenValidationResponse(true, "USER"));


        mockMvc.perform(get("/jobs/{jobId}", "unknownJob")
                 .header("Authorization", "Bearer validtoken"))
                .andExpect(status().isNotFound()) // Expect 404 Not Found
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No Job found with this id"));

        verify(jobService, times(1)).findJobById("unknownJob");
    }

    // --- Test GET /jobs/company/{companyId} ---
     @Test
    @WithMockUser
    void testGetJobsByCompany_Success() throws Exception {
        List<JobDTO> companyJobDTOs = Arrays.asList(jobDTO1); // Assume only one job for this company for simplicity
        when(jobService.findJobsByCompanyId("comp1")).thenReturn(companyJobDTOs);
        when(authServiceClient.validateToken("Bearer validtoken")).thenReturn(new TokenValidationResponse(true, "USER"));


        mockMvc.perform(get("/jobs/company/{companyId}", "comp1")
                .header("Authorization", "Bearer validtoken"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("job1"))
                .andExpect(jsonPath("$[0].company.id").value("comp1"));

        verify(jobService, times(1)).findJobsByCompanyId("comp1");
    }

    @Test
    @WithMockUser
    void testGetJobsByCompany_NotFound() throws Exception {
         // Service returns empty list if company exists but has no jobs
         when(jobService.findJobsByCompanyId("comp-no-jobs")).thenReturn(Collections.emptyList());
         when(authServiceClient.validateToken("Bearer validtoken")).thenReturn(new TokenValidationResponse(true, "USER"));


         mockMvc.perform(get("/jobs/company/{companyId}", "comp-no-jobs")
                 .header("Authorization", "Bearer validtoken"))
                 .andExpect(status().isOk()) // Still OK, but empty list
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.length()").value(0));

         verify(jobService, times(1)).findJobsByCompanyId("comp-no-jobs");
     }


    // --- Test PUT /jobs/{jobId} ---
    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateJob_Admin_Success() throws Exception {
        Job updatedJob = new Job("job1", "Senior SE", "Desc", "100k", "150k", "NY", "comp1");
        when(jobService.updateJob(eq("job1"), any(Job.class))).thenReturn(updatedJob);
        when(authServiceClient.validateToken("Bearer admintoken")).thenReturn(new TokenValidationResponse(true, "ADMIN"));


        mockMvc.perform(put("/jobs/{jobId}", "job1")
                .header("Authorization", "Bearer admintoken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedJob))) // Send updated job data
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("job1"))
                .andExpect(jsonPath("$.title").value("Senior SE"));

        verify(jobService, times(1)).updateJob(eq("job1"), any(Job.class));
    }
    
     @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateJob_Admin_NotFound() throws Exception {
        Job updatedJobData = new Job("unknownJob", "Senior SE", "Desc", "100k", "150k", "NY", "comp1");
        when(jobService.updateJob(eq("unknownJob"), any(Job.class)))
            .thenThrow(new JobNotFoundException("No Job found with this id"));
         when(authServiceClient.validateToken("Bearer admintoken")).thenReturn(new TokenValidationResponse(true, "ADMIN"));


        mockMvc.perform(put("/jobs/{jobId}", "unknownJob")
                .header("Authorization", "Bearer admintoken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedJobData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No Job found with this id"));

        verify(jobService, times(1)).updateJob(eq("unknownJob"), any(Job.class));
    }


    @Test
    @WithMockUser(authorities = "USER") // Non-admin
    void testUpdateJob_User_Forbidden() throws Exception {
        Job updatedJob = new Job("job1", "Senior SE", "Desc", "100k", "150k", "NY", "comp1");
        when(authServiceClient.validateToken("Bearer usertoken")).thenReturn(new TokenValidationResponse(true, "USER"));


        mockMvc.perform(put("/jobs/{jobId}", "job1")
                 .header("Authorization", "Bearer usertoken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedJob)))
                .andExpect(status().isForbidden());

        verify(jobService, never()).updateJob(anyString(), any(Job.class));
    }

    // --- Test DELETE /jobs/{jobId} ---
    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteJob_Admin_Success() throws Exception {
        ApiResponse apiResponse = ApiResponse.builder().success(true).message("Job Deleted Successfully").build();
        ResponseEntity<ApiResponse> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        when(jobService.deleteJob("job1")).thenReturn(responseEntity);
        when(authServiceClient.validateToken("Bearer admintoken")).thenReturn(new TokenValidationResponse(true, "ADMIN"));


        mockMvc.perform(delete("/jobs/{jobId}", "job1")
                 .header("Authorization", "Bearer admintoken"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Assuming ApiResponse is returned
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job Deleted Successfully"));

        verify(jobService, times(1)).deleteJob("job1");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteJob_Admin_NotFound() throws Exception {
        when(jobService.deleteJob("unknownJob")).thenThrow(new JobNotFoundException("No Job found with this id"));
        when(authServiceClient.validateToken("Bearer admintoken")).thenReturn(new TokenValidationResponse(true, "ADMIN"));

        mockMvc.perform(delete("/jobs/{jobId}", "unknownJob")
                .header("Authorization", "Bearer admintoken"))
                .andExpect(status().isNotFound())
                 .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No Job found with this id"));


        verify(jobService, times(1)).deleteJob("unknownJob");
    }

    @Test
    @WithMockUser(authorities = "USER") // Non-admin
    void testDeleteJob_User_Forbidden() throws Exception {
         when(authServiceClient.validateToken("Bearer usertoken")).thenReturn(new TokenValidationResponse(true, "USER"));

        mockMvc.perform(delete("/jobs/{jobId}", "job1")
                .header("Authorization", "Bearer usertoken"))
                .andExpect(status().isForbidden());

        verify(jobService, never()).deleteJob(anyString());
    }
}