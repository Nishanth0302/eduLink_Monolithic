package com.cts.eduLink.application.serviceTest;

import com.cts.eduLink.application.dto.FacultyRegistrationDto;
import com.cts.eduLink.application.entity.AppUser;
import com.cts.eduLink.application.entity.Faculty;
import com.cts.eduLink.application.entity.Role;
import com.cts.eduLink.application.repository.FacultyRepository;
import com.cts.eduLink.application.repository.RoleRepository;
import com.cts.eduLink.application.service.AppUserServiceImpl;
import com.cts.eduLink.application.service.FacultyServiceImpl;
import com.cts.eduLink.application.service.IAppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FacultyServiceTest {

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AppUserServiceImpl appUserService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private FacultyServiceImpl facultyService;

    private FacultyRegistrationDto facultyRegistrationDto;
    private Role facultyRole;
    private Faculty facultyEntity;

    @BeforeEach
    public void setUp() {
        // 1. Initialize DTO
        facultyRegistrationDto = new FacultyRegistrationDto();
        facultyRegistrationDto.setUserName("Nikhil");
        facultyRegistrationDto.setUserEmail("nikhil@eduLink.com");
        facultyRegistrationDto.setPhoneNumber(1234567892L);
        facultyRegistrationDto.setFacultyGender("MALE");
        facultyRegistrationDto.setFacultyAddress("Pune, Maharashtra");

        // 2. Initialize Role
        facultyRole = new Role();
        facultyRole.setRoleName("FACULTY");

        // 3. Initialize Faculty Entity (This is what the repository expects)
        facultyEntity = new Faculty();
        facultyEntity.setId(1L);
        facultyEntity.setUserName(facultyRegistrationDto.getUserName());
        facultyEntity.setUserEmail(facultyRegistrationDto.getUserEmail());
        facultyEntity.setRole(facultyRole);
    }

    @Test
    public void facultyRegistration_200() {
        // Arrange
        // We mock roleRepository because the service likely looks up the role first
        when(roleRepository.findRoleByName("FACULTY")).thenReturn(Optional.of(facultyRole));

        // Mock password encoder if the service encodes password
        when(passwordEncoder.encode(any())).thenReturn("encryptedPassword");

        // FIX: Ensure you are stubbing with Faculty.class and returning a Faculty object
        when(facultyRepository.save(any(Faculty.class))).thenReturn(facultyEntity);

        // Act
        // Assuming the method returns the saved entity or a success message
        var result = facultyService.registerFaculty(facultyRegistrationDto);

        // Assert
        assertNotNull(result, "The registration result should not be null");
        verify(facultyRepository, times(1)).save(any(Faculty.class));
        verify(roleRepository, times(1)).findRoleByName(anyString());
    }

    @Test
    public void facultyRegistration_409_RoleNotFound() {
        // Arrange: Simulate the role not existing in the DB
        when(roleRepository.findRoleByName("FACULTY")).thenReturn(Optional.empty());

        // Act & Assert: Assuming your service throws an exception when role is missing
        assertThrows(RuntimeException.class, () -> {
            facultyService.registerFaculty(facultyRegistrationDto);
        });
    }
}