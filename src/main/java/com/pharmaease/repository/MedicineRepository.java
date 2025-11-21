package com.pharmaease.repository;

import com.pharmaease.model.Medicine;
import com.pharmaease.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByActive(Boolean active);
    List<Medicine> findByNameContainingIgnoreCaseOrGenericNameContainingIgnoreCase(String name, String genericName);
    List<Medicine> findByCategory(String category);
    List<Medicine> findBySupplier(Supplier supplier);
    List<Medicine> findByManufacturer(String manufacturer);

    @Query("SELECT DISTINCT m.category FROM Medicine m WHERE m.active = true ORDER BY m.category")
    List<String> findAllCategories();

    @Query("SELECT DISTINCT m.manufacturer FROM Medicine m WHERE m.active = true ORDER BY m.manufacturer")
    List<String> findAllManufacturers();
}