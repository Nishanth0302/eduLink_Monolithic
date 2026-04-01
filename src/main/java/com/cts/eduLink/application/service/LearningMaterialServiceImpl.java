package com.cts.eduLink.application.service;

import com.cts.eduLink.application.classexception.CourseException;
import com.cts.eduLink.application.classexception.FileException;
import com.cts.eduLink.application.classexception.LearningMaterialException;
import com.cts.eduLink.application.dto.LearningMaterialRegistrationDto;
import com.cts.eduLink.application.entity.Course;
import com.cts.eduLink.application.entity.LearningMaterial;
import com.cts.eduLink.application.projection.LearningCourseMaterialProjection;
import com.cts.eduLink.application.repository.CourseRepository;
import com.cts.eduLink.application.repository.LearningMaterialRepository;
import com.cts.eduLink.application.util.DtoMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class LearningMaterialServiceImpl implements ILearningMaterialService {

    private final CourseRepository courseRepository;
    private final LearningMaterialRepository learningMaterialRepository;

    @Override
    @Transactional
    public String registerLearningMaterial(LearningMaterialRegistrationDto learningMaterialRegistrationDto) throws CourseException,LearningMaterialException {
        Long courseId = learningMaterialRegistrationDto.getCourseId();
        log.info("Attempting to register learning material for course ID: {}", courseId);
        Optional<Course> course = courseRepository.findCourseById(courseId);
        if(course.isEmpty()){
            log.error("Registration failed: Course ID {} not found", courseId);
            throw new CourseException("Course is not registered with id: "+courseId, HttpStatus.NOT_FOUND);
        }
        if(learningMaterialRepository.checkExistingLearningMaterial(courseId)){
            log.error("Registration failed: Material already exists for Course ID {}", courseId);
            throw new LearningMaterialException("LearningMaterial already uploaded for course id: "+courseId,HttpStatus.CONFLICT);
        }
        try {
            LearningMaterial learningMaterial = DtoMapper.learningMaterialDtoSeparator(learningMaterialRegistrationDto);
            learningMaterial.setCourse(course.get());
            learningMaterialRepository.save(learningMaterial);
            log.info("Successfully registered learning material for course ID: {}", courseId);
            return "Learning material uploaded successfully!";
        }catch (IOException i){
            log.error("File upload failed for course ID: {}", courseId, i);
            throw new FileException("Could not save file to disk",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public LearningCourseMaterialProjection findMaterialsByCourseId(Long courseId) {
        Optional<Course> course = courseRepository.findCourseById(courseId);
        if(course.isEmpty()){
            log.error("Registration failed: Course ID {} not found", courseId);
            throw new CourseException("Course is not registered with id: "+courseId, HttpStatus.NOT_FOUND);
        }
        Optional<LearningCourseMaterialProjection> learningCourseMaterialProjection = learningMaterialRepository.findMaterialsByCourseId(courseId);
        if(learningCourseMaterialProjection.isEmpty()){
            throw new LearningMaterialException("No learning material available for the course id: "+courseId,HttpStatus.NOT_FOUND);
        }
        return  learningCourseMaterialProjection.get();
    }
@Override
    public Resource getFileFromProjection(Long id) {
        try {
            LearningMaterial material = learningMaterialRepository.findById(id).orElseThrow(() -> new RuntimeException("Material not found"));
            Path path = Paths.get(material.getLearningMaterialFile());
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists()) return resource;
            else throw new RuntimeException("File not found");
        } catch (MalformedURLException e) {
            throw new FileException("Error reading file",HttpStatus.NOT_FOUND);
        }
    }
}