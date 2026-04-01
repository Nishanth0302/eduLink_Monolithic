package com.cts.eduLink.application.repositoryTest;

import com.cts.eduLink.application.entity.AppUser;
import com.cts.eduLink.application.entity.Role;
import com.cts.eduLink.application.entity.Student;
import com.cts.eduLink.application.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    private Optional<AppUser> appUser;

    @BeforeEach
    public void setUp(){
        appUser = Optional.of(new AppUser());
        Optional<AppUser> appUser = Optional.of(new AppUser());
        appUser.get().setUserName("Nikhil");
        appUser.get().setUserEmail("nikhil@gmail.com");
        appUser.get().setPhoneNumber(123242112L);
        appUser.get().setRole(new Role());
        appUser.get().setStudent(new Student());
        appUserRepository.save(appUser.get());
    }

    @Test

    public void findUserByEmailTest_200(){
        Optional<AppUser> appUserOptional = appUserRepository.findAppUserByUserEmail("nikhil@gmail.com");
        assertTrue(appUserOptional.isPresent());
    }

    @Test
    public void findUserByPhoneNumberTest_200(){
        Optional<AppUser> appUserOptional = appUserRepository.findAppUserByUserPhoneNumber(123242112L);
        assertTrue(appUserOptional.isPresent());
    }
}
