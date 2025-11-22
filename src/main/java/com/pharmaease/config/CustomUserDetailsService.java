package com.pharmaease.config;

import com.pharmaease.model.Pharmacist;
import com.pharmaease.repository.PharmacistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PharmacistRepository pharmacistRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Pharmacist pharmacist = pharmacistRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (!pharmacist.getActive()) {
            throw new UsernameNotFoundException("User is inactive: " + email);
        }

        return User.builder()
                .username(pharmacist.getEmail())
                .password(pharmacist.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + pharmacist.getRole())))
                .build();
    }
}