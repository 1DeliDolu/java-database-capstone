package com.project.back_end.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Patient Model Class
 * 
 * Represents a patient in the Smart Clinic Management System.
 * Captures personal details including contact information and address.
 * Patients book appointments and receive treatment through this system.
 * 
 * JPA Entity mapped to the 'patient' table in MySQL database.
 */
@Entity
public class Patient {

    // ===== Fields =====

    /**
     * Unique identifier for the Patient entity
     * 
     * Auto-generated primary key using identity strategy (auto-increment).
     * Each patient is uniquely identified by this id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Patient's full name
     * 
     * Required field with length constraints.
     * Must be between 3 and 100 characters for meaningful validation.
     */
    @NotNull(message = "Patient name cannot be null")
    @Size(min = 3, max = 100, message = "Patient name must be between 3 and 100 characters")
    private String name;

    /**
     * Patient's email address
     * 
     * Required field with email format validation.
     * Used for communication, password recovery, and login authentication.
     * Must match standard email format.
     * Should be unique per patient to prevent duplicate accounts.
     */
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Patient's password for authentication
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
     * Patient's phone number
     * 
     * Required field with phone format validation.
     * Must be exactly 10 digits (no special characters or spaces).
     * Used for appointment notifications and emergency contact.
     */
    @NotNull(message = "Phone number cannot be null")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    /**
     * Patient's address
     * 
     * Required field with maximum length constraint.
     * Cannot exceed 255 characters.
     * Used for mailing, medical records, and emergency contact information.
     */
    @NotNull(message = "Address cannot be null")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;


    // ===== Constructors =====

    /**
     * No-argument constructor
     * 
     * Required by JPA for entity instantiation during database operations.
     */
    public Patient() {
    }

    /**
     * Parameterized constructor with core information
     * 
     * Creates a new Patient with essential information.
     * 
     * @param name the patient's full name
     * @param email the patient's email address
     * @param password the patient's password
     * @param phone the patient's phone number
     * @param address the patient's home address
     */
    public Patient(String name, String email, String password, String phone, String address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
    }

    /**
     * Full parameterized constructor
     * 
     * Creates a new Patient with all fields including id.
     * 
     * @param id the patient id
     * @param name the patient's full name
     * @param email the patient's email address
     * @param password the patient's password
     * @param phone the patient's phone number
     * @param address the patient's home address
     */
    public Patient(Long id, String name, String email, String password, String phone, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
    }


    // ===== Getters and Setters =====

    /**
     * Get the patient's unique identifier
     * 
     * @return the patient id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the patient's unique identifier
     * 
     * @param id the patient id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the patient's full name
     * 
     * @return the patient's name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the patient's full name
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the patient's email address
     * 
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the patient's email address
     * 
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the patient's password
     * 
     * Note: This field is marked as WRITE_ONLY in JSON serialization,
     * so it will not be exposed in API responses.
     * 
     * @return the patient's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the patient's password
     * 
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the patient's phone number
     * 
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Set the patient's phone number
     * 
     * @param phone the phone number to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Get the patient's address
     * 
     * @return the patient's home address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the patient's address
     * 
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }


    // ===== Object Methods =====

    /**
     * String representation of the Patient
     * 
     * Note: Password is intentionally excluded for security.
     * 
     * @return string representation of Patient
     */
    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
