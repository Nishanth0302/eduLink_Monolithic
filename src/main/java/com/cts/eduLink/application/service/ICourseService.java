package com.cts.eduLink.application.service;

import com.cts.eduLink.application.dto.CourseEnrollmentDto;
import com.cts.eduLink.application.dto.CourseRegistrationDto;
import com.cts.eduLink.application.projection.CourseDetailByIdProjection;
import com.cts.eduLink.application.projection.CourseDetailProjection;
import com.cts.eduLink.application.projection.CourseProjection;

import java.util.List;
import java.util.Map;

public interface ICourseService {
    String registerCourse(CourseRegistrationDto courseRegistrationDto);
    List<CourseProjection> findAllAvailableCourse();
    String updateCourse(Long courseId, CourseRegistrationDto courseRegistrationDto);
    String patchCourse(Long courseId, Map<String, Object> updates);
    String deleteCourse(Long courseId);
    List<CourseProjection> getCoursesByFaculty(Long facultyId);
    public int getFacultyCourseCount(Long facultyId );
    CourseDetailByIdProjection findCourseDetailsById(Long courseId);
    String courseEnrollmentRequest(CourseEnrollmentDto courseEnrollmentDto);
    String updateCourseRating(Long courseId, double newCourseRating);
    List<CourseDetailProjection> findCourseListByStudentId(Long studentId);
}
