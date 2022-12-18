package com.mate.kiraly.AuthService.repository;

import com.mate.kiraly.AuthService.model.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<LocalUser, Long> {
    LocalUser findByUsername(String username);
}
