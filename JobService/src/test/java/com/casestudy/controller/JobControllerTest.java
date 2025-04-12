package com.casestudy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.casestudy.config.FeignClientInterceptor;
import com.casestudy.dto.JobDTO;
import com.casestudy.entities.Job;
import com.casestudy.entities.external.Company;
import com.casestudy.entities.external.Rating;
import com.casestudy.entities.external.TokenValidationResponse;
import com.casestudy.exception.GlobalExceptionHandler;
import com.casestudy.exception.JobNotFoundException;
import com.casestudy.feign.AuthServiceClient;
import com.casestudy.payload.ApiResponse;
import com.casestudy.security.JwtAuthenticationFilter;
import com.casestudy.security.SecurityConfiguration;
import com.casestudy.service.JobServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(JobController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, FeignClientInterceptor.class, GlobalExceptionHandler.class})
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobServiceImpl jobService;

    @MockBean
    private AuthServiceClient authServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    private JobDTO jobDTO1;
    private JobDTO jobDTO2;
    private Job job1;
    private Job job2;
    private List<Job> jobInputList;
    private Company company1;
    private List<Rating> ratings1;

    private static final String JOB_NOT_FOUND = "No Job found with this id";
    private static final String ADMIN_TOKEN = "Bearer admintoken";
    private static final String USER_TOKEN = "Bearer usertoken";
    private static final String VALID_TOKEN = "Bearer validtoken";
    private static final String INVALID_TOKEN = "Bearer invalidtoken";

    @BeforeEach
    void setUp() {
        // Setup Company and Ratings
        company1 = new Company("comp1", "Tech Corp", "Leading tech company");
        ratings1 = Arrays.asList(new Rating("r1", "Great Place", "Good work-life balance", 4.5));
        
        // Setup JobDTOs (which include Company and Ratings)
        jobDTO1 = new JobDTO(
            "job1", 
            "Software Engineer", 
            "Develop awesome software", 
            "80000", 
            "120000", 
            "Remote", 
            company1, 
            ratings1
        );
        
        jobDTO2 = new JobDTO(
            "job2", 
            "Data Scientist", 
            "Analyze data and create models", 
            "90000", 
            "140000", 
            "Hybrid", 
            company1, 
            ratings1
        );
        
        // Setup Job entities (without Company and Ratings)
        job1 = new Job(
            "job1", 
            "Software Engineer", 
            "Develop awesome software", 
            "80000", 
            "120000", 
            "Remote", 
            "comp1"
        );
        
        job2 = new Job(
            "job2", 
            "Data Scientist", 
            "Analyze data and create models", 
            "90000", 
            "140000", 
            "Hybrid", 
            "comp1"
        );
        
        // Setup input jobs for POST request (without IDs as they'll be generated)
        Job jobInput1 = new Job(
            null, 
            "QA Tester", 
            "Test software applications", 
            "60000", 
            "90000", 
            "Onsite", 
            "comp1"
        );
        
        Job jobInput2 = new Job(
            null, 
            "DevOps Engineer", 
            "Implement CI/CD pipelines", 
            "85000", 
            "130000", 
            "Remote", 
            "comp1"
        );
        
        jobInputList = Arrays.asList(jobInput1, jobInput2);
        
        // Configure token validation responses
        when(authServiceClient.validateToken(ADMIN_TOKEN)).thenReturn(new TokenValidationResponse(true, "ADMIN"));
        when(authServiceClient.validateToken(USER_TOKEN)).thenReturn(new TokenValidationResponse(true, "USER"));
        when(authServiceClient.validateToken(VALID_TOKEN)).thenReturn(new TokenValidationResponse(true, "USER"));
        when(authServiceClient.validateToken(INVALID_TOKEN)).thenReturn(new TokenValidationResponse(false, null));
    }

    // ----- GET /jobs Tests -----
    
    @Test
    void testGetAllJobs_Success() throws Exception {
        // Setup
        List<JobDTO> jobDTOs = Arrays.asList(jobDTO1, jobDTO2);
        when(jobService.findAllJobs()).thenReturn(jobDTOs);

        // Execute & Verify
        mockMvc.perform(get("/jobs")
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("job1"))
                .andExpect(jsonPath("$[0].title").value("Software Engineer"))
                .andExpect(jsonPath("$[0].company.name").value("Tech Corp"))
                .andExpect(jsonPath("$[0].company.id").value("comp1"))
                .andExpect(jsonPath("$[0].ratings.length()").value(1))
                .andExpect(jsonPath("$[0].ratings[0].ratings").value(4.5))
                .andExpect(jsonPath("$[1].id").value("job2"))
                .andExpect(jsonPath("$[1].title").value("Data Scientist"))
        		.andExpect(jsonPath("$[1].company.name").value("Tech Corp"));

        verify(jobService, times(1)).findAllJobs();
    }
    
    @Test
    void testGetAllJobs_EmptyList() throws Exception {
        // Setup
        when(jobService.findAllJobs()).thenReturn(Collections.emptyList());

        // Execute & Verify
        mockMvc.perform(get("/jobs")
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(jobService, times(1)).findAllJobs();
    }

    @Test
    void testGetAllJobs_Unauthorized_InvalidToken() throws Exception {
        // Execute & Verify
        mockMvc.perform(get("/jobs")
                .header("Authorization", INVALID_TOKEN))
                .andExpect(status().isUnauthorized());

        verify(jobService, never()).findAllJobs();
    }
    
    @Test
    void testGetAllJobs_Unauthorized_NoToken() throws Exception {
        // Execute & Verify
        mockMvc.perform(get("/jobs"))
                .andExpect(status().isUnauthorized());

        verify(jobService, never()).findAllJobs();
    }

    // ----- POST /jobs Tests -----
    
    @Test
    void testCreateJob_Admin_Success() throws Exception {
        // Setup
        ArgumentCaptor<List<Job>> jobCaptor = ArgumentCaptor.forClass(List.class);
        doNothing().when(jobService).addJob(jobCaptor.capture());

        // Execute & Verify
        mockMvc.perform(post("/jobs")
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jobInputList)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Job Created Successfully"));

        verify(jobService, times(1)).addJob(any());
        
        // Verify content passed to service
        List<Job> capturedJobs = jobCaptor.getValue();
        assertEquals(jobInputList.size(), capturedJobs.size());
        for (int i = 0; i < jobInputList.size(); i++) {
            Job expectedJob = jobInputList.get(i);
            Job actualJob = capturedJobs.get(i);
            assertEquals(expectedJob.getTitle(), actualJob.getTitle());
            assertEquals(expectedJob.getDescription(), actualJob.getDescription());
            assertEquals(expectedJob.getMinSalary(), actualJob.getMinSalary());
            assertEquals(expectedJob.getMaxSalary(), actualJob.getMaxSalary());
            assertEquals(expectedJob.getLocation(), actualJob.getLocation());
            assertEquals(expectedJob.getCompanyId(), actualJob.getCompanyId());
        }
    }
    
    @Test
    void testCreateJob_Admin_MissingCompanyId() throws Exception {
        // Setup
        List<Job> invalidJobs = Arrays.asList(new Job(null, "Invalid Job", "Missing company ID", "50000", "80000", "Remote", null));
        doThrow(new IllegalArgumentException("Company ID is required"))
            .when(jobService).addJob(any());

        // Execute & Verify
        mockMvc.perform(post("/jobs")
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidJobs)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid input: Company ID is required"));

        verify(jobService, times(1)).addJob(any());
    }
    
    @Test
    void testCreateJob_Admin_EmptyBody() throws Exception {
        // Execute & Verify
        mockMvc.perform(post("/jobs")
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]")) // Empty array
                .andExpect(status().isBadRequest());

        verify(jobService, never()).addJob(any());
    }

    @Test
    void testCreateJob_User_Forbidden() throws Exception {
        // Execute & Verify
        mockMvc.perform(post("/jobs")
                .header("Authorization", USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jobInputList)))
                .andExpect(status().isForbidden());

        verify(jobService, never()).addJob(any());
    }

    // ----- GET /jobs/{jobId} Tests -----
    
    @Test
    void testFindJob_Success() throws Exception {
        // Setup
        String jobId = "job1";
        when(jobService.findJobById(jobId)).thenReturn(jobDTO1);

        // Execute & Verify
        mockMvc.perform(get("/jobs/{jobId}", jobId)
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(jobId))
                .andExpect(jsonPath("$.title").value("Software Engineer"))
                .andExpect(jsonPath("$.description").value("Develop awesome software"))
                .andExpect(jsonPath("$.minSalary").value("80000"))
                .andExpect(jsonPath("$.maxSalary").value("120000"))
                .andExpect(jsonPath("$.location").value("Remote"))
                .andExpect(jsonPath("$.company.id").value("comp1"))
                .andExpect(jsonPath("$.company.name").value("Tech Corp"))
                .andExpect(jsonPath("$.ratings[0].id").value("r1"));

        verify(jobService, times(1)).findJobById(jobId);
    }

    @Test
    void testFindJob_NotFound() throws Exception {
        // Setup
        String jobId = "nonexistent";
        when(jobService.findJobById(jobId)).thenThrow(new JobNotFoundException(JOB_NOT_FOUND));

        // Execute & Verify
        mockMvc.perform(get("/jobs/{jobId}", jobId)
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(JOB_NOT_FOUND));

        verify(jobService, times(1)).findJobById(jobId);
    }

    // ----- GET /jobs/company/{companyId} Tests -----
    
    @Test
    void testGetJobsByCompany_Success() throws Exception {
        // Setup
        String companyId = "comp1";
        List<JobDTO> companyJobs = Arrays.asList(jobDTO1, jobDTO2);
        when(jobService.findJobsByCompanyId(companyId)).thenReturn(companyJobs);

        // Execute & Verify
        mockMvc.perform(get("/jobs/company/{companyId}", companyId)
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("job1"))
                .andExpect(jsonPath("$[1].id").value("job2"))
                .andExpect(jsonPath("$[0].company.id").value(companyId))
                .andExpect(jsonPath("$[1].company.id").value(companyId));

        verify(jobService, times(1)).findJobsByCompanyId(companyId);
    }

    @Test
    void testGetJobsByCompany_EmptyList() throws Exception {
        // Setup
        String companyId = "comp-no-jobs";
        when(jobService.findJobsByCompanyId(companyId)).thenReturn(Collections.emptyList());

        // Execute & Verify
        mockMvc.perform(get("/jobs/company/{companyId}", companyId)
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(jobService, times(1)).findJobsByCompanyId(companyId);
    }

    // ----- PUT /jobs/{jobId} Tests -----
    
    @Test
    void testUpdateJob_Admin_Success() throws Exception {
        // Setup
        String jobId = "job1";
        Job updatedJob = new Job(
            jobId, 
            "Senior Software Engineer", 
            "Lead development of critical systems", 
            "100000", 
            "150000", 
            "Hybrid", 
            "comp1"
        );
        
        // Create a Job that would be returned by the service after update
        Job returnedJob = new Job(
            jobId, 
            updatedJob.getTitle(),
            updatedJob.getDescription(),
            updatedJob.getMinSalary(),
            updatedJob.getMaxSalary(),
            updatedJob.getLocation(),
            updatedJob.getCompanyId()
        );
        
        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        when(jobService.updateJob(eq(jobId), jobCaptor.capture())).thenReturn(returnedJob);

        // Execute & Verify
        mockMvc.perform(put("/jobs/{jobId}", jobId)
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedJob)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(jobId))
                .andExpect(jsonPath("$.title").value("Senior Software Engineer"))
                .andExpect(jsonPath("$.description").value("Lead development of critical systems"))
                .andExpect(jsonPath("$.minSalary").value("100000"))
                .andExpect(jsonPath("$.maxSalary").value("150000"))
                .andExpect(jsonPath("$.location").value("Hybrid"));

        verify(jobService, times(1)).updateJob(eq(jobId), any(Job.class));
        
        // Verify the job passed to service
        Job capturedJob = jobCaptor.getValue();
        assertEquals(updatedJob.getId(), capturedJob.getId());
        assertEquals(updatedJob.getTitle(), capturedJob.getTitle());
        assertEquals(updatedJob.getDescription(), capturedJob.getDescription());
        assertEquals(updatedJob.getMinSalary(), capturedJob.getMinSalary());
        assertEquals(updatedJob.getMaxSalary(), capturedJob.getMaxSalary());
        assertEquals(updatedJob.getLocation(), capturedJob.getLocation());
        assertEquals(updatedJob.getCompanyId(), capturedJob.getCompanyId());
    }
    
    @Test
    void testUpdateJob_Admin_NotFound() throws Exception {
        // Setup
        String jobId = "nonexistent";
        Job updatedJob = new Job(
            jobId, 
            "Doesn't Matter", 
            "This job doesn't exist", 
            "100000", 
            "150000", 
            "Remote", 
            "comp1"
        );
        
        when(jobService.updateJob(eq(jobId), any(Job.class)))
            .thenThrow(new JobNotFoundException(JOB_NOT_FOUND));

        // Execute & Verify
        mockMvc.perform(put("/jobs/{jobId}", jobId)
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedJob)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(JOB_NOT_FOUND));

        verify(jobService, times(1)).updateJob(eq(jobId), any(Job.class));
    }

    @Test
    void testUpdateJob_User_Forbidden() throws Exception {
        // Setup
        String jobId = "job1";
        Job updatedJob = new Job(
            jobId, 
            "Senior Software Engineer", 
            "Lead development of critical systems", 
            "100000", 
            "150000", 
            "Hybrid", 
            "comp1"
        );

        // Execute & Verify
        mockMvc.perform(put("/jobs/{jobId}", jobId)
                .header("Authorization", USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedJob)))
                .andExpect(status().isForbidden());

        verify(jobService, never()).updateJob(anyString(), any(Job.class));
    }

    // ----- DELETE /jobs/{jobId} Tests -----
    
    @Test
    void testDeleteJob_Admin_Success() throws Exception {
        // Setup
        String jobId = "job1";
        String deleteMessage = "Job Deleted Successfully: Software Engineer (job1)";
        
        ApiResponse apiResponse = ApiResponse.builder()
                .success(true)
                .message(deleteMessage)
                .build();
                
        ResponseEntity<ApiResponse> responseEntity = ResponseEntity.ok(apiResponse);
        when(jobService.deleteJob(jobId)).thenReturn(responseEntity);

        // Execute & Verify
        mockMvc.perform(delete("/jobs/{jobId}", jobId)
                .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(deleteMessage));

        verify(jobService, times(1)).deleteJob(jobId);
    }

    @Test
    void testDeleteJob_Admin_NotFound() throws Exception {
        // Setup
        String jobId = "nonexistent";
        when(jobService.deleteJob(jobId)).thenThrow(new JobNotFoundException(JOB_NOT_FOUND));

        // Execute & Verify
        mockMvc.perform(delete("/jobs/{jobId}", jobId)
                .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(JOB_NOT_FOUND));

        verify(jobService, times(1)).deleteJob(jobId);
    }

    @Test
    void testDeleteJob_User_Forbidden() throws Exception {
        // Setup
        String jobId = "job1";

        // Execute & Verify
        mockMvc.perform(delete("/jobs/{jobId}", jobId)
                .header("Authorization", USER_TOKEN))
                .andExpect(status().isForbidden());

        verify(jobService, never()).deleteJob(anyString());
    }

    // Helper methods
    private void assertEquals(Object expected, Object actual) {
        if ((expected == null && actual != null) || 
            (expected != null && !expected.equals(actual))) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }
}