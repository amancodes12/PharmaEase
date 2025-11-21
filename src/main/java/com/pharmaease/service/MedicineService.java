package com.pharmaease.service;

import com.pharmaease.model.Inventory;
import com.pharmaease.model.Medicine;
import com.pharmaease.repository.InventoryRepository;
import com.pharmaease.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final InventoryRepository inventoryRepository;

    public Medicine createMedicine(Medicine medicine) {
        Medicine saved = medicineRepository.save(medicine);

        // Create inventory entry for new medicine
        Inventory inventory = new Inventory();
        inventory.setMedicine(saved);
        inventory.setTotalQuantity(0);
        inventory.setAvailableQuantity(0);
        inventory.setReservedQuantity(0);
        inventory.setLowStock(true);
        inventoryRepository.save(inventory);

        return saved;
    }

    public Medicine updateMedicine(Long id, Medicine medicine) {
        Medicine existing = getMedicineById(id);
        existing.setName(medicine.getName());
        existing.setGenericName(medicine.getGenericName());
        existing.setManufacturer(medicine.getManufacturer());
        existing.setCategory(medicine.getCategory());
        existing.setDosageForm(medicine.getDosageForm());
        existing.setStrength(medicine.getStrength());
        existing.setDescription(medicine.getDescription());
        existing.setUnitPrice(medicine.getUnitPrice());
        existing.setSellingPrice(medicine.getSellingPrice());
        existing.setReorderLevel(medicine.getReorderLevel());
        existing.setRequiresPrescription(medicine.getRequiresPrescription());
        existing.setActive(medicine.getActive());
        existing.setSupplier(medicine.getSupplier());
        return medicineRepository.save(existing);
    }

    public void deleteMedicine(Long id) {
        medicineRepository.deleteById(id);
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public List<Medicine> getActiveMedicines() {
        return medicineRepository.findByActive(true);
    }

    public List<Medicine> searchMedicines(String keyword) {
        return medicineRepository.findByNameContainingIgnoreCaseOrGenericNameContainingIgnoreCase(keyword, keyword);
    }

    public List<Medicine> getMedicinesByCategory(String category) {
        return medicineRepository.findByCategory(category);
    }

    public List<String> getAllCategories() {
        return medicineRepository.findAllCategories();
    }

    public List<String> getAllManufacturers() {
        return medicineRepository.findAllManufacturers();
    }
}