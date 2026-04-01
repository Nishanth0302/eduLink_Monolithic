package com.cts.eduLink.application.repository;

import com.cts.eduLink.application.entity.Exam;
import com.cts.eduLink.application.projection.ExamProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    @Query("SELECT new com.cts.eduLink.application.projection.ExamProjection(" +
            "e.examName, e.examLocalDateTime, e.examStatus, e.candidates) " +
            "FROM Exam e ORDER BY e.examLocalDateTime ASC")
    List<ExamProjection> findAllExams();
}