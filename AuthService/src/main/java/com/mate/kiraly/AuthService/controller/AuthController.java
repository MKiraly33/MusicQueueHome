package com.mate.kiraly.AuthService.controller;

import com.mate.kiraly.AuthService.dto.*;
import com.mate.kiraly.AuthService.model.LocalUser;
import com.mate.kiraly.AuthService.repository.UserRepo;
import com.mate.kiraly.AuthService.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    JwtEncoder encoder;

    private final AuthService authService;
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService tokenService) {
        this.authService = tokenService;
    }

    @PostMapping("/token")
    public TokenResponseDTO token(@RequestBody LocalUser user){
        return authService.generateToken(user);
    }
    @PostMapping("/register")
    public RegisterResponseDTO register(@RequestBody RegisterDTO registerDTO){
        return authService.register(registerDTO);
    }
    @GetMapping("/userdetails/{userid}")
    public UserDetailsDTO getUserDetails(@PathVariable Long userid){
        return authService.getUserDetails(userid);
    }
    @GetMapping("/validate")
    public TokenValidateDTO check(@RequestHeader("Auth-Token") String token){ return authService.validateToken(token); }
}
