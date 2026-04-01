package com.cts.eduLink.application.serviceTest;

import com.cts.eduLink.application.classexception.FacultyException;
import com.cts.eduLink.application.dto.FacultyDashboardDto;
import com.cts.eduLink.application.dto.FacultyRegistrationDto;
import com.cts.eduLink.application.entity.*;
import com.cts.eduLink.application.projection.FacultyDetailProjection;
import com.cts.eduLink.application.projection.FacultyProjection;
import com.cts.eduLink.application.repository.CourseRepository;
import com.cts.eduLink.application.repository.FacultyRepository;
import com.cts.eduLink.application.repository.RoleRepository;
import com.cts.eduLink.application.service.AppUserServiceImpl;
import com.cts.eduLink.application.service.FacultyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FacultyServiceTest {

    @Mock
    private FacultyRepository facultyRepository;
    @Mock
    private AppUserServiceImpl appUserService;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private FacultyServiceImpl facultyService;

    private Faculty faculty;
    private AppUser appUser;
    private FacultyRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        appUser.setUserEmail("test@edulink.com");

        faculty = new Faculty();
        faculty.setFacultyId(101L);
        faculty.setFacultyRating(4.0);
        faculty.setTotalFacultyRatingCount(10L);
        faculty.setAppUser(appUser);
        faculty.setCourseSet(new HashSet<>());

        registrationDto = new FacultyRegistrationDto();
        registrationDto.setUserEmail("test@edulink.com");
        registrationDto.setUserName("Test Faculty");
        registrationDto.setPassword("password123");
    }

    @Test
    @DisplayName("Register Faculty - Success")
    void registerFaculty_Success() {
        Role role = new Role();
        role.setRoleName("FACULTY"); // Fixed: matches your Role entity field

        when(roleRepository.findRoleByName("FACULTY")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        String result = facultyService.registerFaculty(registrationDto);

        assertThat(result).contains("You have registered SuccessFully");
        verify(appUserService, times(1)).registerAppUser(any(AppUser.class));
        verify(facultyRepository, times(1)).save(any(Faculty.class));
    }

    @Test
    @DisplayName("Update Faculty - Success")
    void updateFaculty_Success() {
        when(facultyRepository.findFacultyById(101L)).thenReturn(Optional.of(faculty));

        String result = facultyService.updateFaculty(101L, registrationDto);

        assertThat(result).isEqualTo("Faculty profile updated successfully!");
        verify(facultyRepository).save(faculty);
    }

    @Test
    @DisplayName("Filter Faculty By Rating - Success")
    void filterFacultyByRating_Success() {
        FacultyDetailProjection projection = mock(FacultyDetailProjection.class);
        when(projection.getFacultyName()).thenReturn("Dr. Smith");
        when(facultyRepository.filterFacultyByRating(4)).thenReturn(List.of(projection));

        List<FacultyDetailProjection> result = facultyService.filterFacultyByRating(4);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getFacultyName()).isEqualTo("Dr. Smith");
    }

    @Test
    @DisplayName("Filter Faculty By Rating - Throws Exception when empty")
    void filterFacultyByRating_NotFound() {
        when(facultyRepository.filterFacultyByRating(5)).thenReturn(Collections.emptyList());

        FacultyException exception = assertThrows(FacultyException.class, () ->
                facultyService.filterFacultyByRating(5));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Update Rating - Success")
    void updateFacultyRating_Success() {
        when(facultyRepository.findFacultyById(101L)).thenReturn(Optional.of(faculty));

        String result = facultyService.updateFacultyRating(101L, 5.0);

        assertThat(result).isEqualTo("Thanks for you feedBack!");
        assertThat(faculty.getTotalFacultyRatingCount()).isEqualTo(11L);
    }

    @Test
    @DisplayName("Patch Faculty - Success")
    void patchFaculty_Success() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("phoneNumber", "9876543210");
        updates.put("facultyAddress", "New York");

        when(facultyRepository.findFacultyById(101L)).thenReturn(Optional.of(faculty));

        String result = facultyService.patchFaculty(101L, updates);

        assertThat(result).contains("partially updated");
        assertThat(faculty.getFacultyAddress()).isEqualTo("New York");
        assertThat(faculty.getAppUser().getPhoneNumber()).isEqualTo(9876543210L);
    }

    @Test
    @DisplayName("Delete Faculty - Success")
    void deleteFaculty_Success() {
        when(facultyRepository.findFacultyById(101L)).thenReturn(Optional.of(faculty));

        String result = facultyService.deleteFaculty(101L);

        assertThat(result).isEqualTo("Faculty record deleted successfully!");
        verify(facultyRepository).delete(faculty);
    }

    @Test
    @DisplayName("Get Faculty Dashboard - Success")
    void getFacultyDashboard_Success() {
        when(courseRepository.getFacultyCourseCount(101L)).thenReturn(5);
        when(facultyRepository.getUpcomingExamsCount(101L)).thenReturn(2);

        FacultyDashboardDto result = facultyService.getFacultyDashboard(101L);

        assertThat(result.getCourseCount()).isEqualTo(5);
        assertThat(result.getUpcomingExamsCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Get Upcoming Exams - Success")
    void getupComingExams_Success() {
        Exam exam = new Exam();
        when(facultyRepository.findUpcomingExamsByFacultyId(101L)).thenReturn(List.of(exam));

        List<Exam> result = facultyService.getupComingExams(101L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Get Faculty Profile - Success")
    void getFacultyProfile_Success() {
        FacultyProjection projection = mock(FacultyProjection.class);
        when(facultyRepository.findFacultyProfile(101L)).thenReturn(Optional.of(projection));

        Optional<FacultyProjection> result = facultyService.getFacultyProfile(101L);

        assertThat(result).isPresent();
    }
}