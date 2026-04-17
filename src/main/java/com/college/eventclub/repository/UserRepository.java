package com.college.eventclub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.eventclub.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    

}
