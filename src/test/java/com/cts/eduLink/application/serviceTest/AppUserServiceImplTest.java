package com.cts.eduLink.application.serviceTest;

import com.cts.eduLink.application.classexception.AppUserException;
import com.cts.eduLink.application.entity.AppUser;
import com.cts.eduLink.application.entity.Role;
import com.cts.eduLink.application.repository.AppUserRepository;
import com.cts.eduLink.application.service.AppUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppUserServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    private AppUserServiceImpl appUserService;
    private Optional<AppUser> appUser;

    @BeforeEach
    public void setUp(){
        appUserService = new AppUserServiceImpl(appUserRepository);
        appUser = Optional.of(new AppUser());
        appUser.get().setId(1L);
        appUser.get().setUserName("Nikhil");
        appUser.get().setUserEmail("nikhil@cognizant.com");
        appUser.get().setPhoneNumber(1222222221L);
        appUser.get().setRole(new Role());
    }

    @Test
    public void appUserRegistration_200() throws AppUserException {
        // 1. Stubbing (Arrange)
        when(appUserRepository.findAppUserByUserEmail("nikhil@cognizant.com")).thenReturn(Optional.empty());
        when(appUserRepository.findAppUserByUserPhoneNumber(1222222221L)).thenReturn(Optional.empty());

        // 2. ACT - This was missing in your code
        appUserService.registerAppUser(appUser.get());

        // 3. Verification (Assert)
        verify(appUserRepository, times(1)).findAppUserByUserEmail("nikhil@cognizant.com");
        verify(appUserRepository, times(1)).findAppUserByUserPhoneNumber(1222222221L);
        verify(appUserRepository, times(1)).save(appUser.get()); // Also verify the save call
    }

    @Test
    public void appUserRegistrationUserEmail_409(){

        when(appUserRepository.findAppUserByUserEmail("nikhil@cognizant.com")).thenReturn(appUser);

        AppUserException exception = assertThrows(AppUserException.class,()->{
            appUserService.registerAppUser(appUser.get());
        });

        assertEquals("Your "+appUser.get().getUserEmail()+" is Already registered",exception.getMessage(),"Test Case Passed");

        verify(appUserRepository,times(1)).findAppUserByUserEmail("nikhil@cognizant.com");

    }

    @Test
    public void appUserRegistrationUserPhoneNumber_409(){

        when(appUserRepository.findAppUserByUserEmail("nikhil@cognizant.com")).thenReturn(Optional.empty());
        when(appUserRepository.findAppUserByUserPhoneNumber(1222222221L)).thenReturn(appUser);

        AppUserException exception = assertThrows(AppUserException.class,()->{
            appUserService.registerAppUser(appUser.get());
        });

        assertEquals("Your "+appUser.get().getPhoneNumber()+" is Already registered",exception.getMessage(),"Test Case Passed");

        verify(appUserRepository,times(1)).findAppUserByUserEmail("nikhil@cognizant.com");
        verify(appUserRepository,times(1)).findAppUserByUserPhoneNumber(1222222221L);
    }
}
