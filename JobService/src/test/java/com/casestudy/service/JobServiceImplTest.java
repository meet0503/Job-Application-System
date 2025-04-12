package com.casestudy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.casestudy.dto.JobDTO;
import com.casestudy.entities.Job;
import com.casestudy.entities.external.Company;
import com.casestudy.entities.external.Rating;
import com.casestudy.exception.JobNotFoundException;
import com.casestudy.feign.CompanyClient;
import com.casestudy.feign.RatingClient;
import com.casestudy.mapper.JobMapper; // Although static, useful for context
import com.casestudy.payload.ApiResponse;
import com.casestudy.repository.JobRepository;

@ExtendWith(MockitoExtension.class) // Use MockitoExtension for JUnit 5
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyClient companyClient;

    @Mock
    private RatingClient ratingClient;

    @InjectMocks // Inject mocks into this instance
    private JobServiceImpl jobService;

    private Job job1;
    private Job job2;
    private Company company1;
    private List<Rating> ratings1;
    private JobDTO jobDTO1;

    @BeforeEach
    void setUp() {
        // Initialize common test data
        company1 = new Company("comp1", "Tech Corp", "Leading tech company");
        ratings1 = Arrays.asList(new Rating("r1", "Great Place", "Good work-life balance", 4.5));

        job1 = new Job("job1", "Software Engineer", "Develop awesome software", "80000", "120000", "Remote", "comp1");
        job2 = new Job("job2", "Data Analyst", "Analyze data trends", "70000", "100000", "New York", "comp1");

        // Use the actual mapper logic within the test context if needed, or mock static if complex
        // For simplicity, we'll create the DTO manually here based on expected mapping
        jobDTO1 = new JobDTO("job1", "Software Engineer", "Develop awesome software", "80000", "120000", "Remote", company1, ratings1);

    }

    @Test
    void testAddJob_Success() {
        List<Job> jobsToAdd = Collections.singletonList(new Job(null, "QA Tester", "Test software", "60k", "90k", "Austin", "comp1"));

        // No return value for saveAll when used with void addJob, just verify interaction
        jobService.addJob(jobsToAdd);

        // Verify that saveAll was called
        verify(jobRepository, times(1)).saveAll(anyList());
        // Verify that an ID was generated (check the first job in the list)
        assertNotNull(jobsToAdd.get(0).getId());
        assertEquals("comp1", jobsToAdd.get(0).getCompanyId());
    }
    
    @Test
    void testAddJob_Failure_MissingCompanyId() {
        List<Job> jobsToAdd = Collections.singletonList(new Job(null, "QA Tester", "Test software", "60k", "90k", "Austin", null)); // Missing companyId

        // Assert that IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            jobService.addJob(jobsToAdd);
        });

        // Verify that saveAll was *not* called
        verify(jobRepository, never()).saveAll(anyList());
    }


    @Test
    void testFindAllJobs_Success() {
        List<Job> jobs = Arrays.asList(job1, job2);
        when(jobRepository.findAll()).thenReturn(jobs);
        when(companyClient.getCompanyById("comp1")).thenReturn(company1);
        when(ratingClient.getRatingsByCompanyId("comp1")).thenReturn(ratings1); // Assume same ratings for simplicity

        List<JobDTO> result = jobService.findAllJobs();

        assertNotNull(result);
        assertEquals(2, result.size());
        // Check details of the first DTO
        assertEquals("job1", result.get(0).getId());
        assertEquals("Tech Corp", result.get(0).getCompany().getName());
        assertEquals(1, result.get(0).getRatings().size());
        assertEquals(4.5, result.get(0).getRatings().get(0).getRatings());
         // Check details of the second DTO
        assertEquals("job2", result.get(1).getId());
        assertEquals("Tech Corp", result.get(1).getCompany().getName());


        verify(jobRepository, times(1)).findAll();
        // CompanyClient might be called for each unique companyId found
        verify(companyClient, times(2)).getCompanyById("comp1");
        verify(ratingClient, times(2)).getRatingsByCompanyId("comp1");
    }

    @Test
    void testFindAllJobs_Empty() {
        when(jobRepository.findAll()).thenReturn(Collections.emptyList());

        List<JobDTO> result = jobService.findAllJobs();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jobRepository, times(1)).findAll();
        verify(companyClient, never()).getCompanyById(anyString());
        verify(ratingClient, never()).getRatingsByCompanyId(anyString());
    }

    @Test
    void testFindJobById_Success() {
        when(jobRepository.findById("job1")).thenReturn(Optional.of(job1));
        when(companyClient.getCompanyById("comp1")).thenReturn(company1);
        when(ratingClient.getRatingsByCompanyId("comp1")).thenReturn(ratings1);

        JobDTO result = jobService.findJobById("job1");

        assertNotNull(result);
        assertEquals("job1", result.getId());
        assertEquals("Software Engineer", result.getTitle());
        assertEquals("Tech Corp", result.getCompany().getName());
        assertEquals(1, result.getRatings().size());

        verify(jobRepository, times(1)).findById("job1");
        verify(companyClient, times(1)).getCompanyById("comp1");
        verify(ratingClient, times(1)).getRatingsByCompanyId("comp1");
    }

    @Test
    void testFindJobById_NotFound() {
        when(jobRepository.findById("unknownJob")).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> {
            jobService.findJobById("unknownJob");
        });

        verify(jobRepository, times(1)).findById("unknownJob");
        verify(companyClient, never()).getCompanyById(anyString());
        verify(ratingClient, never()).getRatingsByCompanyId(anyString());
    }

    @Test
    void testFindJobsByCompanyId_Success() {
        List<Job> companyJobs = Arrays.asList(job1, job2);
        when(jobRepository.findByCompanyId("comp1")).thenReturn(companyJobs);
        when(companyClient.getCompanyById("comp1")).thenReturn(company1);
        when(ratingClient.getRatingsByCompanyId("comp1")).thenReturn(ratings1);

        List<JobDTO> result = jobService.findJobsByCompanyId("comp1");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("job1", result.get(0).getId());
        assertEquals("job2", result.get(1).getId());
        assertEquals("Tech Corp", result.get(0).getCompany().getName()); // Company is the same for all

        verify(jobRepository, times(1)).findByCompanyId("comp1");
        verify(companyClient, times(1)).getCompanyById("comp1"); // Called once for the company
        verify(ratingClient, times(1)).getRatingsByCompanyId("comp1"); // Called once for the company
    }
    
    @Test
    void testFindJobsByCompanyId_NoJobsFound() {
        when(jobRepository.findByCompanyId("comp2")).thenReturn(Collections.emptyList());
        // Feign clients should not be called if no jobs are found

        List<JobDTO> result = jobService.findJobsByCompanyId("comp2");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(jobRepository, times(1)).findByCompanyId("comp2");
        verify(companyClient, never()).getCompanyById(anyString());
        verify(ratingClient, never()).getRatingsByCompanyId(anyString());
    }


    @Test
    void testDeleteJob_Success() {
        when(jobRepository.findById("job1")).thenReturn(Optional.of(job1));
        // Mock the delete operation (void method)
        doNothing().when(jobRepository).delete(job1);

        ResponseEntity<ApiResponse> response = jobService.deleteJob("job1");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Job Deleted Successfully"));
        assertTrue(response.getBody().getMessage().contains(job1.getTitle()));

        verify(jobRepository, times(1)).findById("job1");
        verify(jobRepository, times(1)).delete(job1);
    }

    @Test
    void testDeleteJob_NotFound() {
        when(jobRepository.findById("unknownJob")).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> {
            jobService.deleteJob("unknownJob");
        });

        verify(jobRepository, times(1)).findById("unknownJob");
        verify(jobRepository, never()).delete(any(Job.class));
    }

    @Test
    void testUpdateJob_Success() {
        Job updatedJobData = new Job("job1", "Senior Software Engineer", "Lead development", "100000", "150000", "Remote", "comp1");

        // Return the existing job first when findById is called
        when(jobRepository.findById("job1")).thenReturn(Optional.of(job1));
        // Return the updated job when save is called
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            // Simulate save updating the object state, return the argument passed to save
             Job jobToSave = invocation.getArgument(0);
             assertEquals("job1", jobToSave.getId()); // Ensure ID hasn't changed
             assertEquals("Senior Software Engineer", jobToSave.getTitle()); // Ensure title updated
             return jobToSave;
        });


        Job result = jobService.updateJob("job1", updatedJobData);

        assertNotNull(result);
        assertEquals("job1", result.getId());
        assertEquals("Senior Software Engineer", result.getTitle());
        assertEquals("Lead development", result.getDescription());
        assertEquals("100000", result.getMinSalary());
        assertEquals("150000", result.getMaxSalary());
        assertEquals("Remote", result.getLocation());
        assertEquals("comp1", result.getCompanyId());

        verify(jobRepository, times(1)).findById("job1");
        verify(jobRepository, times(1)).save(any(Job.class)); // Check that save was called
    }

    @Test
    void testUpdateJob_NotFound() {
         Job updatedJobData = new Job("unknownJob", "Senior Software Engineer", "Lead development", "100000", "150000", "Remote", "comp1");
        when(jobRepository.findById("unknownJob")).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> {
            jobService.updateJob("unknownJob", updatedJobData);
        });

        verify(jobRepository, times(1)).findById("unknownJob");
        verify(jobRepository, never()).save(any(Job.class));
    }
}