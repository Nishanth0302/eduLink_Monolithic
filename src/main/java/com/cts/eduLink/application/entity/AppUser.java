package com.cts.eduLink.application.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Entity
@Data
@Getter
@Setter
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;
    private String userEmail;
    private Long phoneNumber;
    private String userPassword;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(mappedBy = "appUser")
    private Student student;

    @OneToOne(mappedBy = "appUser")
    private Faculty faculty;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL)
    private List<Notification> notificationList;

    @OneToMany(mappedBy = "appUser")
    private List<FeedBack> feedBackList;
}
