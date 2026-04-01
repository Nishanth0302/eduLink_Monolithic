package com.cts.eduLink.application.controller;

import com.cts.eduLink.application.classexception.SecurityException;
import com.cts.eduLink.application.dto.AuthRequestDto;
import com.cts.eduLink.security.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appUser")
@AllArgsConstructor
@Slf4j
public class AppUserController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody AuthRequestDto authRequestDto) {
        log.info("Login attempt initiated for user: {}", authRequestDto.getUserEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequestDto.getUserEmail(), authRequestDto.getUserPassword())
            );
        }
        catch (BadCredentialsException ex){
            throw new SecurityException("Invalid Credentials", HttpStatus.FORBIDDEN);
        }
        catch (Exception ex){
            throw new SecurityException(ex.getMessage(), HttpStatus.FORBIDDEN);
        }
        log.info("Authentication successful for user: {}", authRequestDto.getUserEmail());
        String token = jwtUtils.generateToken(authRequestDto.getUserEmail());
        return ResponseEntity.ok(token);
    }
}
