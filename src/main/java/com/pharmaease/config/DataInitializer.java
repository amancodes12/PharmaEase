package com.pharmaease.config;

import com.pharmaease.model.*;
import com.pharmaease.repository.*;
import com.pharmaease.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PharmacistRepository pharmacistRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryRepository inventoryRepository;
    private final StockBatchRepository stockBatchRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    
    private final PharmacistService pharmacistService;
    private final MedicineService medicineService;
    private final StockBatchService stockBatchService;
    private final OrderService orderService;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        // Check if data already exists
        if (pharmacistRepository.count() > 0) {
            log.info("Database already contains data. Skipping initialization.");
            return;
        }

        log.info("Starting database initialization with dummy data...");

        // Create Pharmacists
        List<Pharmacist> pharmacists = createPharmacists();
        
        // Create Suppliers
        List<Supplier> suppliers = createSuppliers();
        
        // Create Medicines (inventory records are created automatically)
        List<Medicine> medicines = createMedicines(suppliers);
        
        // Create Stock Batches
        createStockBatches(medicines);
        
        // Create Customers
        List<Customer> customers = createCustomers();
        
        // Create Orders
        createOrders(pharmacists, customers, medicines);
        
        log.info("Database initialization completed successfully!");
        log.info("Created: {} pharmacists, {} suppliers, {} medicines, {} customers, {} orders",
                pharmacists.size(), suppliers.size(), medicines.size(), customers.size(), 
                orderRepository.count());
    }

    private List<Pharmacist> createPharmacists() {
        log.info("Creating pharmacists...");
        List<Pharmacist> pharmacists = new ArrayList<>();

        Pharmacist admin = new Pharmacist();
        admin.setName("Admin User");
        admin.setEmail("admin@pharmaease.com");
        admin.setPassword("admin123");
        admin.setPhone("+91-9876543210");
        admin.setLicenseNumber("PH-ADMIN-001");
        admin.setRole("ADMIN");
        admin.setActive(true);
        pharmacists.add(pharmacistService.createPharmacist(admin));

        Pharmacist pharmacist1 = new Pharmacist();
        pharmacist1.setName("Dr. Rajesh Kumar");
        pharmacist1.setEmail("rajesh@pharmaease.com");
        pharmacist1.setPassword("pharma123");
        pharmacist1.setPhone("+91-9876543211");
        pharmacist1.setLicenseNumber("PH-RJK-002");
        pharmacist1.setRole("PHARMACIST");
        pharmacist1.setActive(true);
        pharmacists.add(pharmacistService.createPharmacist(pharmacist1));

        Pharmacist pharmacist2 = new Pharmacist();
        pharmacist2.setName("Dr. Priya Sharma");
        pharmacist2.setEmail("priya@pharmaease.com");
        pharmacist2.setPassword("pharma123");
        pharmacist2.setPhone("+91-9876543212");
        pharmacist2.setLicenseNumber("PH-PRS-003");
        pharmacist2.setRole("PHARMACIST");
        pharmacist2.setActive(true);
        pharmacists.add(pharmacistService.createPharmacist(pharmacist2));

        log.info("Created {} pharmacists", pharmacists.size());
        return pharmacists;
    }

    private List<Supplier> createSuppliers() {
        log.info("Creating suppliers...");
        List<Supplier> suppliers = new ArrayList<>();

        String[] supplierNames = {
            "MediCorp Pharmaceuticals", "HealthCare Supplies Ltd", "Global Pharma Distributors",
            "Prime Medical Solutions", "Apex Healthcare Products", "MedSupply International"
        };

        String[] cities = {"Mumbai", "Delhi", "Bangalore", "Chennai", "Hyderabad", "Pune"};
        String[] emails = {
            "contact@medicorp.com", "info@healthcare.com", "sales@globalpharma.com",
            "support@primemed.com", "contact@apexhealth.com", "info@medsupply.com"
        };

        for (int i = 0; i < supplierNames.length; i++) {
            Supplier supplier = new Supplier();
            supplier.setName(supplierNames[i]);
            supplier.setEmail(emails[i]);
            supplier.setPhone("+91-" + (8000000000L + i));
            supplier.setAddress("Building " + (i + 1) + ", Industrial Area");
            supplier.setCity(cities[i]);
            supplier.setCountry("India");
            supplier.setActive(true);
            suppliers.add(supplierRepository.save(supplier));
        }

        log.info("Created {} suppliers", suppliers.size());
        return suppliers;
    }

    private List<Medicine> createMedicines(List<Supplier> suppliers) {
        log.info("Creating medicines...");
        List<Medicine> medicines = new ArrayList<>();

        String[][] medicineData = {
            {"Paracetamol 500mg", "Paracetamol", "Cipla Ltd", "Analgesic", "Tablet", "500mg", "Pain relief and fever reducer", "5.00", "8.00", "50", "false"},
            {"Ibuprofen 400mg", "Ibuprofen", "Sun Pharma", "Analgesic", "Tablet", "400mg", "Anti-inflammatory pain reliever", "8.00", "12.00", "40", "false"},
            {"Amoxicillin 250mg", "Amoxicillin", "Dr. Reddy's", "Antibiotic", "Capsule", "250mg", "Broad spectrum antibiotic", "25.00", "35.00", "30", "true"},
            {"Azithromycin 500mg", "Azithromycin", "Lupin Ltd", "Antibiotic", "Tablet", "500mg", "Macrolide antibiotic", "45.00", "60.00", "25", "true"},
            {"Cetirizine 10mg", "Cetirizine", "Glenmark", "Antihistamine", "Tablet", "10mg", "Allergy and hay fever treatment", "3.00", "5.00", "60", "false"},
            {"Omeprazole 20mg", "Omeprazole", "Torrent Pharma", "Antacid", "Capsule", "20mg", "Gastric acid reducer", "12.00", "18.00", "35", "false"},
            {"Metformin 500mg", "Metformin", "Zydus Cadila", "Antidiabetic", "Tablet", "500mg", "Type 2 diabetes medication", "15.00", "22.00", "40", "true"},
            {"Atorvastatin 10mg", "Atorvastatin", "Mankind Pharma", "Cardiovascular", "Tablet", "10mg", "Cholesterol lowering drug", "20.00", "30.00", "30", "true"},
            {"Amlodipine 5mg", "Amlodipine", "Intas Pharma", "Cardiovascular", "Tablet", "5mg", "Blood pressure medication", "18.00", "25.00", "35", "true"},
            {"Salbutamol Inhaler", "Salbutamol", "Cipla Ltd", "Respiratory", "Inhaler", "100mcg", "Asthma and COPD treatment", "120.00", "150.00", "20", "true"},
            {"Cough Syrup 100ml", "Dextromethorphan", "Himalaya", "Cough & Cold", "Syrup", "100ml", "Cough suppressant", "85.00", "110.00", "25", "false"},
            {"Vitamin D3 60000IU", "Cholecalciferol", "Dabur", "Vitamin", "Capsule", "60000IU", "Vitamin D supplement", "25.00", "35.00", "30", "false"},
            {"Calcium 500mg", "Calcium Carbonate", "Unichem", "Mineral", "Tablet", "500mg", "Calcium supplement", "8.00", "12.00", "50", "false"},
            {"Multivitamin Tablets", "Multivitamin", "Himalaya", "Vitamin", "Tablet", "1 tablet", "Daily multivitamin supplement", "45.00", "65.00", "30", "false"},
            {"Aspirin 75mg", "Aspirin", "Bayer", "Cardiovascular", "Tablet", "75mg", "Blood thinner and pain relief", "10.00", "15.00", "40", "false"},
            {"Diclofenac Gel 30g", "Diclofenac", "Volini", "Topical", "Cream", "30g", "Topical pain relief gel", "55.00", "75.00", "20", "false"},
            {"Ciprofloxacin 500mg", "Ciprofloxacin", "Ranbaxy", "Antibiotic", "Tablet", "500mg", "Broad spectrum antibiotic", "35.00", "50.00", "25", "true"},
            {"Metronidazole 400mg", "Metronidazole", "Alkem", "Antibiotic", "Tablet", "400mg", "Antibacterial and antiprotozoal", "22.00", "32.00", "30", "true"},
            {"Folic Acid 5mg", "Folic Acid", "Emcure", "Vitamin", "Tablet", "5mg", "Folic acid supplement", "5.00", "8.00", "50", "false"},
            {"Iron Tablets 100mg", "Ferrous Fumarate", "Abbott", "Mineral", "Tablet", "100mg", "Iron supplement for anemia", "12.00", "18.00", "40", "false"}
        };

        for (int i = 0; i < medicineData.length; i++) {
            Medicine medicine = new Medicine();
            medicine.setName(medicineData[i][0]);
            medicine.setGenericName(medicineData[i][1]);
            medicine.setManufacturer(medicineData[i][2]);
            medicine.setCategory(medicineData[i][3]);
            medicine.setDosageForm(medicineData[i][4]);
            medicine.setStrength(medicineData[i][5]);
            medicine.setDescription(medicineData[i][6]);
            medicine.setUnitPrice(new BigDecimal(medicineData[i][7]));
            medicine.setSellingPrice(new BigDecimal(medicineData[i][8]));
            medicine.setReorderLevel(Integer.parseInt(medicineData[i][9]));
            medicine.setRequiresPrescription(Boolean.parseBoolean(medicineData[i][10]));
            medicine.setActive(true);
            
            // Assign random supplier
            medicine.setSupplier(suppliers.get(random.nextInt(suppliers.size())));
            
            medicines.add(medicineService.createMedicine(medicine));
        }

        log.info("Created {} medicines", medicines.size());
        return medicines;
    }

    private void createStockBatches(List<Medicine> medicines) {
        log.info("Creating stock batches...");
        int batchCount = 0;

        for (Medicine medicine : medicines) {
            // Create 1-3 batches per medicine
            int numBatches = 1 + random.nextInt(3);
            
            for (int i = 0; i < numBatches; i++) {
                StockBatch batch = new StockBatch();
                batch.setBatchNumber("BATCH-" + medicine.getId() + "-" + (i + 1) + "-" + 
                    LocalDate.now().getYear());
                
                int quantity = 50 + random.nextInt(200); // 50-250 units
                batch.setQuantity(quantity);
                batch.setRemainingQuantity(quantity);
                
                // Cost price is 70-80% of unit price
                BigDecimal costPrice = medicine.getUnitPrice()
                    .multiply(BigDecimal.valueOf(0.70 + random.nextDouble() * 0.10));
                batch.setCostPrice(costPrice);
                
                // Manufacturing date: 3-12 months ago
                LocalDate manufacturingDate = LocalDate.now().minusMonths(3 + random.nextInt(9));
                batch.setManufacturingDate(manufacturingDate);
                
                // Expiry date: 6-24 months from manufacturing
                LocalDate expiryDate = manufacturingDate.plusMonths(6 + random.nextInt(18));
                batch.setExpiryDate(expiryDate);
                
                batch.setActive(true);
                batch.setMedicine(medicine);
                
                stockBatchService.createBatch(batch);
                batchCount++;
            }
        }

        log.info("Created {} stock batches", batchCount);
    }

    private List<Customer> createCustomers() {
        log.info("Creating customers...");
        List<Customer> customers = new ArrayList<>();

        String[][] customerData = {
            {"Ramesh Patel", "ramesh.patel@email.com", "+91-9876543201", "123, MG Road, Mumbai", "PAN123456"},
            {"Sita Devi", "sita.devi@email.com", "+91-9876543202", "456, Connaught Place, Delhi", "PAN234567"},
            {"Amit Kumar", "amit.kumar@email.com", "+91-9876543203", "789, Brigade Road, Bangalore", "PAN345678"},
            {"Priya Singh", "priya.singh@email.com", "+91-9876543204", "321, Anna Salai, Chennai", "PAN456789"},
            {"Vikram Reddy", "vikram.reddy@email.com", "+91-9876543205", "654, Hitech City, Hyderabad", "PAN567890"},
            {"Anjali Sharma", "anjali.sharma@email.com", "+91-9876543206", "987, Koregaon Park, Pune", "PAN678901"},
            {"Mohammed Ali", "mohammed.ali@email.com", "+91-9876543207", "147, Park Street, Kolkata", "PAN789012"},
            {"Kavita Nair", "kavita.nair@email.com", "+91-9876543208", "258, MG Road, Kochi", "PAN890123"},
            {"Rajesh Iyer", "rajesh.iyer@email.com", "+91-9876543209", "369, Commercial Street, Bangalore", "PAN901234"},
            {"Sunita Mehta", "sunita.mehta@email.com", "+91-9876543210", "741, Linking Road, Mumbai", "PAN012345"}
        };

        for (String[] data : customerData) {
            Customer customer = new Customer();
            customer.setName(data[0]);
            customer.setEmail(data[1]);
            customer.setPhone(data[2]);
            customer.setAddress(data[3]);
            customer.setIdNumber(data[4]);
            customer.setActive(true);
            customers.add(customerRepository.save(customer));
        }

        log.info("Created {} customers", customers.size());
        return customers;
    }

    private void createOrders(List<Pharmacist> pharmacists, List<Customer> customers, List<Medicine> medicines) {
        log.info("Creating orders...");
        int orderCount = 0;

        // Create 15-20 orders
        for (int i = 0; i < 18; i++) {
            Orders order = new Orders();
            order.setOrderNumber("ORD-" + String.format("%06d", i + 1));
            
            // Random customer (80% chance) or null for walk-in
            if (random.nextDouble() < 0.8) {
                order.setCustomer(customers.get(random.nextInt(customers.size())));
            }
            
            // Random pharmacist
            order.setPharmacist(pharmacists.get(random.nextInt(pharmacists.size())));
            
            // Create 1-4 order items
            List<OrderItem> orderItems = new ArrayList<>();
            int numItems = 1 + random.nextInt(4);
            List<Medicine> selectedMedicines = new ArrayList<>(medicines);
            
            for (int j = 0; j < numItems && !selectedMedicines.isEmpty(); j++) {
                Medicine medicine = selectedMedicines.remove(random.nextInt(selectedMedicines.size()));
                
                // Check if medicine has inventory
                Inventory inventory = inventoryRepository.findByMedicine(medicine).orElse(null);
                if (inventory == null || inventory.getAvailableQuantity() == 0) {
                    continue; // Skip if no stock
                }
                
                int quantity = 1 + random.nextInt(Math.min(5, inventory.getAvailableQuantity()));
                
                OrderItem item = new OrderItem();
                item.setMedicine(medicine);
                item.setQuantity(quantity);
                item.setUnitPrice(medicine.getSellingPrice());
                item.setTotalPrice(medicine.getSellingPrice().multiply(BigDecimal.valueOf(quantity)));
                
                // Assign batch if available
                List<StockBatch> batches = stockBatchRepository.findByMedicineAndActive(medicine, true);
                if (!batches.isEmpty()) {
                    item.setBatch(batches.get(0));
                }
                
                orderItems.add(item);
            }
            
            if (orderItems.isEmpty()) {
                continue; // Skip if no items
            }
            
            order.setOrderItems(orderItems);
            
            // Calculate totals
            BigDecimal subtotal = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            order.setSubtotal(subtotal);
            order.setTax(subtotal.multiply(BigDecimal.valueOf(0.05))); // 5% tax
            order.setDiscount(BigDecimal.ZERO);
            order.setTotalAmount(subtotal.add(order.getTax()));
            
            // Random status: 70% completed, 20% pending, 10% cancelled
            double statusRand = random.nextDouble();
            if (statusRand < 0.7) {
                order.setStatus(Orders.OrderStatus.COMPLETED);
                order.setPaid(true);
            } else if (statusRand < 0.9) {
                order.setStatus(Orders.OrderStatus.PENDING);
                order.setPaid(false);
            } else {
                order.setStatus(Orders.OrderStatus.CANCELLED);
                order.setPaid(false);
            }
            
            // Random payment method
            Orders.PaymentMethod[] methods = Orders.PaymentMethod.values();
            order.setPaymentMethod(methods[random.nextInt(methods.length)]);
            
            // Set created date (some orders in the past)
            if (i < 15) {
                order.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
            } else {
                order.setCreatedAt(LocalDateTime.now().minusHours(random.nextInt(24)));
            }
            
            try {
                orderService.createOrder(order);
                orderCount++;
            } catch (Exception e) {
                log.warn("Failed to create order {}: {}", order.getOrderNumber(), e.getMessage());
            }
        }

        log.info("Created {} orders", orderCount);
    }
}
