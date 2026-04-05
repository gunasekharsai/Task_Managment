package com.taskmanagment.app.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagment.app.Models.UserModel;

@Repository
public interface UserRepository extends JpaRepository<UserModel, String> {
 
    Optional<UserModel> findByEmail(String email);
 
    Optional<UserModel> findByUsername(String username);
 
    boolean existsByEmail(String email);
 
    boolean existsByUsername(String username);
}
