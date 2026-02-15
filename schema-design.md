## 📘 Okuma: JPA ve Anotasyonlar: Örnekler 

**Tahmini süre:** 5 dk 

---

## 🎯 Öğrenme Hedefleri 

Bu okumanın sonunda şunları yapabileceksiniz: 

* Java Persistence API’yi (*JPA*) açıklamak
* `@Entity`, `@Id`, `@GeneratedValue` ve `@Transient` dâhil önemli anotasyonları uygulamak 

---

## 🧩 Giriş 

Java Persistence API (*JPA*), Java’da nesne-ilişkisel eşleme (*ORM*) için standart API’dir. Geliştiricilerin Java sınıflarını veritabanı tablolarına eşlemesine olanak tanır; böylece SQL gerekmeksizin basit Java nesneleri kullanarak ilişkisel verilerle etkileşim kurabilirsiniz. 

JPA yalnızca bir spesifikasyondur; bu nedenle bir implementasyona ihtiyaç duyar. Çoğu Spring Boot projesinde varsayılan implementasyon *Hibernate*’dir. 

JPA ile, `INSERT`, `UPDATE` veya `SELECT` sorgularını manuel olarak yazmak yerine `Doctor`, `Patient` ve `Appointment` gibi anotasyonlarla işaretlenmiş sınıflar oluşturursunuz; gerisini *Spring Data JPA* halleder. 

---

## ✅ Veri Doğrulama Neden Önemlidir? 

Veri veritabanına ulaşmadan önce ve iş mantığında kullanılmasından çok daha önce, verinin geçerli, eksiksiz ve güvenli olduğundan emin olmak istersiniz. Doğrulama kuralları şunları zorunlu kılar: 

* **Gerekli alanlar**
  Örnek: Kullanıcı adı null olmamalıdır
* **Biçim kuralları**
  Örnek: E-posta geçerli olmalıdır
* **Uzunluk kısıtları**
  Örnek: İsim 3–100 karakter olmalıdır
* **Mantıksal kısıtlar**
  Örnek: Tarih gelecekte olmalıdır 

Bu, uygulamanızı kötü veriden korur, hataları azaltır ve kullanıcı deneyimini iyileştirir. 

Spring Boot, bu kuralları anotasyonlar üzerinden uygulamak için *Bean Validation API*’nin (javax.validation veya jakarta.validation) referans implementasyonu olan *Hibernate Validator*’u kullanır. 

---

## 🏷️ Temel Anotasyonlar 

| Anotasyon                   | Amaç                                                            |
| --------------------------- | --------------------------------------------------------------- |
| `@Entity`                   | Sınıfı veritabanına eşlenen bir varlık (entity) olarak bildirir |
| `@Id`                       | Birincil anahtarı işaretler                                     |
| `@GeneratedValue`           | Birincil anahtarı otomatik üretir                               |
| `@NotNull`                  | Alan null olmamalıdır                                           |
| `@Size(min, max)`           | String uzunluk kısıtı                                           |
| `@Email`                    | Alan geçerli bir e-posta adresi olmalıdır                       |
| `@Pattern(regex)`           | Alan verilen düzenli ifadeyle eşleşmelidir                      |
| `@JsonProperty(WRITE_ONLY)` | Hassas alanları (parolalar) JSON çıktısından gizler             |
| `@ManyToOne`                | Çoktan-bire ilişkiyi tanımlar                                   |
| `@Future`                   | Tarih/saat gelecekte olmalıdır                                  |
| `@Transient`                | Alan veritabanına kaydedilmemelidir                             |

---

## 👤 Örnek 1: Admin Modeli 

Bu basit model, bir varlığın nasıl tanımlanacağını ve hassas verinin nasıl korunacağını gösterir: 

```java
@Entity
public class Admin {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 @NotNull(message = "Username cannot be null")
 private String username;
 @NotNull
 @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
 private String password;
 // Getters and setters
}
```

* `@Entity` bu sınıfı bir veritabanı tablosuna eşler
* `@JsonProperty(WRITE_ONLY)` parolanın API yanıtlarında asla açığa çıkmamasını sağlar
* `@NotNull` gerekli alanların boş bırakılmamasını sağlar 

---

## 🩺 Örnek 2: Validasyonlu Doctor 

```java
@Entity
public class Doctor {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 @NotNull
 @Size(min = 3, max = 100)
 private String name;
 @NotNull
 @Size(min = 3, max = 50)
 private String specialty;
 @Email
 @NotNull
 private String email;
 @Size(min = 6)
 @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
 private String password;
 @Pattern(regexp = "\\d{10}")
 private String phone;
 @ElementCollection
 private List<String> availableTimes;
}
```

Alan seviyesinde doğrulama, veri kalitesini sağlar. `@Email`, `@Size` ve `@Pattern` yapı (structure) uygular. `@ElementCollection`, string gibi basit değerlerin bir listesini ayrı bir tabloda saklamak için kullanılır. 

---

## 🗓️ Örnek 3: İlişkiler ve Transient Mantık ile Appointment 

```java
@Entity
public class Appointment {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 @ManyToOne
 @NotNull
 private Doctor doctor;
 @ManyToOne
 @NotNull
 private Patient patient;
 @Future
 private LocalDateTime appointmentTime;
 private int status; // 0 = Scheduled, 1 = Completed
 @Transient
 public LocalDateTime getEndTime() {
 return appointmentTime.plusHours(1);
 }
}
```

* `@ManyToOne`, randevu ile hem doktor hem de hasta arasındaki ilişkiyi tanımlar
* `@Future`, planlama için mantıksal kısıtları zorunlu kılar
* `@Transient`, `getEndTime()` metodunun veritabanına kaydedilmesini engeller 

---

## 📄 MongoDB Desteği: @Document Kullanımı 

JPA ilişkisel veritabanlarıyla çalışırken, Spring Boot *Spring Data MongoDB* aracılığıyla MongoDB gibi NoSQL veritabanlarını da destekler. `@Entity` yerine `@Document` kullanırsınız. 

```java
@Document(collection = "prescriptions")
public class Prescription {
 @Id
 private String id;
 @NotNull
 @Size(min = 3, max = 100)
 private String patientName;
 @NotNull
 private Long appointmentId;
 @NotNull
 @Size(min = 3, max = 100)
 private String medication;
 @Size(max = 200)
 private String doctorNotes;
}
```

MongoDB bunu JSON benzeri bir formatta belge (*document*) olarak saklar. JPA modellerindekiyle aynı doğrulama anotasyonlarını kullanmaya devam edersiniz. 

---

## 🔐 Validasyon ve Serileştirme Güvenliği 

Veriyi bir REST API üzerinden dışarı açarken, parola gibi hassas alanları güvence altına almak kritiktir. Şunu kullanın: 

```java
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
private String password;
```

Bu, alanın `POST` ile set edilmesine izin verir; ancak `GET` yanıtında asla döndürülmez. 

Ayrıca, iç alanların JSON’a serileştirilmesini tamamen engellemek için `@JsonIgnore` kullanabilirsiniz. 

---

## 🧾 Özet 

Bu okumada şunları öğrendiniz: 

* Model sınıflarını oluşturma ve doğrulama:

  * Varlıkları ilişkisel tablolara ve MongoDB belgelerine eşleme
  * `@ManyToOne` gibi varlık ilişkilerini tanımlama
  * `@NotNull`, `@Email` ve `@Pattern` gibi anotasyonlarla kullanıcı girdisini doğrulama
  * `@Transient` kullanarak mantıksal alanları hariç tutma
  * JSON serileştirme anotasyonlarıyla hassas alanları güvence altına alma 

Bu desenler, gerçek dünya Java backend uygulamalarında temiz, güvenli ve tutarlı veri modellerinin belkemiğini oluşturur. 

---

## 👥 Yazar(lar) 

Upkar Lidder
IBM Skills Network Team 
# Smart Clinic Management System - Database Schema Design

## MySQL Database Design

This section documents the relational database schema for the Smart Clinic Management System. All structured, transactional data is stored in MySQL with proper normalization, constraints, and relationship management.

### **Core Tables Overview**

The MySQL database consists of the following core tables:

1. **admin** - System administrators who manage doctors and platform
2. **patient** - Patient user accounts and contact information
3. **doctor** - Doctor profiles with specialty and availability
4. **appointment** - Appointment bookings linking patients and doctors

---

### **Table 1: admin**

**Purpose:** Store administrator credentials for system management

```sql
CREATE TABLE admin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Column Descriptions:**
| Column | Type | Constraints | Purpose |
|--------|------|-----------|---------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique admin identifier |
| username | VARCHAR(100) | NOT NULL, UNIQUE | Login username |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Administrator email address |
| password | VARCHAR(255) | NOT NULL | Hashed password (bcrypt or similar) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Account creation timestamp |
| updated_at | TIMESTAMP | AUTO UPDATE | Last modification timestamp |

**Design Decisions:**

- Username and email are UNIQUE to prevent duplicate admin accounts
- Password stored as hashed value (255 chars accommodates bcrypt hash)
- Timestamps track account lifecycle for audit purposes

---

### **Table 2: patient**

**Purpose:** Store patient user information and contact details

```sql
CREATE TABLE patient (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  phone VARCHAR(10) NOT NULL,
  address VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Column Descriptions:**
| Column | Type | Constraints | Purpose |
|--------|------|-----------|---------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique patient identifier |
| name | VARCHAR(100) | NOT NULL | Patient full name (3-100 chars validated in app) |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Patient email (unique to prevent duplicate registrations) |
| password | VARCHAR(255) | NOT NULL | Hashed password |
| phone | VARCHAR(10) | NOT NULL | Phone number (10 digits, validated in app) |
| address | VARCHAR(255) | NULLABLE | Patient home address (up to 255 chars) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration timestamp |
| updated_at | TIMESTAMP | AUTO UPDATE | Last profile update |

**Design Decisions:**

- Email is UNIQUE to enforce one account per email
- Phone is VARCHAR(10) since validation happens in application layer
- Address is optional (NULLABLE) for future flexibility
- All user fields stored (future enhancement: separate "Profile" table if needed)

---

### **Table 3: doctor**

**Purpose:** Store doctor profiles with specialization and availability

```sql
CREATE TABLE doctor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  specialty VARCHAR(50) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  phone VARCHAR(10) NOT NULL,
  available_times JSON,
  status ENUM('active', 'inactive') DEFAULT 'active',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Column Descriptions:**
| Column | Type | Constraints | Purpose |
|--------|------|-----------|---------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique doctor identifier |
| name | VARCHAR(100) | NOT NULL | Doctor full name |
| specialty | VARCHAR(50) | NOT NULL | Medical specialty (e.g., Cardiology, Neurology) |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Doctor email (unique per doctor) |
| password | VARCHAR(255) | NOT NULL | Hashed password |
| phone | VARCHAR(10) | NOT NULL | Contact phone number |
| available_times | JSON | NULLABLE | JSON array of available time slots (e.g., ["09:00-10:00", "10:00-11:00"]) |
| status | ENUM | DEFAULT 'active' | Account status (active/inactive for soft deletes) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Profile creation timestamp |
| updated_at | TIMESTAMP | AUTO UPDATE | Last update timestamp |

**Design Decisions:**

- `available_times` uses JSON type for flexibility (allows multiple time slots per day)
- `status` ENUM allows soft delete (mark inactive instead of physical deletion)
- Specialty is VARCHAR(50) for common specialties
- Email is UNIQUE to match one doctor per account

---

### **Table 4: appointment**

**Purpose:** Store appointment bookings linking patients with doctors

```sql
CREATE TABLE appointment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  appointment_time DATETIME NOT NULL,
  status INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
  FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE RESTRICT
);
```

**Column Descriptions:**
| Column | Type | Constraints | Purpose |
|--------|------|-----------|---------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique appointment identifier |
| patient_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to patient (ON DELETE CASCADE) |
| doctor_id | BIGINT | NOT NULL, FOREIGN KEY | Reference to doctor (ON DELETE RESTRICT) |
| appointment_time | DATETIME | NOT NULL | Scheduled appointment date and time |
| status | INT | DEFAULT 0 | 0=scheduled, 1=completed, 2=cancelled |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Booking creation time |
| updated_at | TIMESTAMP | AUTO UPDATE | Last modification time |

**Foreign Key Relationships:**

- `patient_id` → `patient(id)` ON DELETE CASCADE: If patient deleted, appointments are cascade deleted
- `doctor_id` → `doctor(id)` ON DELETE RESTRICT: Prevent deletion of doctor with active appointments

**Design Decisions:**

- Appointment duration is fixed at 1 hour (can be derived from appointment_time)
- Status as INT (0/1/2) allows flexibility for future status additions
- CASCADE delete on patient (patients can be deleted, appointments removed)
- RESTRICT on doctor (don't delete doctors with pending appointments)
- DATETIME stores both date and time for exact scheduling

---

### **Optional Tables for Future Enhancement**

**clinic_location Table** (Future)

```sql
CREATE TABLE clinic_location (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  location_name VARCHAR(100) NOT NULL,
  address VARCHAR(255) NOT NULL,
  phone VARCHAR(10),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**payment Table** (Future)

```sql
CREATE TABLE payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  appointment_id BIGINT NOT NULL UNIQUE,
  amount DECIMAL(10, 2) NOT NULL,
  payment_method VARCHAR(50),
  status ENUM('pending', 'completed', 'failed') DEFAULT 'pending',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE
);
```

---

### **Design Decisions & Rationale**

#### **1. What happens if a patient is deleted?**

- **Decision:** ON DELETE CASCADE on appointment table
- **Rationale:** When a patient account is removed, all associated appointments are also deleted (patient cancels account)
- **Risk:** Data loss of historical appointments; mitigation could be soft-delete approach

#### **2. Should a doctor be allowed to have overlapping appointments?**

- **Decision:** NO overlapping appointments allowed
- **Rationale:** One doctor cannot be in two places at once
- **Implementation:** Business logic in application layer checks `appointment_time` availability before booking
- **Validation:** Query checks if doctor has appointment at same time before insert

#### **3. Why NOT NULL constraints?**

- **patient_id, doctor_id:** Every appointment must reference valid patient and doctor
- **phone:** Contact required for patients and doctors
- **appointment_time:** Appointment must have scheduled time

#### **4. Why UNIQUE constraints on email?**

- Prevents multiple accounts with same email
- Simplifies login queries (one email = one account)

#### **5. Why JSON for available_times?**

- Doctor may have multiple time slots per day
- Flexible without needing separate table
- Easy to parse in application code

---

### **Indexing Strategy**

For performance optimization, consider adding:

```sql
CREATE INDEX idx_patient_email ON patient(email);
CREATE INDEX idx_doctor_email ON doctor(email);
CREATE INDEX idx_doctor_specialty ON doctor(specialty);
CREATE INDEX idx_appointment_patient ON appointment(patient_id);
CREATE INDEX idx_appointment_doctor ON appointment(doctor_id);
CREATE INDEX idx_appointment_time ON appointment(appointment_time);
CREATE INDEX idx_appointment_status ON appointment(status);
```

---

## MongoDB Collection Design

This section documents the NoSQL (document-based) database schema for unstructured and flexible data in the Smart Clinic Management System. MongoDB is ideal for prescriptions where format may vary, notes can be extensive, and schema evolution is important.

---

### **Why MongoDB for Prescriptions?**

**Trade-off Analysis:**

- **Structured data (SQL):** Appointments are relational, predictable, and require constraints
- **Flexible data (NoSQL):** Prescriptions have variable fields, extensive notes, and evolving formats
- **Decision:** MongoDB for prescriptions because:
  - Doctors add varying types of notes and instructions
  - Prescription format may change based on medication type
  - Easy to add new fields (dosage frequency, side effects, contraindications) without schema migration
  - Better performance for document retrieval without JOIN operations

---

### **Collection 1: prescriptions**

**Purpose:** Store prescription records created by doctors for patients during or after appointments

**Document Structure (JSON):**

```json
{
  "_id": ObjectId,
  "appointmentId": 12345,
  "patientName": "John Doe",
  "patientEmail": "john@example.com",
  "doctorName": "Dr. Sarah Smith",
  "doctorId": 5,
  "medication": "Amoxicillin",
  "dosage": "500mg",
  "frequency": "Three times daily",
  "duration": "7 days",
  "startDate": ISODate("2025-02-15"),
  "endDate": ISODate("2025-02-22"),
  "instructions": "Take with food. Do not skip doses.",
  "sideEffects": [
    "Mild nausea",
    "Possible allergic reaction - seek immediate care"
  ],
  "contraindications": [
    "Penicillin allergies",
    "Concurrent use with methotrexate"
  ],
  "doctorNotes": "Patient showed improvement. If symptoms persist after 5 days, return for follow-up.",
  "refillable": true,
  "refillCount": 2,
  "createdAt": ISODate("2025-02-15T10:30:00Z"),
  "updatedAt": ISODate("2025-02-15T10:30:00Z")
}
```

---

### **Field Descriptions**

| Field             | Type     | Description                                       | Example                              |
| ----------------- | -------- | ------------------------------------------------- | ------------------------------------ |
| \_id              | ObjectId | MongoDB auto-generated unique identifier          | ObjectId("507f1f77bcf86cd799439011") |
| appointmentId     | Long     | Reference to appointment in MySQL                 | 12345                                |
| patientName       | String   | Patient full name (denormalized from MySQL)       | "John Doe"                           |
| patientEmail      | String   | Patient email (denormalized for quick access)     | "john@example.com"                   |
| doctorName        | String   | Doctor name (denormalized for readability)        | "Dr. Sarah Smith"                    |
| doctorId          | Long     | Reference to doctor in MySQL                      | 5                                    |
| medication        | String   | Name of prescribed medication                     | "Amoxicillin"                        |
| dosage            | String   | Dosage strength and form                          | "500mg"                              |
| frequency         | String   | How often to take medication                      | "Three times daily"                  |
| duration          | String   | How long to take medication                       | "7 days"                             |
| startDate         | ISODate  | When patient should start medication              | "2025-02-15"                         |
| endDate           | ISODate  | When medication course ends                       | "2025-02-22"                         |
| instructions      | String   | How to take medication (with food, etc.)          | "Take with food"                     |
| sideEffects       | Array    | List of possible side effects                     | ["Mild nausea", "Allergic reaction"] |
| contraindications | Array    | Medical conditions/medications to avoid with this | ["Penicillin allergies"]             |
| doctorNotes       | String   | Additional doctor observations and instructions   | "Follow up if symptoms persist"      |
| refillable        | Boolean  | Whether prescription can be refilled              | true                                 |
| refillCount       | Integer  | Number of refills remaining                       | 2                                    |
| createdAt         | ISODate  | Prescription creation timestamp (UTC)             | "2025-02-15T10:30:00Z"               |
| updatedAt         | ISODate  | Last update timestamp                             | "2025-02-15T10:30:00Z"               |

---

### **Design Decisions & Rationale**

#### **1. Should each doctor have their own available time slots?**

**Design Choice:** YES - Stored in MySQL doctor table as JSON array

**Rationale:**

- A doctor's availability is core operational data (structured, relational)
- Used frequently for appointment booking validation
- Benefits from MySQL constraints and querying capabilities
- Example: `available_times: ["09:00-10:00", "10:00-11:00", "14:00-15:00"]`
- Each appointment booking validates against this list

**Implementation:**

```sql
-- In doctor table
available_times JSON COMMENT 'Array of time slots available for booking'
```

```javascript
// In application code - validate availability
const doctorAvailableTimes = JSON.parse(doctor.available_times);
const isSlotAvailable = doctorAvailableTimes.includes(requestedTime);
```

**Future Enhancement:** Separate `doctor_availability` table for recurring schedule management

---

#### **2. Should a patient's past appointment history be retained forever?**

**Design Choice:** YES - Keep all appointments in MySQL (soft delete if needed)

**Rationale:**

- **Medical Records:** Appointment history is critical for patient care continuity
- **Legal Compliance:** Healthcare regulations (HIPAA) often require record retention
- **Analytics:** Historical data enables usage reports and insights
- **Patient Safety:** Doctors need to see previous visits and treatments

**Implementation Approach:**

```sql
-- Keep appointment history with status tracking
-- status: 0 = scheduled, 1 = completed, 2 = cancelled
-- All records preserved regardless of status
```

**Data Retention Policy:**

- Soft delete: Mark patient as inactive but keep all appointment/prescription records
- Retention: Keep historical data for minimum 7-10 years (per general healthcare standards)
- Archive: Move very old records to archive table after X years if needed

**Query Example:**

```sql
-- Show complete patient history
SELECT * FROM appointment
WHERE patient_id = 123
ORDER BY appointment_time DESC
-- Shows all appointments including past, completed, and cancelled
```

---

#### **3. Is a prescription tied to a specific appointment or can it exist independently?**

**Design Choice:** TIED to appointment - prescriptions are appointment-outcome documents

**Relationship:**

```
Appointment (MySQL) ──1:N──> Prescription (MongoDB)
appointmentId (FK)           appointmentId (Reference)
```

**Rationale:**

- A prescription is generated **during or after an appointment**
- Legal/medical requirement: prescription must document which appointment it's for
- Enables audit trail: "Which appointment led to this prescription?"
- Prevents orphaned prescriptions without patient context
- Doctor needs appointment details to create relevant prescriptions

**Implementation:**

- `appointmentId` is REQUIRED field in every prescription document (NOT NULL)
- Appointment must exist before prescription can be created
- If appointment is cancelled, prescription behavior needs business logic:
  - **Option A:** Mark prescription as cancelled
  - **Option B:** Keep prescription but flag appointment as superseded
  - **Option C:** Allow doctor to manually revoke if needed

**Database Referential Integrity:**

```javascript
// MongoDB validation schema
{
  validator: {
    $jsonSchema: {
      required: ["appointmentId"],
      properties: {
        appointmentId: {
          bsonType: "long",
          description: "Required reference to appointment"
        }
      }
    }
  }
}
```

---

### **Denormalization in MongoDB**

Notice that the prescription document contains fields like `patientName`, `patientEmail`, `doctorName` - these are intentional denormalizations (copied from MySQL).

**Why Denormalize?**

- **Read Performance:** No need to JOIN with MySQL to display prescription
- **Historical Records:** If patient/doctor name changes in MySQL, prescription preserves original context
- **Self-Contained:** Prescription document is complete without external lookups
- **Audit Trail:** Shows exactly who prescribed to whom at that moment

**Trade-off:**

- Write Complexity: Must update prescription if doctor name changes (rarely happens)
- Storage: Slightly larger documents due to redundant fields
- Benefit: Much faster reads and historical accuracy

---

### **MongoDB Indexing Strategy**

For optimized queries, create these indexes:

```javascript
db.prescriptions.createIndex({ appointmentId: 1 });
db.prescriptions.createIndex({ patientEmail: 1 });
db.prescriptions.createIndex({ doctorId: 1 });
db.prescriptions.createIndex({ createdAt: -1 });
db.prescriptions.createIndex({ patientName: "text" }); // Text search on patient name
```

---

### **Cross-Database Transactions**

**Scenario:** Patient deletes appointment. What happens to prescription?

**Design Decision:** Handle in application logic

```
1. Patient deletes appointment (MySQL)
   → appointment.status = 2 (cancelled)

2. Application logic:
   → Query MongoDB for prescriptions with appointmentId
   → Flag prescriptions as "associated with cancelled appointment"
   → Doctor can still view context but system indicates cancellation

3. No hard deletion from either database
   → Maintains audit trail
   → Preserves medical history
```

---

### **Summary: MySQL vs MongoDB**

| Concern           | MySQL (Structured)         | MongoDB (Flexible)          |
| ----------------- | -------------------------- | --------------------------- |
| **Patient Data**  | ✓ Clean, validated         | ✗ Would be redundant        |
| **Appointments**  | ✓ Relational, constrained  | ✗ Would lose validation     |
| **Prescriptions** | ✗ Rigid schema, wasteful   | ✓ Flexible, document-based  |
| **Availability**  | ✓ Frequent updates/queries | ✗ Overly complex NoSQL      |
| **Notes/History** | ✗ Text fields waste space  | ✓ Natural fit for documents |

---
