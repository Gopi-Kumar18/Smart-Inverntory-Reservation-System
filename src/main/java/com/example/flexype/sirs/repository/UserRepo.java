package com.example.flexype.sirs.repository;


import com.example.flexype.sirs.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<AppUser, Long> {
    // e.g., Optional<AppUser> findByUsername(String username);
}
