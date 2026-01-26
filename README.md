
# **PharmaEase – Pharmacy Management System**

PharmaEase is a complete, real-world **Pharmacy Management System** designed for managing medicines, billing, inventory, pharmacists, customers, orders, and sales reporting.
Built with a **Java Springboot backend**, **MySQL database**, and a **clean, minimal frontend using Tailwind CSS**, PharmaEase delivers a smooth, professional, and modern user experience.

---

## 🚀 **Features**

### **👥 User Management**

* Admin & Pharmacist login
* Admin can register new pharmacists
* Secure authentication & password hashing

### **💊 Medicine Management**

* Add, view, search, update, and delete medicines
* Track brands, generics, stock, and prices
* Real-time inventory updates

### **🧾 Billing System**

* Generate bills instantly
* Auto-calculation of price, quantity, and totals
* Taxes & discounts (future scope)
* Printable invoice (upcoming)

### **📦 Inventory Management**

* Stock monitoring
* Alerts for low stock
* Batch update options

### **📊 Reports & Dashboard**

* Daily/Monthly sales tracking
* Inventory analytics (future upgrade)
* Clean, dashboard-style UI

### **🎨 UI / UX**

* Tailwind-based minimal, professional design
* Fully responsive layout
* Smooth transitions & clean color palette

---

## 🛠️ **Tech Stack**

### **Frontend**

* HTML5
* CSS3 (Tailwind CSS)
* JavaScript
* Thymeleaf Template

### **Backend**

* Java Springboot

### **Database**

* MySQL Workbench

### **IDE Used**

* IntelliJ IDEA Ultimate

---

## 📂 **Project Structure**

```
pharmaease/
├── src/
│   ├── main/
│   │   ├── java/com/pharmaease/
│   │   │   ├── servlets/
│   │   │   ├── dao/
│   │   │   ├── models/
│   │   │   └── util/DatabaseConnection.java
│   │   ├── webapp/
│   │   │   ├── WEB-INF/
│   │   │   ├── views/
│   │   │   └── assets/
│   │   │       ├── css/
│   │   │       └── js/
└── README.md
```

---

## 🗄️ **Database Schema**

### **Pharmacist Table**

| Field        | Type             | Description        |
| ------------ | ---------------- | ------------------ |
| pharmacistId | INT (PK, AI)     | Unique ID          |
| name         | VARCHAR          | Pharmacist name    |
| email        | VARCHAR (unique) | Login email        |
| passwordHash | VARCHAR          | Encrypted password |

### **Medicines Table**

| Field       | Type         | Description |
| ----------- | ------------ | ----------- |
| medicineId  | INT (PK, AI) |             |
| name        | VARCHAR      |             |
| brand       | VARCHAR      |             |
| genericName | VARCHAR      |             |
| price       | DECIMAL      |             |
| stock       | INT          |             |

### **Orders Table**

| Field        | Type         |
| ------------ | ------------ |
| orderId      | INT (PK, AI) |
| customerName | VARCHAR      |
| totalAmount  | DECIMAL      |
| orderDate    | TIMESTAMP    |

---

## 🔧 **How to Run the Project**

### **1️⃣ Clone the Repository**

```
git clone https://github.com/amancodes12/pharmaease.git
```

### **2️⃣ Import Backend in IntelliJ IDEA**

* Open → New Project from Existing Sources
* Select the project folder
* Add Apache Tomcat server

### **3️⃣ Set Up MySQL**

* Open MySQL Workbench
* Run the SQL script included in `/database/` or copy from README
* Update credentials in `DatabaseConnection.java`

### **4️⃣ Start Tomcat Server**

Run the project and open:

```
http://localhost:8080/pharmaease
```

---

## 🧭 **Current Progress**

* Medicine module frontend completed
* Billing UI with auto-calculation added
* Database set up in MySQL
* Backend is in development

---

## 🛠️ **Future Enhancements**

* Invoice PDF download
* GST & discount handling
* Supplier management
* Mobile version
* Real-time analytics dashboard

---

## 👤 **Author**
**Krishna Yadav**
**Aman Yadav**
Final-Year IT Student

* GitHub: [https://github.com/krishna7275](https://github.com/krishna7275)
* LinkedIn: [https://www.linkedin.com/in/krishna7275](https://www.linkedin.com/in//krishna7275)
* GitHub: [https://github.com/amancodes12](https://github.com/amancodes12)
* LinkedIn: [https://www.linkedin.com/in/aman-yadav-01040534b/](https://www.linkedin.com/in/aman-yadav-01040534b/)
