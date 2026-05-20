package com.cts.eduLink.application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Faculty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    private Long facultyId;
    private String facultyGender;
    private String facultyAddress;
    private int facultyYearOfExperience;
    private double facultyRating;
    private long totalFacultyRatingCount;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "app_user_id",referencedColumnName = "id")
    private AppUser appUser;

    @ManyToMany
    @JoinTable(
            name = "faculty_course_mapping",
            joinColumns = @JoinColumn(name = "faculty_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courseSet = new HashSet<>();
}
