package com.casestudy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.casestudy.entities.Company;
import com.casestudy.exception.CompanyNotFoundException;
import com.casestudy.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class) 
class CompanyServiceImplTest {

    @Mock // Mock the repository dependency
    private CompanyRepository companyRepository;

    @InjectMocks // Inject the mocks into the service instance
    private CompanyServiceImpl companyService;

    private Company company1;
    private Company company2;

    @BeforeEach // Setup common test data before each test
    void setUp() {
        company1 = new Company("comp1", "Tech Solutions", "Innovative tech company");
        company2 = new Company("comp2", "Global Biz", "International business services");
    }

    @Test
    void testAddCompany_Success() {
        // Arrange
        Company newCompany1 = new Company(null, "New Corp 1", "Desc 1");
        Company newCompany2 = new Company(null, "New Corp 2", "Desc 2");
        List<Company> companiesToAdd = Arrays.asList(newCompany1, newCompany2);

        // Act
        // No return value for saveAll, just verify interaction
        companyService.addCompany(companiesToAdd);

        // Assert
        ArgumentCaptor<List<Company>> captor = ArgumentCaptor.forClass(List.class);
        verify(companyRepository, times(1)).saveAll(captor.capture()); // Verify saveAll was called once

        List<Company> savedCompanies = captor.getValue();
        assertEquals(2, savedCompanies.size());
        // Verify IDs were generated for both companies
        assertNotNull(savedCompanies.get(0).getId());
        assertNotNull(savedCompanies.get(1).getId());
        assertEquals("New Corp 1", savedCompanies.get(0).getName());
        assertEquals("New Corp 2", savedCompanies.get(1).getName());
    }

    @Test
    void testFindAllCompanies_Success() {
        // Arrange
        List<Company> companies = Arrays.asList(company1, company2);
        when(companyRepository.findAll()).thenReturn(companies); // Mock repository response

        // Act
        List<Company> result = companyService.findAllCompanies();

        // Assert
        assertNotNull(result); //
        assertEquals(2, result.size()); //
        assertEquals("comp1", result.get(0).getId());
        assertEquals("comp2", result.get(1).getId());
        verify(companyRepository, times(1)).findAll(); // Verify findAll was called
    }

    @Test
    void testFindAllCompanies_Empty() {
        // Arrange
        when(companyRepository.findAll()).thenReturn(Collections.emptyList()); // Mock empty list response

        // Act
        List<Company> result = companyService.findAllCompanies();

        // Assert
        assertNotNull(result); //
        assertTrue(result.isEmpty()); //
        verify(companyRepository, times(1)).findAll(); //
    }

    @Test
    void testFindCompanyById_Success() {
        // Arrange
        when(companyRepository.findById("comp1")).thenReturn(Optional.of(company1)); // Mock repository response

        // Act
        Company result = companyService.findCompanyById("comp1");

        // Assert
        assertNotNull(result);
        assertEquals("comp1", result.getId());
        assertEquals("Tech Solutions", result.getName());
        verify(companyRepository, times(1)).findById("comp1"); // Verify findById was called
    }

    @Test
    void testFindCompanyById_NotFound() {
        // Arrange
        when(companyRepository.findById("unknownId")).thenReturn(Optional.empty()); // Mock empty optional response

        // Act & Assert
        CompanyNotFoundException exception = assertThrows(CompanyNotFoundException.class, () -> { // Assert exception is thrown
            companyService.findCompanyById("unknownId");
        });
        assertEquals(CompanyServiceImpl.COMPANY_NOT_FOUND, exception.getMessage());
        verify(companyRepository, times(1)).findById("unknownId"); // Verify findById was called
    }

    @Test
    void testDeleteCompany_Success() {
        // Arrange
        when(companyRepository.findById("comp1")).thenReturn(Optional.of(company1)); // Mock findById
        doNothing().when(companyRepository).delete(company1); // Mock delete action

        // Act
        Company deletedCompany = companyService.deleteCompany("comp1");

        // Assert
        assertNotNull(deletedCompany); //
        assertEquals(company1.getId(), deletedCompany.getId());
        assertEquals(company1.getName(), deletedCompany.getName());
        verify(companyRepository, times(1)).findById("comp1"); // Verify findById was called
        verify(companyRepository, times(1)).delete(company1); // Verify delete was called
    }

    @Test
    void testDeleteCompany_NotFound() {
        // Arrange
        when(companyRepository.findById("unknownId")).thenReturn(Optional.empty()); // Mock findById returning empty

        // Act & Assert
        assertThrows(CompanyNotFoundException.class, () -> { // Assert exception is thrown
            companyService.deleteCompany("unknownId");
        });
        verify(companyRepository, times(1)).findById("unknownId"); // Verify findById was called
        verify(companyRepository, never()).delete(any(Company.class)); // Verify delete was never called
    }

    @Test
    void testUpdateCompany_Success() {
        // Arrange
        String companyId = "comp1";
        Company existingCompany = new Company(companyId, "Old Name", "Old Desc");
        Company updatedCompanyData = new Company(null, "New Name", "New Desc"); // ID in updated data is ignored

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany)); // Mock findById

        // Mock save to return the updated company
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
            Company savedCompany = invocation.getArgument(0);
            // Assert that the company being saved has the correct ID and updated fields
            assertEquals(companyId, savedCompany.getId());
            assertEquals(updatedCompanyData.getName(), savedCompany.getName());
            assertEquals(updatedCompanyData.getDescription(), savedCompany.getDescription());
            return savedCompany; // Return the saved (updated) company
        });

        // Act
        Company result = companyService.updateCompany(companyId, updatedCompanyData);

        // Assert
        assertNotNull(result); //
        assertEquals(companyId, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());

        verify(companyRepository, times(1)).findById(companyId); // Verify findById called
        verify(companyRepository, times(1)).save(any(Company.class)); // Verify save called
    }

    @Test
    void testUpdateCompany_NotFound() {
        // Arrange
        String companyId = "unknownId";
        Company updatedCompanyData = new Company(null, "New Name", "New Desc");
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty()); // Mock findById returning empty

        // Act & Assert
        assertThrows(CompanyNotFoundException.class, () -> { // Assert exception is thrown
            companyService.updateCompany(companyId, updatedCompanyData);
        });
        verify(companyRepository, times(1)).findById(companyId); // Verify findById called
        verify(companyRepository, never()).save(any(Company.class)); // Verify save never called
    }
}