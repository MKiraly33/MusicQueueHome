package com.mate.kiraly.AuthService.service;

import com.mate.kiraly.AuthService.dto.*;
import com.mate.kiraly.AuthService.model.LocalUser;
import com.mate.kiraly.AuthService.repository.UserRepo;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${password.min-length}")
    Integer passwordMinLength;

    @Autowired
    private UserRepo userRepo;

    public AuthService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder){
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public TokenResponseDTO generateToken(LocalUser user){
        LocalUser dbUser = userRepo.findByUsername(user.getUsername());
        if(dbUser == null){
            return new TokenResponseDTO();
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String providedPassword = "{bcrypt}" + bCryptPasswordEncoder.encode(user.getPassword());
        if(bCryptPasswordEncoder.matches(user.getPassword(), dbUser.getPassword())){
            return new TokenResponseDTO();
        }

        Instant now = Instant.now();
        //String scope = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(6 * 60 * 60))
                .claim("id", dbUser.getId().toString())
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

    public TokenValidateDTO validateToken(final String token) {
        TokenValidateDTO tokenValidateDTO = new TokenValidateDTO();
        try {
            Jwt jwt = jwtDecoder.decode(token);
            tokenValidateDTO.setMessage("Authentication successful");
            tokenValidateDTO.setIsValid(true);
            tokenValidateDTO.setUserId(Long.parseLong(jwt.getClaimAsString("id")));
        }catch (JwtException e){
            tokenValidateDTO.setMessage("Authentication failed!");
            tokenValidateDTO.setIsValid(false);
            tokenValidateDTO.setUserId(null);
        }
        return tokenValidateDTO;
    }
}
