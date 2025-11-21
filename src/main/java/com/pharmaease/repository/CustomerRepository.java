package com.pharmaease.repository;

import com.pharmaease.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByActive(Boolean active);
    List<Customer> findByNameContainingIgnoreCase(String name);
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByEmail(String email);
}