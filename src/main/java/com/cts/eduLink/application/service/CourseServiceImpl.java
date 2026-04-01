package com.cts.eduLink.application.service;

import com.cts.eduLink.application.classexception.CourseException;
import com.cts.eduLink.application.classexception.FacultyException;
import com.cts.eduLink.application.classexception.StudentException;
import com.cts.eduLink.application.dto.CourseEnrollmentDto;
import com.cts.eduLink.application.dto.CourseRegistrationDto;
import com.cts.eduLink.application.entity.*;
import com.cts.eduLink.application.projection.CourseProjection;
import com.cts.eduLink.application.projection.CourseDetailByIdProjection;
import com.cts.eduLink.application.projection.CourseDetailProjection;
import com.cts.eduLink.application.repository.CourseRepository;
import com.cts.eduLink.application.repository.FacultyRepository;
import com.cts.eduLink.application.util.DtoMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.util.Map;
import com.cts.eduLink.application.repository.StudentRepository;
import com.cts.eduLink.application.util.RatingCalculator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class CourseServiceImpl implements ICourseService {
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public String registerCourse(CourseRegistrationDto courseRegistrationDto) throws FacultyException {
        log.info("Course registration has intercepted inside service");
        Optional<Faculty> facultyOption = facultyRepository.findFacultyById(courseRegistrationDto.getFacultyId());
        if (facultyOption.isEmpty()) {
            log.error("{} is not authorized to register course", courseRegistrationDto.getFacultyId());
            throw new FacultyException(courseRegistrationDto.getFacultyId() + " is not registered", HttpStatus.BAD_REQUEST);
        }
        Course course = DtoMapper.courseDtoSeparator(courseRegistrationDto);
        log.error("Unable to separate faculty from courseRegistrationDto");
        course.setCourseStatus("ACTIVE");
        course.getFacultySet().add(facultyOption.get());
        facultyOption.get().getCourseSet().add(course);
        courseRepository.save(course);
        log.info("Course with id {} saved successFully into database", course.getCourseId());
        return "Course has registered successFully with course Id: " + course.getCourseId();
    }

    @Override
    @Transactional
    public String updateCourse(Long courseId, CourseRegistrationDto courseRegistrationDto) {
        log.info("Updating course details for Course ID: {}", courseId);
        Course existingCourse = courseRepository.findByCourseId(courseId)
                .orElseThrow(() -> new CourseException("Course not found with ID: " + courseId, HttpStatus.NOT_FOUND));
        DtoMapper.updateCourseFromDto(existingCourse, courseRegistrationDto);
        courseRepository.save(existingCourse);
        log.info("Course ID: {} updated successfully", courseId);
        return "Course updated successfully!";
    }

    @Override
    @Transactional
    public String patchCourse(Long courseId, Map<String, Object> updates) {
        log.info("Patch update initiated for Course ID: {}", courseId);
        Course course = courseRepository.findByCourseId(courseId)
                .orElseThrow(() -> new CourseException("Course not found with ID: " + courseId, HttpStatus.NOT_FOUND));
        updates.forEach((key, value) -> {
            if (value != null) {
                switch (key) {
                    case "courseTitle":
                        course.setCourseTitle(value.toString());
                        break;
                    case "courseSubject":
                        course.setCourseSubject(value.toString());
                        break;
                    case "courseGradeLevel":
                        course.setCourseGradeLevel(value.toString());
                        break;
                    case "courseCredit":
                        course.setCourseCredit(Integer.parseInt(value.toString()));
                        break;
                    case "courseStatus":
                        course.setCourseStatus(value.toString());
                        break;
                }
            }
        });
        courseRepository.save(course);
        log.info("Course ID: {} partially updated successfully", courseId);
        return "Course partially updated successfully!";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String deleteCourse(Long courseId) {
        log.info("Deletion request initiated for Course ID: {}", courseId);
        Course course = courseRepository.findByCourseId(courseId)
                .orElseThrow(() -> new CourseException("Course not found with ID: " + courseId, HttpStatus.NOT_FOUND));
        course.setCourseStatus("INACTIVE");

        log.info("Course ID: {} deleted successfully", courseId);
        return "Course deleted successfully with Id : "+courseId;
    }

    @Override
    public List<CourseProjection> findAllAvailableCourse() throws CourseException {
        log.info("User has requested to display course List!");
        List<CourseProjection> courseProjections = courseRepository.findAllAvailableCourse();
        if (courseProjections.isEmpty()) {
            log.error("no course is available to display");
            throw new CourseException("No course Available!", HttpStatus.NOT_FOUND);
        }
        log.info("Course List has been accessed SuccessFully and first course name is {}", courseProjections.getFirst().getCourseTitle());
        return courseProjections;
    }

    @Override
    public CourseDetailByIdProjection findCourseDetailsById(Long courseId) throws CourseException {
        Optional<CourseDetailByIdProjection> courseDetailByIdProjection = courseRepository.findCourseDetailsById(courseId);
        if (courseDetailByIdProjection.isEmpty()) {
            log.debug("User requested for course {} which is not available", courseId);
            throw new CourseException("No Course available for course id : " + courseId, HttpStatus.NOT_FOUND);
        }
        log.debug("{} has fetched successfully from course table", courseId);
        return courseDetailByIdProjection.get();
    }

    @Override
    public List<CourseProjection> getCoursesByFaculty(Long facultyId) {
        return courseRepository.findCoursesByFacultyId(facultyId);
    }

    @Transactional
    public String courseEnrollmentRequest(CourseEnrollmentDto courseEnrollmentDto) throws StudentException, CourseException {
        log.info("Received enrollment request: Student ID {} for Course ID {}", courseEnrollmentDto.getStudentId(), courseEnrollmentDto.getCourseId());
        Optional<Student> student = studentRepository.findStudentById(courseEnrollmentDto.getStudentId());
        if (student.isEmpty()) {
            log.error("Enrollment failed: Student ID {} not found", courseEnrollmentDto.getStudentId());
            throw new StudentException("Invalid student id: " + courseEnrollmentDto.getStudentId(), HttpStatus.BAD_REQUEST);
        }
        Optional<Course> course = courseRepository.findCourseById(courseEnrollmentDto.getCourseId());
        if (course.isEmpty()) {
            log.error("Enrollment failed: Course ID {} not found", courseEnrollmentDto.getCourseId());
            throw new CourseException("Invalid course id: " + courseEnrollmentDto.getCourseId(), HttpStatus.BAD_REQUEST);
        }
        if (student.get().getCourseSet().contains(course.get())) {
            log.warn("Student {} already registered for course {}", courseEnrollmentDto.getStudentId(), courseEnrollmentDto.getCourseId());
            throw new CourseException("You are already registered with this course", HttpStatus.CONFLICT);
        }
        student.get().getCourseSet().add(course.get());
        course.get().getStudentSet().add(student.get());
        courseRepository.save(course.get());
        log.info("Successfully enrolled Student ID {} into Course ID {}", courseEnrollmentDto.getStudentId(), courseEnrollmentDto.getCourseId());
        return "Enrolled SuccessFull!";
    }

    @Override
    @Transactional
    public String updateCourseRating(Long courseId, double newCourseRating) throws CourseException {
        log.info("Updating rating for course ID: {} with new rating: {}", courseId, newCourseRating);
        Optional<Course> course = courseRepository.findCourseById(courseId);
        if (course.isEmpty()) {
            log.error("Course not found with ID: {}", courseId);
            throw new CourseException("Course is not registered", HttpStatus.NOT_FOUND);
        }
        double newRating = RatingCalculator.calculateRating(course.get().getCourseRating(), newCourseRating, course.get().getTotalCourseRatingCount());
        course.get().setTotalCourseRatingCount(course.get().getTotalCourseRatingCount() + 1);
        course.get().setCourseRating(newRating);
        log.info("Course {} updated. New Rating: {}, Total Reviews: {}",
                courseId, newRating, course.get().getTotalCourseRatingCount());
        return "Thanks for you feedBack!";
    }

    @Override
    public int getFacultyCourseCount(Long facultyId) {
        int count = courseRepository.getFacultyCourseCount(facultyId);
        return count;
    }

    @Override
    public List<CourseDetailProjection> findCourseListByStudentId(Long studentId) throws CourseException, StudentException {
        log.info("Fetching course list for student ID: {}", studentId);
        Optional<Student> student = studentRepository.findStudentById(studentId);
        if (student.isEmpty()) {
            log.error("Student lookup failed: ID {} not registered", studentId);
            throw new StudentException("Student is not registered with id: " + studentId, HttpStatus.UNAUTHORIZED);
        }
        List<CourseDetailProjection> courseDetailProjections = courseRepository.findCourseListByStudentId(studentId);
        if (courseDetailProjections.isEmpty()) {
            log.warn("No courses found for student ID: {}", studentId);
            throw new CourseException("No course available for student id: " + studentId, HttpStatus.NOT_FOUND);
        }
        log.info("Successfully retrieved {} courses for student ID: {}", courseDetailProjections.size(), studentId);
        return courseDetailProjections;
    }
}
