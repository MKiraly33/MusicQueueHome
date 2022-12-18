package com.mate.kiraly.AuthService.service;

import com.mate.kiraly.AuthService.model.LocalUser;
import com.mate.kiraly.AuthService.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AuthUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LocalUser localUser = userRepo.findByUsername(username);
        if (localUser == null) {
            throw new UsernameNotFoundException(username);
        }
        UserDetails user = User.withUsername(localUser.getUsername()).password(localUser.getPassword())
                .authorities("USER").build();
        return user;
    }


}
