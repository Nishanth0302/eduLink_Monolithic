package com.cts.eduLink.application.controller;

import com.cts.eduLink.application.dto.FacultyRegistrationDto;
import com.cts.eduLink.application.dto.FacultyDashboardDto;
import com.cts.eduLink.application.entity.Exam;
import com.cts.eduLink.application.projection.FacultyProjection;
import com.cts.eduLink.application.projection.FacultyDetailProjection;
import com.cts.eduLink.application.service.IFacultyService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/faculty")
@AllArgsConstructor
@Slf4j
public class FacultyController {

    private final IFacultyService facultyService;

    @PostMapping("/register")
    public ResponseEntity<String> registerFaculty(@Valid @RequestBody FacultyRegistrationDto facultyRegistrationDto){
        log.info("{} has initiated the registration as a Faculty",facultyRegistrationDto.getUserEmail());
        return ResponseEntity.status(200).body(facultyService.registerFaculty(facultyRegistrationDto));
    }

    @PreAuthorize("hasRole('FACULTY')")
    @PutMapping("/update/{facultyId}")
    public ResponseEntity<String> updateFaculty(@Valid @PathVariable Long facultyId, @RequestBody FacultyRegistrationDto facultyRegistrationDto) {
        log.info("Received request to update faculty with ID: {}", facultyId);
        String response = facultyService.updateFaculty(facultyId, facultyRegistrationDto);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('FACULTY')")
    @PatchMapping("/patch/{facultyId}")
    public ResponseEntity<String> patchFaculty(@Valid @PathVariable Long facultyId,
            @RequestBody Map<String, Object> updates) {
        log.info("Received patch request for facultyId: {}", facultyId);
        String response = facultyService.patchFaculty(facultyId, updates);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{facultyId}")
    public ResponseEntity<String> deleteFaculty(@Valid @PathVariable Long facultyId) {
        log.info("Received request to delete faculty with ID: {}", facultyId);
        String response = facultyService.deleteFaculty(facultyId);
        return ResponseEntity.ok(response);
        }
    @PreAuthorize("hasRole('STUDENT')")
    @PatchMapping("/updateRating/{facultyId}/{newFacultyRating}")
    public ResponseEntity<String> updateFacultyRating(@Valid @PathVariable Long facultyId, @PathVariable double newFacultyRating){
        return ResponseEntity.status(200).body(facultyService.updateFacultyRating(facultyId,newFacultyRating));
    }

    @PreAuthorize("hasRole('FACULTY')")
    @GetMapping("/dashboard/{facultyId}")
    public ResponseEntity<FacultyDashboardDto> getFacultyDashboard(@Valid @PathVariable Long facultyId){
        log.info("Faculty dashboard requested for facultyId: {}", facultyId);
        FacultyDashboardDto dashboardDto = facultyService.getFacultyDashboard(facultyId);
        return ResponseEntity.status(200).body(dashboardDto);
    }

    @PreAuthorize("hasRole('FACULTY')")
    @GetMapping("/profile/{facultyId}")
    public ResponseEntity<FacultyProjection> getFacultyProfile(@Valid @PathVariable Long facultyId){
        return facultyService.getFacultyProfile(facultyId)
                .map(app -> ResponseEntity.ok(app))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('FACULTY')")
    @GetMapping("/upcoming/{facultyId}")
    public ResponseEntity<List<Exam>> getupComingExams(@Valid @PathVariable Long facultyId) {
        // This now matches the return type of your service/repository
        return ResponseEntity.ok(facultyService.getupComingExams(facultyId));
    }

    @PreAuthorize("hasRole('FACULTY')")
    @GetMapping("/upComingCount/{facultyId}")
    public Map<String,Integer> getupComingExamsCount(@Valid @PathVariable Long facultyId){
        int count = facultyService.getupComingExamsCount(facultyId);
        return Map.of("upComing Exams", count);
    }
}

