package com.cts.eduLink.application.serviceTest;

import com.cts.eduLink.application.classexception.ExamException;
import com.cts.eduLink.application.dto.ExamCreationRequestDto;
import com.cts.eduLink.application.entity.Course;
import com.cts.eduLink.application.entity.Exam;
import com.cts.eduLink.application.projection.ExamProjection;
import com.cts.eduLink.application.repository.CourseRepository;
import com.cts.eduLink.application.repository.ExamRepository;
import com.cts.eduLink.application.service.ExamServiceImpl;
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
class ExamServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ExamRepository examRepository;

    @InjectMocks
    private ExamServiceImpl examService;

    private Exam exam;
    private Course course;
    private ExamCreationRequestDto examDto;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setCourseId(1L);
        course.setCourseTitle("Java Programming");

        exam = new Exam();
        exam.setExamId(101L);
        exam.setExamName("Midterm");
        exam.setCourse(course);

        examDto = new ExamCreationRequestDto();
        examDto.setExamName("Midterm");
        examDto.setCourseId(1L);
    }

    @Test
    @DisplayName("Create Exam - Success")
    void createExam_Success() {
        when(courseRepository.findByCourseId(1L)).thenReturn(Optional.of(course));

        String result = examService.createExam(examDto);

        assertThat(result).contains("Exam created successfully");
        verify(examRepository, times(1)).save(any(Exam.class));
    }

    @Test
    @DisplayName("Create Exam - Course Not Found")
    void createExam_CourseNotFound() {
        when(courseRepository.findByCourseId(1L)).thenReturn(Optional.empty());

        ExamException exception = assertThrows(ExamException.class, () ->
                examService.createExam(examDto));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).contains("Course not found");
    }

    @Test
    @DisplayName("Update Exam - Success")
    void updateExam_Success() {
        when(examRepository.findById(101L)).thenReturn(Optional.of(exam));
        when(courseRepository.findByCourseId(1L)).thenReturn(Optional.of(course));

        String result = examService.updateExam(101L, examDto);

        assertThat(result).isEqualTo("Exam updated successfully!");
        verify(examRepository).save(exam);
    }

    @Test
    @DisplayName("Patch Exam - Success updating specific fields")
    void patchExam_Success() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("examName", "Final Exam");
        updates.put("candidates", 50);

        when(examRepository.findById(101L)).thenReturn(Optional.of(exam));

        String result = examService.patchExam(101L, updates);

        assertThat(result).isEqualTo("Exam partially updated successfully!");
        assertThat(exam.getExamName()).isEqualTo("Final Exam");
        verify(examRepository).save(exam);
    }

    @Test
    @DisplayName("Delete Exam - Success")
    void deleteExam_Success() {
        when(examRepository.findById(101L)).thenReturn(Optional.of(exam));

        String result = examService.deleteExam(101L);

        assertThat(result).isEqualTo("Exam deleted successfully!");
        verify(examRepository).delete(exam);
    }

    @Test
    @DisplayName("Find All Exams - Success via Projection")
    void findAllExams_Success() {
        ExamProjection projection = mock(ExamProjection.class);
        when(projection.getExamName()).thenReturn("Midterm");
        when(examRepository.findAllExams()).thenReturn(List.of(projection));

        List<ExamProjection> result = examService.findAllExams();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExamName()).isEqualTo("Midterm");
    }

    @Test
    @DisplayName("Find All Exams - Throws Exception when empty")
    void findAllExams_NotFound() {
        when(examRepository.findAllExams()).thenReturn(Collections.emptyList());

        ExamException exception = assertThrows(ExamException.class, () ->
                examService.findAllExams());

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("No exams found!");
    }
}