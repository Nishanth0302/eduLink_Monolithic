package com.cts.eduLink.application.serviceTest;

import com.cts.eduLink.application.classexception.CourseException;
import com.cts.eduLink.application.classexception.FacultyException;
import com.cts.eduLink.application.classexception.StudentException;
import com.cts.eduLink.application.dto.CourseEnrollmentDto;
import com.cts.eduLink.application.dto.CourseRegistrationDto;
import com.cts.eduLink.application.entity.Course;
import com.cts.eduLink.application.entity.Faculty;
import com.cts.eduLink.application.entity.Student;
import com.cts.eduLink.application.projection.CourseDetailByIdProjection;
import com.cts.eduLink.application.projection.CourseProjection;
import com.cts.eduLink.application.repository.CourseRepository;
import com.cts.eduLink.application.repository.FacultyRepository;
import com.cts.eduLink.application.repository.StudentRepository;
import com.cts.eduLink.application.service.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private FacultyRepository facultyRepository;
    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course;
    private Faculty faculty;
    private Student student;
    private CourseRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        faculty = new Faculty();
        faculty.setFacultyId(1L);
        faculty.setCourseSet(new HashSet<>());

        student = new Student();
        student.setStudentId(10L);
        student.setCourseSet(new HashSet<>());

        course = new Course();
        course.setCourseId(101L);
        course.setCourseTitle("Java Microservices");
        course.setFacultySet(new HashSet<>());
        course.setStudentSet(new HashSet<>());

        registrationDto = new CourseRegistrationDto();
        registrationDto.setFacultyId(1L);
        registrationDto.setCourseTitle("Java Microservices");
    }

    @Test
    @DisplayName("Register Course - Success")
    void registerCourse_Success() throws FacultyException {

        when(facultyRepository.findFacultyById(1L)).thenReturn(Optional.of(faculty));

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course courseToSave = invocation.getArgument(0);
            courseToSave.setCourseId(101L); // Assign the ID the test expects
            return courseToSave;
        });

        String result = courseService.registerCourse(registrationDto);

        assertThat(result).isEqualTo("Course has registered successFully with course Id: 101");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Register Course - Faculty Not Found")
    void registerCourse_FacultyNotFound() {
        when(facultyRepository.findFacultyById(1L)).thenReturn(Optional.empty());

        assertThrows(FacultyException.class, () -> courseService.registerCourse(registrationDto));
    }

    @Test
    @DisplayName("Patch Course - Success updating status")
    void patchCourse_Success() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("courseStatus", "ARCHIVED");

        when(courseRepository.findByCourseId(101L)).thenReturn(Optional.of(course));

        String result = courseService.patchCourse(101L, updates);

        assertThat(result).contains("partially updated");
        assertThat(course.getCourseStatus()).isEqualTo("ARCHIVED");
    }

    @Test
    @DisplayName("Delete Course - Soft Delete (Inactive status)")
    void deleteCourse_Success() {
        when(courseRepository.findByCourseId(101L)).thenReturn(Optional.of(course));

        String result = courseService.deleteCourse(101L);

        assertThat(result).contains("deleted successfully");
        assertThat(course.getCourseStatus()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("Find Course Details By ID - Success")
    void findCourseDetailsById_Success() {
        CourseDetailByIdProjection projection = mock(CourseDetailByIdProjection.class);
        when(courseRepository.findCourseDetailsById(101L)).thenReturn(Optional.of(projection));

        CourseDetailByIdProjection result = courseService.findCourseDetailsById(101L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Course Enrollment - Success")
    void courseEnrollmentRequest_Success() throws StudentException, CourseException {
        CourseEnrollmentDto enrollmentDto = new CourseEnrollmentDto();
        enrollmentDto.setStudentId(10L);
        enrollmentDto.setCourseId(101L);

        when(studentRepository.findStudentById(10L)).thenReturn(Optional.of(student));
        when(courseRepository.findCourseById(101L)).thenReturn(Optional.of(course));

        String result = courseService.courseEnrollmentRequest(enrollmentDto);

        assertThat(result).isEqualTo("Enrolled SuccessFull!");
        assertThat(student.getCourseSet()).contains(course);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Course Enrollment - Already Registered Conflict")
    void courseEnrollmentRequest_Conflict() {
        CourseEnrollmentDto enrollmentDto = new CourseEnrollmentDto();
        enrollmentDto.setStudentId(10L);
        enrollmentDto.setCourseId(101L);

        student.getCourseSet().add(course); // Pre-enrolling

        when(studentRepository.findStudentById(10L)).thenReturn(Optional.of(student));
        when(courseRepository.findCourseById(101L)).thenReturn(Optional.of(course));

        CourseException exception = assertThrows(CourseException.class, () ->
                courseService.courseEnrollmentRequest(enrollmentDto));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Update Course Rating - Success")
    void updateCourseRating_Success() {
        course.setCourseRating(4.0);
        course.setTotalCourseRatingCount(10L);
        when(courseRepository.findCourseById(101L)).thenReturn(Optional.of(course));

        String result = courseService.updateCourseRating(101L, 5.0);

        assertThat(result).isEqualTo("Thanks for you feedBack!");
        assertThat(course.getTotalCourseRatingCount()).isEqualTo(11L);
    }

    @Test
    @DisplayName("Find All Available Courses - Throws Exception when empty")
    void findAllAvailableCourse_NotFound() {
        when(courseRepository.findAllAvailableCourse()).thenReturn(Collections.emptyList());

        assertThrows(CourseException.class, () -> courseService.findAllAvailableCourse());
    }
}