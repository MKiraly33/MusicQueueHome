package com.mate.kiraly.AuthService.service;

import com.mate.kiraly.AuthService.dto.RegisterDTO;
import com.mate.kiraly.AuthService.dto.RegisterResponseDTO;
import com.mate.kiraly.AuthService.dto.TokenResponseDTO;
import com.mate.kiraly.AuthService.dto.UserDetailsDTO;
import com.mate.kiraly.AuthService.model.LocalUser;
import com.mate.kiraly.AuthService.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final JwtEncoder jwtEncoder;

    @Value("${password.min-length}")
    Integer passwordMinLength;

    @Autowired
    private UserRepo userRepo;

    public AuthService(JwtEncoder jwtEncoder){
        this.jwtEncoder = jwtEncoder;
    }

    public TokenResponseDTO generateToken(Authentication authentication){
        Instant now = Instant.now();
        String scope = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(6 * 60 * 60))
                .subject(authentication.getName())
                .claim("scope", scope)
                .build();
        TokenResponseDTO tokenResponseDTO = new TokenResponseDTO();
        tokenResponseDTO.setToken(this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue());
        return tokenResponseDTO;
    }

    public RegisterResponseDTO register(RegisterDTO registerDTO) {

        RegisterResponseDTO registerResponseDTO = new RegisterResponseDTO();

        if(registerDTO.getPassword().length() < passwordMinLength){
            registerResponseDTO.setResult("Password is shorter than the minimum allowed!");
            return registerResponseDTO;
        }

        LocalUser localUser = userRepo.findByUsername(registerDTO.getUsername());

        if (localUser == null) {
            LocalUser toRegister = new LocalUser();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            toRegister.setPassword("{bcrypt}" + bCryptPasswordEncoder.encode(registerDTO.getPassword()));
            toRegister.setUsername(registerDTO.getUsername());
            userRepo.save(toRegister);
            registerResponseDTO.setResult("Successfully registered!");
            return registerResponseDTO;
        }

        registerResponseDTO.setResult("Username already taken!");
        return registerResponseDTO;
    }

    public UserDetailsDTO getUserDetails(Long userId){
        Optional<LocalUser> user = userRepo.findById(userId);
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
        userDetailsDTO.setUsername(null);
        userDetailsDTO.setId(null);
        if(user.isPresent()){
            userDetailsDTO.setUsername(user.get().getUsername());
            userDetailsDTO.setId(user.get().getId());
        }
        return userDetailsDTO;
    }
}
