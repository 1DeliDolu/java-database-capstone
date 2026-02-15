package com.project.back_end.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

/**
 * Admin Model Class
 * 
 * Represents a system administrator in the Smart Clinic Management System.
 * Admins have access to manage the backend portal, including user access,
 * data review, and system maintenance.
 * 
 * JPA Entity mapped to the 'admin' table in MySQL database.
 */
@Entity
public class Admin {

    // ===== Fields =====

    /**
     * Unique identifier for the Admin entity
     * 
     * Auto-generated primary key using identity strategy (auto-increment).
     * Each admin is uniquely identified by this id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Admin username for authentication
     * 
     * Used to log into the system.
     * Cannot be null - validation ensures this field is always provided.
     */
    @NotNull(message = "Username cannot be null")
    private String username;

    /**
     * Admin password for authentication
     * 
     * Hashed password for secure storage.
     * Marked as WRITE_ONLY: can be accepted in POST requests but will not be 
     * exposed in JSON API responses (GET requests).
     * Cannot be null - validation ensures this field is always provided.
     */
    @NotNull(message = "Password cannot be null")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;


    // ===== Constructors =====

    /**
     * No-argument constructor
     * 
     * Required by JPA for entity instantiation during database operations.
     */
    public Admin() {
    }

    /**
     * Parameterized constructor
     * 
     * Creates a new Admin with specified username and password.
     * 
     * @param username the admin's username
     * @param password the admin's password
     */
    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Full parameterized constructor
     * 
     * Creates a new Admin with all fields including id.
     * 
     * @param id the admin id
     * @param username the admin's username
     * @param password the admin's password
     */
    public Admin(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }


    // ===== Getters and Setters =====

    /**
     * Get the admin's unique identifier
     * 
     * @return the admin id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the admin's unique identifier
     * 
     * @param id the admin id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the admin's username
     * 
     * @return the admin username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the admin's username
     * 
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get the admin's password
     * 
     * Note: This field is marked as WRITE_ONLY in JSON serialization,
     * so it will not be exposed in API responses.
     * 
     * @return the admin password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the admin's password
     * 
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * String representation of the Admin
     * 
     * Note: Password is intentionally excluded for security.
     * 
     * @return string representation of Admin
     */
    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
