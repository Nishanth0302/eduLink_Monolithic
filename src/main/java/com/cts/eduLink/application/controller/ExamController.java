package com.cts.eduLink.application.controller;

import com.cts.eduLink.application.classexception.ExamException;
import com.cts.eduLink.application.dto.ExamCreationRequestDto;
import com.cts.eduLink.application.entity.Exam;
import com.cts.eduLink.application.projection.ExamProjection;
import com.cts.eduLink.application.service.IExamService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exam")
@Slf4j
public class ExamController {

    private final IExamService examService;

    public ExamController(IExamService examService) {
        this.examService = examService;
    }

    @PreAuthorize("hasRole('FACULTY')")
    @PostMapping("/register")
    public ResponseEntity<String> createExam(@Valid @RequestBody ExamCreationRequestDto request) {
        log.info("Creating a new exam: {}", request.getExamName());
        examService.createExam(request);
        return ResponseEntity.status(HttpStatus.OK).body("created successfully");
    }

    @PreAuthorize("hasRole('FACULTY')")
    @PutMapping("/update/{examId}")
    public ResponseEntity<String> updateExam(@Valid @PathVariable Long examId, @RequestBody ExamCreationRequestDto request) {
        log.info("Received request to update exam with ID: {}", examId);
        String response = examService.updateExam(examId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('FACULTY')")
    @PatchMapping("/patch/{examId}")
    public ResponseEntity<String> patchExam(@Valid @PathVariable Long examId, @RequestBody Map<String, Object> updates) {
        log.info("Patch update for examId: {} has been initiated successfully", examId);
        return ResponseEntity.status(200).body(examService.patchExam(examId, updates));
    }

    @PreAuthorize("hasRole('FACULTY')")
    @DeleteMapping("/delete/{examId}")
    public ResponseEntity<String> deleteExam(@Valid  @PathVariable Long examId) {
        log.info("Delete operation for examId: {} has been initiated successfully", examId);
        return ResponseEntity.status(200).body(examService.deleteExam(examId));
    }

    @PreAuthorize("hasAnyRole('STUDENT','FACULTY')")
    @GetMapping("/allExams")
    public ResponseEntity<List<ExamProjection>> getAllExams() throws ExamException {
        log.info("Controller: Request received to fetch all exam projections");
        return ResponseEntity.ok(examService.findAllExams());
    }

}