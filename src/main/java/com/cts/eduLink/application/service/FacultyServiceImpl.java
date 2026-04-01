package com.cts.eduLink.application.service;

import com.cts.eduLink.application.classexception.FacultyException;
import com.cts.eduLink.application.dto.FacultyRegistrationDto;
import com.cts.eduLink.application.dto.FacultyDashboardDto;
import com.cts.eduLink.application.entity.AppUser;
import com.cts.eduLink.application.entity.Faculty;
import com.cts.eduLink.application.entity.Role;
import com.cts.eduLink.application.projection.FacultyProjection;
import com.cts.eduLink.application.repository.CourseRepository;
import com.cts.eduLink.application.projection.FacultyDetailProjection;
import com.cts.eduLink.application.repository.FacultyRepository;
import com.cts.eduLink.application.repository.RoleRepository;
import com.cts.eduLink.application.util.RatingCalculator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.util.Map;
import com.cts.eduLink.application.util.DtoMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.cts.eduLink.application.constants.ErrorConstant.Faculty_Error;

import com.cts.eduLink.application.entity.Exam;
import com.cts.eduLink.application.entity.Course;

@Service
@AllArgsConstructor
@Slf4j
public class FacultyServiceImpl implements IFacultyService {

    private final FacultyRepository facultyRepository;
    private final AppUserServiceImpl appUserService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;

    @Transactional
    @Override
    public String registerFaculty(FacultyRegistrationDto facultyRegistrationDto) {

        log.debug("AppUser and Faculty separation initiated");
        AppUser appUser = DtoMapper.appUserDtoSeparator(facultyRegistrationDto,passwordEncoder);
        Faculty faculty = DtoMapper.facultyDtoSeparator(facultyRegistrationDto);
        Optional<Role> role = roleRepository.findRoleByName("FACULTY");

        appUser.setRole(role.get());
        log.debug("appUser instance has sent for registration");
        appUserService.registerAppUser(appUser);
        faculty.setAppUser(appUser);
        facultyRepository.save(faculty);
        log.info("faculty entity has saved into database for {}",appUser.getUserEmail());
        return "You have registered SuccessFully, your login id is: "+faculty.getFacultyId();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String updateFaculty(Long facultyId, FacultyRegistrationDto dto) {
        log.info("Updating faculty profile for ID: {}", facultyId);
        Faculty existingFaculty = facultyRepository.findFacultyById(facultyId)
                .orElseThrow(() -> new FacultyException("Faculty not found with ID: " + facultyId, org.springframework.http.HttpStatus.NOT_FOUND));
        DtoMapper.updateFacultyFromDto(existingFaculty, dto);
        facultyRepository.save(existingFaculty);

        log.info("Faculty profile updated successfully for ID: {}", facultyId);
        return "Faculty profile updated successfully!";
    }

    @Override
    public List<FacultyDetailProjection> filterFacultyByRating(int facultyRating) throws FacultyException {
        log.info("Faculty rating filtration request has sent to database");
        List<FacultyDetailProjection> facultyDetailProjections = facultyRepository.filterFacultyByRating(facultyRating);
        if (facultyDetailProjections.isEmpty()) {
            log.error("No faculty available with {} ratting", facultyRating);
            throw new FacultyException(Faculty_Error + facultyRating, HttpStatus.NOT_FOUND);
        }
        log.info("Faculty with rating {} fetch successfully and first faculty name is {}", facultyRating, facultyDetailProjections.getFirst().getFacultyName());
        return facultyDetailProjections;
    }

    @Override
    @Transactional
    public String updateFacultyRating(Long facultyId, double newFacultyRating) {
        log.info("Updating rating for Faculty ID: {} with new score: {}", facultyId, newFacultyRating);
        Optional<Faculty> faculty = facultyRepository.findFacultyById(facultyId);
        if(faculty.isEmpty()){
            log.error("Faculty with ID {} not found", facultyId);
            throw new FacultyException("Faculty is not registered",HttpStatus.NOT_FOUND);
        }
        Long totalFacultyRating = faculty.get().getTotalFacultyRatingCount();
        double newRating = RatingCalculator.calculateRating(faculty.get().getFacultyRating(),newFacultyRating,totalFacultyRating);
        faculty.get().setFacultyRating(newRating);
        faculty.get().setTotalFacultyRatingCount(totalFacultyRating+1);
        log.info("Update successful for Faculty ID: {}. Rating changed to {} (Total reviews: {})",facultyId, newRating, totalFacultyRating + 1);
        return "Thanks for you feedBack!";
    }

    @Transactional
    public String patchFaculty(Long facultyId, Map<String, Object> updates) {
        log.info("Patch update initiated for Faculty ID: {}", facultyId);

        Faculty faculty = facultyRepository.findFacultyById(facultyId)
                .orElseThrow(() -> new FacultyException("Faculty not found", HttpStatus.NOT_FOUND));

        updates.forEach((key, value) -> {
            switch (key) {
                case "phoneNumber":
                    faculty.getAppUser().setPhoneNumber(Long.valueOf(value.toString()));
                    break;
                case "facultyYearOfExperience":
                    faculty.setFacultyYearOfExperience((Integer) value);
                    break;
                case "facultyAddress":
                    faculty.setFacultyAddress((String) value);
                    break;
            }
        });

        facultyRepository.save(faculty);
        return "Faculty record partially updated successfully!";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String deleteFaculty(Long facultyId) {
        log.info("Deletion request initiated for Faculty ID: {}", facultyId);
        Faculty faculty = facultyRepository.findFacultyById(facultyId)
                .orElseThrow(() -> new FacultyException("Faculty not found with ID: " + facultyId, org.springframework.http.HttpStatus.NOT_FOUND));
        facultyRepository.delete(faculty);

        log.info("Faculty ID: {} and associated user deleted successfully", facultyId);
        return "Faculty record deleted successfully!";
    }

    @Override
    public FacultyDashboardDto getFacultyDashboard(Long facultyId) {
        log.info("Generating dashboard data for faculty: {}", facultyId);
        int courses = courseRepository.getFacultyCourseCount(facultyId);
        int exams = facultyRepository.getUpcomingExamsCount(facultyId);
        FacultyDashboardDto dashboard = new FacultyDashboardDto();
        dashboard.setCourseCount(courses);
        dashboard.setUpcomingExamsCount(exams);

        return dashboard;
    }

    @Override
    public List<Course> getFacultyCourses(Long facultyId) {
        log.debug("Fetching courses for faculty: {}", facultyId);
        Optional<Faculty> facultyOptional = facultyRepository.findFacultyById(facultyId);

        if (facultyOptional.isEmpty()) {
            log.warn("Faculty not found with id: {}", facultyId);
            return List.of();
        }
        Faculty faculty = facultyOptional.get();
        List<Course> courses = new ArrayList<>(faculty.getCourseSet());
        log.debug("Found {} courses for faculty: {}", courses.size(), facultyId);
        return courses;
    }

    public List<Exam> getupComingExams(Long facultyId ){
        return facultyRepository.findUpcomingExamsByFacultyId(facultyId);
    }

    public int getupComingExamsCount(Long facultyId){
        return facultyRepository.getUpcomingExamsCount(facultyId);
    }

    public Optional<FacultyProjection> getFacultyProfile(Long facultyId) throws FacultyException {
        return facultyRepository.findFacultyProfile(facultyId);
    }
}

