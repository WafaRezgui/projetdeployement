package com.example.contentmanagement.security;

import com.example.contentmanagement.entity.User;
import com.example.contentmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom User Details Service
 * WHY: Provides Spring Security with user authentication details
 * Loads user from MongoDB and maps roles to Spring Security authorities
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        var authorities = new java.util.ArrayList<SimpleGrantedAuthority>();

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role ->
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()))
            );
        } else if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountLocked(user.isLocked())
                .disabled(!user.isEnabled())
                .build();
    }
}