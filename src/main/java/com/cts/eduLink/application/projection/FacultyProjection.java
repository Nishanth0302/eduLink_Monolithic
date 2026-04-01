package com.cts.eduLink.application.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FacultyProjection {
    private final String userName;
    private final String userEmail;
    private final Long phoneNumber;
    private final String facultyGender;
    private final int facultyYearOfExperience;
    private final String facultyAddress;
    private final double facultyRating;
}