package com.pharmaease.service;

import com.pharmaease.model.Pharmacist;
import com.pharmaease.repository.PharmacistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacistService {

    private final PharmacistRepository pharmacistRepository;
    private final PasswordEncoder passwordEncoder;

    public Pharmacist register(Pharmacist pharmacist) {
        if (pharmacistRepository.existsByEmail(pharmacist.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        pharmacist.setPassword(passwordEncoder.encode(pharmacist.getPassword()));
        return pharmacistRepository.save(pharmacist);
    }

    public Optional<Pharmacist> authenticate(String email, String password) {
        Optional<Pharmacist> pharmacist = pharmacistRepository.findByEmail(email);
        if (pharmacist.isPresent() && passwordEncoder.matches(password, pharmacist.get().getPassword())) {
            return pharmacist;
        }
        return Optional.empty();
    }

    public Pharmacist createPharmacist(Pharmacist pharmacist) {
        pharmacist.setPassword(passwordEncoder.encode(pharmacist.getPassword()));
        return pharmacistRepository.save(pharmacist);
    }

    public Pharmacist updatePharmacist(Long id, Pharmacist pharmacist) {
        Pharmacist existing = getPharmacistById(id);
        existing.setName(pharmacist.getName());
        existing.setPhone(pharmacist.getPhone());
        existing.setLicenseNumber(pharmacist.getLicenseNumber());
        existing.setRole(pharmacist.getRole());
        existing.setActive(pharmacist.getActive());
        return pharmacistRepository.save(existing);
    }

    public void deletePharmacist(Long id) {
        pharmacistRepository.deleteById(id);
    }

    public Pharmacist getPharmacistById(Long id) {
        return pharmacistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found"));
    }

    public Optional<Pharmacist> getPharmacistByEmail(String email) {
        return pharmacistRepository.findByEmail(email);
    }

    public List<Pharmacist> getAllPharmacists() {
        return pharmacistRepository.findAll();
    }

    public List<Pharmacist> getActivePharmacists() {
        return pharmacistRepository.findByActive(true);
    }
}