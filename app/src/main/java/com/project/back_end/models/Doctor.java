package com.project.back_end.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Doctor Model Class
 * 
 * Represents a healthcare provider in the Smart Clinic Management System.
 * Stores information about the doctor including contact details, medical specialty,
 * and availability for scheduling appointments.
 * 
 * JPA Entity mapped to the 'doctor' table in MySQL database.
 */
@Entity
public class Doctor {

    // ===== Fields =====

    /**
     * Unique identifier for the Doctor entity
     * 
     * Auto-generated primary key using identity strategy (auto-increment).
     * Each doctor is uniquely identified by this id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Doctor's full name
     * 
     * Required field with length constraints.
     * Must be between 3 and 100 characters for meaningful validation.
     */
    @NotNull(message = "Doctor name cannot be null")
    @Size(min = 3, max = 100, message = "Doctor name must be between 3 and 100 characters")
    private String name;

    /**
     * Doctor's medical specialty
     * 
     * Required field with length constraints.
     * Examples: Cardiology, Neurology, Pediatrics, Dermatology, etc.
     * Must be between 3 and 50 characters.
     */
    @NotNull(message = "Specialty cannot be null")
    @Size(min = 3, max = 50, message = "Specialty must be between 3 and 50 characters")
    private String specialty;

    /**
     * Doctor's email address
     * 
     * Required field with email format validation.
     * Used for communication and login authentication.
     * Must match standard email format.
     */
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Doctor's password for authentication
     * 
     * Required field with minimum length constraint.
     * Hashed password for secure storage.
     * Marked as WRITE_ONLY: can be accepted in POST requests but will not be 
     * exposed in JSON API responses (GET requests).
     * Minimum 6 characters for reasonable complexity.
     */
    @NotNull(message = "Password cannot be null")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * Doctor's phone number
     * 
     * Required field with phone format validation.
     * Must be exactly 10 digits (no special characters or spaces).
     * Used for contact and appointment notifications.
     */
    @NotNull(message = "Phone number cannot be null")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    /**
     * List of doctor's available time slots
     * 
     * Stored as a collection of strings in a separate table via @ElementCollection.
     * Each time slot is represented as a string (e.g., "09:00-10:00", "10:00-11:00").
     * Allows for flexible scheduling and multiple time slots per day.
     * 
     * Example: ["09:00-10:00", "10:00-11:00", "14:00-15:00"]
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> availableTimes;


    // ===== Constructors =====

    /**
     * No-argument constructor
     * 
     * Required by JPA for entity instantiation during database operations.
     */
    public Doctor() {
    }

    /**
     * Parameterized constructor with core information
     * 
     * Creates a new Doctor with essential information.
     * 
     * @param name the doctor's full name
     * @param specialty the medical specialty
     * @param email the doctor's email address
     * @param password the doctor's password
     * @param phone the doctor's phone number
     */
    public Doctor(String name, String specialty, String email, String password, String phone) {
        this.name = name;
        this.specialty = specialty;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    /**
     * Full parameterized constructor
     * 
     * Creates a new Doctor with all fields including id and availableTimes.
     * 
     * @param id the doctor id
     * @param name the doctor's full name
     * @param specialty the medical specialty
     * @param email the doctor's email address
     * @param password the doctor's password
     * @param phone the doctor's phone number
     * @param availableTimes list of available time slots
     */
    public Doctor(Long id, String name, String specialty, String email, String password, String phone, List<String> availableTimes) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.availableTimes = availableTimes;
    }


    // ===== Getters and Setters =====

    /**
     * Get the doctor's unique identifier
     * 
     * @return the doctor id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the doctor's unique identifier
     * 
     * @param id the doctor id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the doctor's full name
     * 
     * @return the doctor's name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the doctor's full name
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the doctor's medical specialty
     * 
     * @return the medical specialty
     */
    public String getSpecialty() {
        return specialty;
    }

    /**
     * Set the doctor's medical specialty
     * 
     * @param specialty the specialty to set
     */
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    /**
     * Get the doctor's email address
     * 
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the doctor's email address
     * 
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the doctor's password
     * 
     * Note: This field is marked as WRITE_ONLY in JSON serialization,
     * so it will not be exposed in API responses.
     * 
     * @return the doctor's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the doctor's password
     * 
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the doctor's phone number
     * 
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Set the doctor's phone number
     * 
     * @param phone the phone number to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Get the list of available time slots
     * 
     * Returns the doctor's available times for appointment scheduling.
     * Example: ["09:00-10:00", "10:00-11:00", "14:00-15:00"]
     * 
     * @return list of available time slots
     */
    public List<String> getAvailableTimes() {
        return availableTimes;
    }

    /**
     * Set the list of available time slots
     * 
     * @param availableTimes list of available time slots to set
     */
    public void setAvailableTimes(List<String> availableTimes) {
        this.availableTimes = availableTimes;
    }


    // ===== Object Methods =====

    /**
     * String representation of the Doctor
     * 
     * Note: Password is intentionally excluded for security.
     * 
     * @return string representation of Doctor
     */
    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", specialty='" + specialty + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", availableTimes=" + availableTimes +
                '}';
    }
//      - The @NotNull annotation ensures that a phone number must be provided.
//      - The @Pattern(regexp = "^[0-9]{10}$") annotation validates that the phone number must be exactly 10 digits long.

// 7. 'availableTimes' field:
//    - Type: private List<String>
//    - Description:
//      - Represents the available times for the doctor in a list of time slots.
//      - Each time slot is represented as a string (e.g., "09:00-10:00", "10:00-11:00").
//      - The @ElementCollection annotation ensures that the list of time slots is stored as a separate collection in the database.

// 8. Getters and Setters:
//    - Standard getter and setter methods are provided for all fields: id, name, specialty, email, password, phone, and availableTimes.

}
