package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    @Autowired
    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    /**
     * Retrieves patient details using the authentication token.
     *
     * @param token The authentication token for the patient
     * @return ResponseEntity with patient details or error message
     */
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        
        // Validate token for patient role
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        // Fetch patient details
        return patientService.getPatientDetails(token);
    }

    /**
     * Creates a new patient (patient registration).
     *
     * @param patient The patient details to register
     * @return ResponseEntity with success or error message
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody Patient patient) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate if patient already exists
            boolean isNewPatient = service.validatePatient(patient);
            
            if (!isNewPatient) {
                response.put("message", "Patient with email id or phone no already exist");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
            
            // Create the patient
            int result = patientService.createPatient(patient);
            
            if (result == 1) {
                response.put("message", "Signup successful");
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else {
                response.put("message", "Internal server error");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.put("message", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handles patient login.
     *
     * @param login The login credentials (email and password)
     * @return ResponseEntity with token if login is successful, error message otherwise
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    /**
     * Retrieves all appointments for a specific patient.
     *
     * @param id    The ID of the patient
     * @param token The authentication token for the patient
     * @return ResponseEntity with list of appointments or error message
     */
    @GetMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(
            @PathVariable Long id,
            @PathVariable String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Validate token for patient role
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        // Fetch patient appointments
        return patientService.getPatientAppointment(id, token);
    }

    /**
     * Filters patient appointments based on condition and/or doctor name.
     *
     * @param condition The condition to filter appointments (e.g., "upcoming", "past")
     * @param name      The doctor name or description to filter by
     * @param token     The authentication token for the patient
     * @return ResponseEntity with filtered appointments or error message
     */
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Validate token for patient role
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        // Filter patient appointments
        return service.filterPatient(condition, name, token);
    }
}


