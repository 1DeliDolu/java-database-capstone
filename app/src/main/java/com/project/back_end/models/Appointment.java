package com.project.back_end.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Appointment Model Class
 * 
 * Represents a scheduled meeting between a doctor and a patient in the Smart Clinic 
 * Management System. It includes metadata such as the date, time, and status of the 
 * appointment, and provides helper methods to extract specific time-based information.
 * 
 * JPA Entity mapped to the 'appointment' table in MySQL database.
 * Establishes ManyToOne relationships with both Doctor and Patient entities.
 */
@Entity
public class Appointment {

    // ===== Fields =====

    /**
     * Unique identifier for the Appointment entity
     * 
     * Auto-generated primary key using identity strategy (auto-increment).
     * Each appointment is uniquely identified by this id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Doctor assigned to this appointment
     * 
     * Many-to-one relationship: multiple appointments can be scheduled with one doctor.
     * Required field - every appointment must have an assigned doctor.
     */
    @ManyToOne
    @NotNull(message = "Doctor cannot be null")
    private Doctor doctor;

    /**
     * Patient assigned to this appointment
     * 
     * Many-to-one relationship: multiple appointments can be scheduled for one patient.
     * Required field - every appointment must be associated with a patient.
     */
    @ManyToOne
    @NotNull(message = "Patient cannot be null")
    private Patient patient;

    /**
     * Date and time when the appointment is scheduled to occur
     * 
     * Must be in the future - appointments cannot be scheduled in the past.
     * Uses LocalDateTime to store both date and time information.
     */
    @Future(message = "Appointment time must be in the future")
    @NotNull(message = "Appointment time cannot be null")
    private LocalDateTime appointmentTime;

    /**
     * Status of the appointment
     * 
     * Status codes:
     * - 0: Scheduled (appointment is upcoming)
     * - 1: Completed (appointment has been completed)
     * - 2: Cancelled (appointment has been cancelled)
     * 
     * Default value is 0 (Scheduled) when appointment is created.
     */
    @NotNull(message = "Status cannot be null")
    private int status;


    // ===== Constructors =====

    /**
     * No-argument constructor
     * 
     * Required by JPA for entity instantiation during database operations.
     */
    public Appointment() {
        this.status = 0; // Default: Scheduled
    }

    /**
     * Parameterized constructor
     * 
     * Creates a new Appointment with core information.
     * 
     * @param doctor the assigned doctor
     * @param patient the assigned patient
     * @param appointmentTime the scheduled date and time
     */
    public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = 0; // Default: Scheduled
    }

    /**
     * Full parameterized constructor
     * 
     * Creates a new Appointment with all fields including id and status.
     * 
     * @param id the appointment id
     * @param doctor the assigned doctor
     * @param patient the assigned patient
     * @param appointmentTime the scheduled date and time
     * @param status the appointment status (0=scheduled, 1=completed, 2=cancelled)
     */
    public Appointment(Long id, Doctor doctor, Patient patient, LocalDateTime appointmentTime, int status) {
        this.id = id;
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }


    // ===== Helper Methods (Transient - Not Persisted) =====

    /**
     * Calculates and returns the end time of the appointment
     * 
     * Assumes appointments are 1 hour long by default.
     * This method is transient (not persisted to database).
     * Useful for display and scheduling logic.
     * 
     * @return the appointment end time (appointmentTime + 1 hour)
     */
    @Transient
    public LocalDateTime getEndTime() {
        return appointmentTime.plusHours(1);
    }

    /**
     * Extracts and returns only the date portion of the appointment
     * 
     * This method is transient (not persisted to database).
     * Useful for displaying appointment date without time information.
     * 
     * @return the appointment date (LocalDate)
     */
    @Transient
    public LocalDate getAppointmentDate() {
        return appointmentTime.toLocalDate();
    }

    /**
     * Extracts and returns only the time portion of the appointment
     * 
     * This method is transient (not persisted to database).
     * Useful for displaying appointment time without date information.
     * 
     * @return the appointment time (LocalTime)
     */
    @Transient
    public LocalTime getAppointmentTimeOnly() {
        return appointmentTime.toLocalTime();
    }

    /**
     * Converts status code to human-readable string
     * 
     * Helper method for UI display and logging.
     * 
     * @return status description (e.g., "Scheduled", "Completed", "Cancelled")
     */
    @Transient
    public String getStatusName() {
        switch (this.status) {
            case 0:
                return "Scheduled";
            case 1:
                return "Completed";
            case 2:
                return "Cancelled";
            default:
                return "Unknown";
        }
    }


    // ===== Getters and Setters =====

    /**
     * Get the appointment's unique identifier
     * 
     * @return the appointment id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the appointment's unique identifier
     * 
     * @param id the appointment id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the doctor assigned to this appointment
     * 
     * @return the Doctor entity
     */
    public Doctor getDoctor() {
        return doctor;
    }

    /**
     * Set the doctor assigned to this appointment
     * 
     * @param doctor the Doctor entity to set
     */
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    /**
     * Get the patient assigned to this appointment
     * 
     * @return the Patient entity
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * Set the patient assigned to this appointment
     * 
     * @param patient the Patient entity to set
     */
    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    /**
     * Get the appointment's scheduled date and time
     * 
     * @return the LocalDateTime of the appointment
     */
    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    /**
     * Set the appointment's scheduled date and time
     * 
     * @param appointmentTime the LocalDateTime to set
     */
    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    /**
     * Get the appointment's status
     * 
     * @return status code (0=scheduled, 1=completed, 2=cancelled)
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set the appointment's status
     * 
     * @param status the status code to set (0=scheduled, 1=completed, 2=cancelled)
     */
    public void setStatus(int status) {
        this.status = status;
    }


    // ===== Object Methods =====

    /**
     * String representation of the Appointment
     * 
     * @return string representation of Appointment
     */
    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", doctor=" + (doctor != null ? doctor.getName() : "null") +
                ", patient=" + (patient != null ? patient.getName() : "null") +
                ", appointmentTime=" + appointmentTime +
                ", status=" + getStatusName() +
                '}';
    }

// 7. 'getAppointmentDate' method:
//    - Type: private LocalDate
//    - Description:
//      - This method extracts only the date part from the appointmentTime field.
//      - It returns a LocalDate object representing just the date (without the time) of the scheduled appointment.

// 8. 'getAppointmentTimeOnly' method:
//    - Type: private LocalTime
//    - Description:
//      - This method extracts only the time part from the appointmentTime field.
//      - It returns a LocalTime object representing just the time (without the date) of the scheduled appointment.

// 9. Constructor(s):
//    - A no-argument constructor is implicitly provided by JPA for entity creation.
//    - A parameterized constructor can be added as needed to initialize fields.

// 10. Getters and Setters:
//    - Standard getter and setter methods are provided for accessing and modifying the fields: id, doctor, patient, appointmentTime, status, etc.

}

