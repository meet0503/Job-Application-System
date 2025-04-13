package com.casestudy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.casestudy.entities.Rating;
import com.casestudy.exception.CompanyNotFoundException;
import com.casestudy.exception.RatingNotFoundException;
import com.casestudy.repository.RatingRepository;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    @Captor
    private ArgumentCaptor<Rating> ratingCaptor;

    private Rating testRating;
    private String companyId;
    private String ratingId;
    
    @BeforeEach
    void setUp() {
        companyId = "company123";
        ratingId = "rating123";
        testRating = new Rating(
            ratingId,
            "Great Company",
            "I really enjoyed working here",
            4.5,
            companyId
        );
    }

    @Test
    void testGetAllRatings_Success() {
        Rating rating2 = new Rating(
            "rating456",
            "Good Experience",
            "Friendly environment",
            4.0,
            companyId
        );
        List<Rating> expectedRatings = Arrays.asList(testRating, rating2);
        
        when(ratingRepository.findByCompanyId(companyId)).thenReturn(expectedRatings);
        
        List<Rating> actualRatings = ratingService.getAllRatings(companyId);
        
        assertEquals(2, actualRatings.size());
        assertEquals(expectedRatings, actualRatings);
        verify(ratingRepository, times(1)).findByCompanyId(companyId);
    }

    @Test
    void testGetAllRatings_EmptyList() {
        when(ratingRepository.findByCompanyId("nonExistentCompany")).thenReturn(Arrays.asList());
        
        List<Rating> actualRatings = ratingService.getAllRatings("nonExistentCompany");
        
        assertEquals(0, actualRatings.size());
        verify(ratingRepository, times(1)).findByCompanyId("nonExistentCompany");
    }

    @Test
    void testAddRating_Success() {
        Rating ratingToAdd = new Rating(
            null, // ID will be generated
            "New Rating",
            "Great feedback",
            5.0,
            null // CompanyId will be set in service
        );
        
        ratingService.addRating(companyId, ratingToAdd);
        
        verify(ratingRepository, times(1)).save(ratingCaptor.capture());
        Rating capturedRating = ratingCaptor.getValue();
        
        assertNotNull(capturedRating.getId()); // UUID should be generated
        assertEquals("New Rating", capturedRating.getTitle());
        assertEquals("Great feedback", capturedRating.getFeedback());
        assertEquals(5.0, capturedRating.getRatings());
        assertEquals(companyId, capturedRating.getCompanyId());
    }

    @Test
    void testAddRating_NullCompanyId() {
        Rating ratingToAdd = new Rating(
            null,
            "New Rating",
            "Great feedback",
            5.0,
            null
        );
        
        CompanyNotFoundException exception = assertThrows(CompanyNotFoundException.class, () -> {
            ratingService.addRating(null, ratingToAdd);
        });
        
        assertEquals("CompanyId is either blank or null", exception.getMessage());
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void testAddRating_EmptyCompanyId() {
        Rating ratingToAdd = new Rating(
            null,
            "New Rating",
            "Great feedback",
            5.0,
            null
        );
        
        CompanyNotFoundException exception = assertThrows(CompanyNotFoundException.class, () -> {
            ratingService.addRating("", ratingToAdd);
        });
        
        assertEquals("CompanyId is either blank or null", exception.getMessage());
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void testFindRatingById_Success() {
        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
        
        Rating foundRating = ratingService.findRatingById(ratingId);
        
        assertEquals(testRating, foundRating);
        verify(ratingRepository, times(1)).findById(ratingId);
    }

    @Test
    void testFindRatingById_NotFound() {
        when(ratingRepository.findById("nonExistentId")).thenReturn(Optional.empty());
        
        RatingNotFoundException exception = assertThrows(RatingNotFoundException.class, () -> {
            ratingService.findRatingById("nonExistentId");
        });
        
        assertEquals("No Ratings found with this id", exception.getMessage());
        verify(ratingRepository, times(1)).findById("nonExistentId");
    }

    @Test
    void testDeleteRating_Success() {
        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));
        
        Rating deletedRating = ratingService.deleteRating(ratingId);
        
        assertEquals(testRating, deletedRating);
        verify(ratingRepository, times(1)).findById(ratingId);
        verify(ratingRepository, times(1)).delete(testRating);
    }

    @Test
    void testDeleteRating_NotFound() {
        when(ratingRepository.findById("nonExistentId")).thenReturn(Optional.empty());
        
        RatingNotFoundException exception = assertThrows(RatingNotFoundException.class, () -> {
            ratingService.deleteRating("nonExistentId");
        });
        
        assertEquals("No Ratings found with this id", exception.getMessage());
        verify(ratingRepository, times(1)).findById("nonExistentId");
        verify(ratingRepository, never()).delete(any(Rating.class));
    }

    @Test
    void testUpdateRating_Success() {
        Rating existingRating = new Rating(
            ratingId,
            "Original Title",
            "Original Feedback",
            3.5,
            companyId
        );

        Rating updatedRatingData = new Rating(
            null,
            "Updated Title",
            "Updated Feedback",
            4.5,
            companyId
        );

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(existingRating));
        when(ratingRepository.save(any(Rating.class))).thenReturn(existingRating); 

        ratingService.updateRating(ratingId, updatedRatingData);

        verify(ratingRepository).findById(ratingId);
        verify(ratingRepository).save(ratingCaptor.capture());

        Rating savedRating = ratingCaptor.getValue();

        assertEquals("Updated Title", savedRating.getTitle());
        assertEquals("Updated Feedback", savedRating.getFeedback());
        assertEquals(4.5, savedRating.getRatings());
    }



    @Test
    void testUpdateRating_NotFound() {
        Rating updatedRatingData = new Rating(
            null,
            "Updated Title",
            "Updated Feedback",
            4.5,
            companyId
        );
        
        when(ratingRepository.findById("nonExistentId")).thenReturn(Optional.empty());
        
        RatingNotFoundException exception = assertThrows(RatingNotFoundException.class, () -> {
            ratingService.updateRating("nonExistentId", updatedRatingData);
        });
        
        assertEquals("No Ratings found with this id", exception.getMessage());
        verify(ratingRepository, times(1)).findById("nonExistentId");
        verify(ratingRepository, never()).save(any(Rating.class));
    }
}