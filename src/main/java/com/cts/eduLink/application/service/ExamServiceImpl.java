package com.cts.eduLink.application.service;

import com.cts.eduLink.application.dto.ExamCreationRequestDto;
import com.cts.eduLink.application.entity.Exam;
import com.cts.eduLink.application.entity.Course;
import com.cts.eduLink.application.projection.ExamProjection;
import com.cts.eduLink.application.repository.ExamRepository;
import com.cts.eduLink.application.repository.CourseRepository;
import com.cts.eduLink.application.classexception.ExamException;
import com.cts.eduLink.application.util.DtoMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ExamServiceImpl implements IExamService {
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    ExamRepository examRepository;
    public String createExam(ExamCreationRequestDto examCreationRequestDto) {
        log.info("Creating a new exam: {}", examCreationRequestDto.getExamName());
        Exam exam = DtoMapper.ExamDtoSeparator(examCreationRequestDto);
        Course course = courseRepository.findByCourseId(examCreationRequestDto.getCourseId()).orElseThrow(() -> new ExamException("Course not found with ID: " + examCreationRequestDto.getCourseId(), HttpStatus.NOT_FOUND));

        exam.setCourse(course);
        examRepository.save(exam);
        log.info("Exam created successfully: {}", examCreationRequestDto.getExamName());
        return "Exam created successfully with ID: " + exam.getExamId();
    }
    @Override
    @Transactional
    public String updateExam(Long examId, ExamCreationRequestDto dto) {
        log.info("Updating exam details for ID: {}", examId);
        Exam existingExam = examRepository.findById(examId).orElseThrow(() -> new ExamException("Exam not found with ID: " + examId, HttpStatus.NOT_FOUND));
        DtoMapper.updateExamFromDto(existingExam, dto);
        if (dto.getCourseId() != null) {
            Course course = courseRepository.findByCourseId(dto.getCourseId()).orElseThrow(() -> new ExamException("Course not found with ID: " + dto.getCourseId(), HttpStatus.NOT_FOUND));
            existingExam.setCourse(course);
        }
        examRepository.save(existingExam);

        log.info("Exam ID: {} updated successfully", examId);
        return "Exam updated successfully!";
    }
    @Override
    @Transactional
    public String patchExam(Long examId, java.util.Map<String, Object> updates) {
        log.info("Patch update initiated for Exam ID: {}", examId);
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new ExamException("Exam not found with ID: " + examId, HttpStatus.NOT_FOUND));
        updates.forEach((key, value) -> {
            if (value != null) {
                switch (key) {
                    case "examName":
                        exam.setExamName(value.toString());
                        break;
                    case "status":
                        exam.setExamStatus(value.toString());
                        break;
                    case "candidates":
                        exam.setCandidates(Integer.parseInt(value.toString()));
                        break;
                    case "courseId":
                        Long newCourseId = Long.valueOf(value.toString());
                        Course course = courseRepository.findByCourseId(newCourseId).orElseThrow(() -> new ExamException("Course not found with ID: " + newCourseId, HttpStatus.NOT_FOUND));
                        exam.setCourse(course);
                        break;
                }
            }
        });
        examRepository.save(exam);
        log.info("Exam ID: {} partially updated successfully", examId);
        return "Exam partially updated successfully!";
    }
    @Override
    @Transactional
    public String deleteExam(Long examId) {
        log.info("Deletion request initiated for Exam ID: {}", examId);
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new com.cts.eduLink.application.classexception.ExamException("Exam not found with ID: " + examId, HttpStatus.NOT_FOUND));
        examRepository.delete(exam);
        log.info("Exam ID: {} deleted successfully", examId);
        return "Exam deleted successfully!";
    }
    @Override
    public List<ExamProjection> findAllExams() throws ExamException {
        log.info("User has requested to display all exams via projection");
        List<ExamProjection> examProjections = examRepository.findAllExams();
        if (examProjections.isEmpty()) {
            log.error("No exams are currently available in the system");
            throw new ExamException("No exams found!", HttpStatus.NOT_FOUND);
        }
        log.info("Exam list accessed successfully. Total exams found: {}. First exam: {}", examProjections.size(), examProjections.get(0).getExamName());
        return examProjections;
    }
}