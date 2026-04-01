package com.cts.eduLink.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
public class AssignmentDto {
    private Long id;
    @NotBlank(message="Assignment id is required")
    private Long assignmentId;
    @NotBlank(message="Assignemnt Title is required")

    @Size(min=5, max=200, message = "Assignment title should be between 5 to 200 characters")
    private String assignmentTitle;
    @NotBlank(message = "Assignment Description can't be null")
    @Size(min=50, max=2000, message = "Assignment Description should be between 50 to 2000 characters")
    private String assignmentDescription;
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(Active|Expired)$", message = "Status must be Active, Draft, or Archived")
    private String assignmentStatus;
    private int totalMarks;
    @NotNull(message = "Course ID is required")
    private Long courseId;

}