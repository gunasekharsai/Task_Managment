package com.taskmanagment.app.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskmanagment.app.Models.UserModel;
import com.taskmanagment.app.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
 
    private final UserRepository userRepository;
 
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        UserModel user = userRepository.findByEmail(emailOrUsername)
            .or(() -> userRepository.findByUsername(emailOrUsername))
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with email or username: " + emailOrUsername));
        return UserPrincipal.create(user);
    }
 
    @Transactional(readOnly = true)
    public UserDetails loadUserById(String id) {
        UserModel user = userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }
}
