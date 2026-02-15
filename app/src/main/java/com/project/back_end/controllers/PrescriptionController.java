package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "prescription")
public class PrescriptionController {
    
    private final PrescriptionService prescriptionService;
    private final Service service;

    public PrescriptionController(PrescriptionService prescriptionService, Service service) {
        this.prescriptionService = prescriptionService;
        this.service = service;
    }

    /**
     * Saves a new prescription for an appointment.
     * Only accessible to doctors with a valid token.
     *
     * @param prescription The prescription details to save
     * @param token        The authentication token for the doctor
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @RequestBody Prescription prescription,
            @PathVariable String token) {
        
        Map<String, String> response = new HashMap<>();
        
        // Validate token for doctor role
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        // Save the prescription
        return prescriptionService.savePrescription(prescription);
    }

    /**
     * Retrieves a prescription by appointment ID.
     * Only accessible to doctors with a valid token.
     *
     * @param appointmentId The ID of the appointment
     * @param token         The authentication token for the doctor
     * @return ResponseEntity with prescription details or error message
     */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Validate token for doctor role
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        // Retrieve the prescription
        return prescriptionService.getPrescription(appointmentId);
    }
}
