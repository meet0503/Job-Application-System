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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.casestudy.entities.Rating;
import com.casestudy.entities.external.TokenValidationResponse;
import com.casestudy.exception.CompanyNotFoundException;
import com.casestudy.exception.GlobalExceptionHandler;
import com.casestudy.exception.RatingNotFoundException;
import com.casestudy.feign.AuthServiceClient;
import com.casestudy.security.JwtAuthenticationFilter;
import com.casestudy.security.SecurityConfiguration;
import com.casestudy.service.RatingServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(RatingController.class)
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RatingServiceImpl ratingService;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    private Rating testRating1;
    private Rating testRating2;
    private List<Rating> testRatings;
    private String companyId;

    private static final String RATING_NOT_FOUND = "No Ratings found with this id";
    private static final String ADMIN_TOKEN = "Bearer admintoken";
    private static final String USER_TOKEN = "Bearer usertoken";
    private static final String VALID_TOKEN = "Bearer validtoken";
    private static final String INVALID_TOKEN = "Bearer invalidtoken";

    @BeforeEach
    void setUp() {
        companyId = "company123";
        
        // Create test ratings
        testRating1 = new Rating(
            "rating123",
            "Great Company",
            "I really enjoyed working here",
            4.5,
            companyId
        );

        testRating2 = new Rating(
            "rating456",
            "Good Experience",
            "Friendly environment",
            4.0,
            companyId
        );
        
        testRatings = Arrays.asList(testRating1, testRating2);
        
        // Configure token validation responses
        when(authServiceClient.validateToken(ADMIN_TOKEN)).thenReturn(new TokenValidationResponse(true, "ADMIN"));
        when(authServiceClient.validateToken(USER_TOKEN)).thenReturn(new TokenValidationResponse(true, "USER"));
        when(authServiceClient.validateToken(VALID_TOKEN)).thenReturn(new TokenValidationResponse(true, "USER"));
        when(authServiceClient.validateToken(INVALID_TOKEN)).thenReturn(new TokenValidationResponse(false, null));
    }

    // ----- GET /ratings Tests -----
    
    @Test
    void testFindAllRatings_Success() throws Exception {
        // Setup
        when(ratingService.getAllRatings(companyId)).thenReturn(testRatings);

        // Execute & Verify
        mockMvc.perform(get("/ratings")
                .param("companyId", companyId)
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("rating123"))
                .andExpect(jsonPath("$[0].title").value("Great Company"))
                .andExpect(jsonPath("$[0].feedback").value("I really enjoyed working here"))
                .andExpect(jsonPath("$[0].ratings").value(4.5))
                .andExpect(jsonPath("$[0].companyId").value(companyId))
                .andExpect(jsonPath("$[1].id").value("rating456"));

        verify(ratingService, times(1)).getAllRatings(companyId);
    }
    
    @Test
    void testFindAllRatings_EmptyList() throws Exception {
        // Setup
        when(ratingService.getAllRatings("nonExistentCompany")).thenReturn(Collections.emptyList());

        // Execute & Verify
        mockMvc.perform(get("/ratings")
                .param("companyId", "nonExistentCompany")
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(ratingService, times(1)).getAllRatings("nonExistentCompany");
    }

    @Test
    void testFindAllRatings_Unauthorized_InvalidToken() throws Exception {
        // Execute & Verify
        mockMvc.perform(get("/ratings")
                .param("companyId", companyId)
                .header("Authorization", INVALID_TOKEN))
                .andExpect(status().isUnauthorized());

        verify(ratingService, never()).getAllRatings(anyString());
    }
    
    @Test
    void testFindAllRatings_Unauthorized_NoToken() throws Exception {
        // Execute & Verify
        mockMvc.perform(get("/ratings")
        		.header("Authorization", INVALID_TOKEN)
                .param("companyId", companyId))
                .andExpect(status().isUnauthorized());

        verify(ratingService, never()).getAllRatings(anyString());
    }

    // ----- POST /ratings Tests -----
    
    @Test
    void testCreateRating_Success() throws Exception {
        // Setup
        ArgumentCaptor<Rating> ratingCaptor = ArgumentCaptor.forClass(Rating.class);
        doNothing().when(ratingService).addRating(eq(companyId), ratingCaptor.capture());

        // Execute & Verify
        mockMvc.perform(post("/ratings")
                .param("companyId", companyId)
                .header("Authorization", VALID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRating1)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Rating Created Successfully"));

        verify(ratingService, times(1)).addRating(eq(companyId), any(Rating.class));
        
        // Verify rating passed to service
        Rating capturedRating = ratingCaptor.getValue();
        assertEquals(testRating1.getTitle(), capturedRating.getTitle());
        assertEquals(testRating1.getFeedback(), capturedRating.getFeedback());
        assertEquals(testRating1.getRatings(), capturedRating.getRatings());
    }
    
    @Test
    void testCreateRating_MissingCompanyId() throws Exception {
        // Setup
        doThrow(new CompanyNotFoundException("CompanyId is either blank or null"))
            .when(ratingService).addRating(eq(""), any(Rating.class));

        // Execute & Verify
        mockMvc.perform(post("/ratings")
                .param("companyId", "")
                .header("Authorization", VALID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRating1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("CompanyId is either blank or null"));

        verify(ratingService, times(1)).addRating(eq(""), any(Rating.class));
    }

    // ----- GET /ratings/{ratingId} Tests -----
    
    @Test
    void testFindRatingById_Success() throws Exception {
        // Setup
        String ratingId = "rating123";
        when(ratingService.findRatingById(ratingId)).thenReturn(testRating1);

        // Execute & Verify
        mockMvc.perform(get("/ratings/{ratingId}", ratingId)
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ratingId))
                .andExpect(jsonPath("$.title").value("Great Company"))
                .andExpect(jsonPath("$.feedback").value("I really enjoyed working here"))
                .andExpect(jsonPath("$.ratings").value(4.5))
                .andExpect(jsonPath("$.companyId").value(companyId));

        verify(ratingService, times(1)).findRatingById(ratingId);
    }

    @Test
    void testFindRatingById_NotFound() throws Exception {
        // Setup
        String ratingId = "nonexistent";
        when(ratingService.findRatingById(ratingId)).thenThrow(new RatingNotFoundException(RATING_NOT_FOUND));

        // Execute & Verify
        mockMvc.perform(get("/ratings/{ratingId}", ratingId)
                .header("Authorization", VALID_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(RATING_NOT_FOUND));

        verify(ratingService, times(1)).findRatingById(ratingId);
    }

    // ----- PUT /ratings/{ratingId} Tests -----
    
    @Test
    void testUpdateRating_Admin_Success() throws Exception {
        // Setup
        String ratingId = "rating123";
        Rating updatedRating = new Rating(
            ratingId,
            "Updated Title",
            "Updated Feedback",
            5.0,
            companyId
        );
        
        ArgumentCaptor<Rating> ratingCaptor = ArgumentCaptor.forClass(Rating.class);
        when(ratingService.updateRating(eq(ratingId), ratingCaptor.capture())).thenReturn(updatedRating);

        // Execute & Verify
        mockMvc.perform(put("/ratings/{ratingId}", ratingId)
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRating)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ratingId))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.feedback").value("Updated Feedback"))
                .andExpect(jsonPath("$.ratings").value(5.0))
                .andExpect(jsonPath("$.companyId").value(companyId));

        verify(ratingService, times(1)).updateRating(eq(ratingId), any(Rating.class));
        
        // Verify rating passed to service
        Rating capturedRating = ratingCaptor.getValue();
        assertEquals(updatedRating.getTitle(), capturedRating.getTitle());
        assertEquals(updatedRating.getFeedback(), capturedRating.getFeedback());
        assertEquals(updatedRating.getRatings(), capturedRating.getRatings());
        assertEquals(updatedRating.getCompanyId(), capturedRating.getCompanyId());
    }
    
    @Test
    void testUpdateRating_Admin_NotFound() throws Exception {
        // Setup
        String ratingId = "nonexistent";
        Rating updatedRating = new Rating(
            ratingId,
            "Doesn't Matter",
            "This rating doesn't exist",
            5.0,
            companyId
        );
        
        when(ratingService.updateRating(eq(ratingId), any(Rating.class)))
            .thenThrow(new RatingNotFoundException(RATING_NOT_FOUND));

        // Execute & Verify
        mockMvc.perform(put("/ratings/{ratingId}", ratingId)
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRating)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(RATING_NOT_FOUND));

        verify(ratingService, times(1)).updateRating(eq(ratingId), any(Rating.class));
    }

    @Test
    void testUpdateRating_User_Forbidden() throws Exception {
        // Setup
        String ratingId = "rating123";
        Rating updatedRating = new Rating(
            ratingId,
            "Updated Title",
            "Updated Feedback",
            5.0,
            companyId
        );

        // Execute & Verify
        mockMvc.perform(put("/ratings/{ratingId}", ratingId)
                .header("Authorization", USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRating)))
                .andExpect(status().isForbidden());

        verify(ratingService, never()).updateRating(anyString(), any(Rating.class));
    }

    // ----- DELETE /ratings/{ratingId} Tests -----
    
    @Test
    void testDeleteRating_Admin_Success() throws Exception {
        // Setup
        String ratingId = "rating123";
        when(ratingService.deleteRating(ratingId)).thenReturn(testRating1);

        String expectedMessage = "Rating with Title " + testRating1.getTitle() + " is deleted successfully";

        // Execute & Verify
        mockMvc.perform(delete("/ratings/{ratingId}", ratingId)
                .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMessage));

        verify(ratingService, times(1)).deleteRating(ratingId);
    }

    @Test
    void testDeleteRating_Admin_NotFound() throws Exception {
        // Setup
        String ratingId = "nonexistent";
        when(ratingService.deleteRating(ratingId)).thenThrow(new RatingNotFoundException(RATING_NOT_FOUND));

        // Execute & Verify
        mockMvc.perform(delete("/ratings/{ratingId}", ratingId)
                .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(RATING_NOT_FOUND));

        verify(ratingService, times(1)).deleteRating(ratingId);
    }

    @Test
    void testDeleteRating_User_Forbidden() throws Exception {
        // Setup
        String ratingId = "rating123";

        // Execute & Verify
        mockMvc.perform(delete("/ratings/{ratingId}", ratingId)
                .header("Authorization", USER_TOKEN))
                .andExpect(status().isForbidden());

        verify(ratingService, never()).deleteRating(anyString());
    }

    // Helper methods
    private void assertEquals(Object expected, Object actual) {
        if ((expected == null && actual != null) || 
            (expected != null && !expected.equals(actual))) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }
}