package com.project.back_end.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Prescription Model Class (MongoDB Document)
 * 
 * Represents a medication prescription issued during an appointment in the Smart Clinic 
 * Management System. Prescriptions are stored as flexible JSON-like documents in MongoDB,
 * allowing for variable schema and detailed medication instructions.
 * 
 * Spring Data MongoDB Document mapped to the 'prescriptions' collection.
 */
@Document(collection = "prescriptions")
public class Prescription {

    // ===== Fields =====

    /**
     * Unique identifier for the Prescription document
     * 
     * MongoDB auto-generated ObjectId stored as String.
     * Serves as the primary key (_id field) in the MongoDB collection.
     * Auto-generated on document creation if not provided.
     */
    @Id
    private String id;

    /**
     * Patient's full name receiving the prescription
     * 
     * Required field with length constraints.
     * Denormalized from Patient table for document completeness and search capabilities.
     * Must be between 3 and 100 characters.
     */
    @NotNull(message = "Patient name cannot be null")
    @Size(min = 3, max = 100, message = "Patient name must be between 3 and 100 characters")
    private String patientName;

    /**
     * Reference to the associated appointment
     * 
     * Required field linking this prescription to a specific appointment.
     * Uses Long to match the appointmentId from the Appointment entity in MySQL.
     * Enables referential integrity and audit trail.
     */
    @NotNull(message = "Appointment ID cannot be null")
    private Long appointmentId;

    /**
     * Name of the prescribed medication
     * 
     * Required field with length constraints.
     * Examples: Amoxicillin, Paracetamol, Ibuprofen, etc.
     * Must be between 3 and 100 characters.
     */
    @NotNull(message = "Medication cannot be null")
    @Size(min = 3, max = 100, message = "Medication name must be between 3 and 100 characters")
    private String medication;

    /**
     * Dosage information for the prescribed medication
     * 
     * Required field with length constraints.
     * Examples: "500mg", "1 tablet", "2 capsules", "5ml 3x daily", etc.
     * Must be between 3 and 20 characters.
     */
    @NotNull(message = "Dosage cannot be null")
    @Size(min = 3, max = 20, message = "Dosage must be between 3 and 20 characters")
    private String dosage;

    /**
     * Optional notes and instructions from the doctor
     * 
     * Optional field for additional guidance, warnings, or special instructions.
     * Examples: "Take with food", "Do not exceed dosage", etc.
     * Cannot exceed 200 characters.
     */
    @Size(max = 200, message = "Doctor notes must not exceed 200 characters")
    private String doctorNotes;

    /**
     * Timestamp when the prescription was created
     * 
     * Auto-populated timestamp for audit trail and record tracking.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the prescription was last updated
     * 
     * Auto-populated timestamp for tracking modifications.
     */
    private LocalDateTime updatedAt;


    // ===== Constructors =====

    /**
     * No-argument constructor
     * 
     * Required for MongoDB document instantiation and serialization.
     */
    public Prescription() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Parameterized constructor with core fields
     * 
     * Creates a new Prescription with essential medication information.
     * 
     * @param patientName the patient's full name
     * @param appointmentId the reference to the appointment
     * @param medication the name of the prescribed medication
     * @param dosage the dosage information
     */
    public Prescription(String patientName, Long appointmentId, String medication, String dosage) {
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.medication = medication;
        this.dosage = dosage;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Full parameterized constructor
     * 
     * Creates a new Prescription with all fields including optional doctor notes.
     * 
     * @param patientName the patient's full name
     * @param appointmentId the reference to the appointment
     * @param medication the name of the prescribed medication
     * @param dosage the dosage information
     * @param doctorNotes optional notes from the doctor
     */
    public Prescription(String patientName, Long appointmentId, String medication, String dosage, String doctorNotes) {
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.medication = medication;
        this.dosage = dosage;
        this.doctorNotes = doctorNotes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Complete parameterized constructor
     * 
     * Creates a new Prescription with all fields including id and timestamps.
     * Typically used during database retrieval operations.
     * 
     * @param id the MongoDB document id
     * @param patientName the patient's full name
     * @param appointmentId the reference to the appointment
     * @param medication the name of the prescribed medication
     * @param dosage the dosage information
     * @param doctorNotes optional notes from the doctor
     * @param createdAt the creation timestamp
     * @param updatedAt the update timestamp
     */
    public Prescription(String id, String patientName, Long appointmentId, String medication, 
                       String dosage, String doctorNotes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.medication = medication;
        this.dosage = dosage;
        this.doctorNotes = doctorNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // ===== Getters and Setters =====

    /**
     * Get the prescription's MongoDB document id
     * 
     * @return the document id (MongoDB ObjectId as String)
     */
    public String getId() {
        return id;
    }

    /**
     * Set the prescription's MongoDB document id
     * 
     * @param id the document id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the patient's name
     * 
     * @return the patient's full name
     */
    public String getPatientName() {
        return patientName;
    }

    /**
     * Set the patient's name
     * 
     * @param patientName the patient's full name to set
     */
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    /**
     * Get the associated appointment ID
     * 
     * @return the appointment id that triggered this prescription
     */
    public Long getAppointmentId() {
        return appointmentId;
    }

    /**
     * Set the associated appointment ID
     * 
     * @param appointmentId the appointment id to set
     */
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    /**
     * Get the name of the prescribed medication
     * 
     * @return the medication name
     */
    public String getMedication() {
        return medication;
    }

    /**
     * Set the name of the prescribed medication
     * 
     * @param medication the medication name to set
     */
    public void setMedication(String medication) {
        this.medication = medication;
    }

    /**
     * Get the dosage information
     * 
     * @return the dosage details
     */
    public String getDosage() {
        return dosage;
    }

    /**
     * Set the dosage information
     * 
     * @param dosage the dosage details to set
     */
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    /**
     * Get the doctor's notes for this prescription
     * 
     * @return the doctor's notes (may be null if not provided)
     */
    public String getDoctorNotes() {
        return doctorNotes;
    }

    /**
     * Set the doctor's notes for this prescription
     * 
     * @param doctorNotes the doctor's notes to set
     */
    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    /**
     * Get the creation timestamp
     * 
     * @return the timestamp when the prescription was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the creation timestamp
     * 
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the last update timestamp
     * 
     * @return the timestamp when the prescription was last updated
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set the last update timestamp
     * 
     * @param updatedAt the update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    // ===== Object Methods =====

    /**
     * String representation of the Prescription
     * 
     * @return string representation of Prescription document
     */
    @Override
    public String toString() {
        return "Prescription{" +
                "id='" + id + '\'' +
                ", patientName='" + patientName + '\'' +
                ", appointmentId=" + appointmentId +
                ", medication='" + medication + '\'' +
                ", dosage='" + dosage + '\'' +
                ", doctorNotes='" + doctorNotes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

// 8. Getters and Setters:
//    - Standard getter and setter methods are provided for all fields: id, patientName, medication, dosage, doctorNotes, and appointmentId.
//    - These methods allow access and modification of the fields of the Prescription class.


}
