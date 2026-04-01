package com.cts.eduLink.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseEnrollmentDto {
    @NotNull(message = "Course ID is required for enrollment")
    private Long courseId;

    @NotNull(message = "Student ID is required for enrollment")
    private Long studentId;
}
