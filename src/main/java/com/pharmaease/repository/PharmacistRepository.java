package com.pharmaease.repository;

import com.pharmaease.model.Pharmacist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacistRepository extends JpaRepository<Pharmacist, Long> {
    Optional<Pharmacist> findByEmail(String email);
    Optional<Pharmacist> findByLicenseNumber(String licenseNumber);
    List<Pharmacist> findByActive(Boolean active);
    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);
}