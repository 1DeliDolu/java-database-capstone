package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;

@Service
public class PrescriptionService {
    
    private final PrescriptionRepository prescriptionRepository;

    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Saves a new prescription to the database.
     * 
     * @param prescription The prescription object to be saved
     * @return ResponseEntity with a success or error message
     */
    @Transactional
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if a prescription already exists for this appointment
            List<Prescription> existingPrescriptions = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
            
            if (existingPrescriptions != null && !existingPrescriptions.isEmpty()) {
                response.put("message", "A prescription already exists for this appointment");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // Save the new prescription
            prescriptionRepository.save(prescription);
            response.put("message", "Prescription saved successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
            
        } catch (Exception e) {
            response.put("message", "Error saving prescription: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves the prescription associated with a specific appointment ID.
     * 
     * @param appointmentId The appointment ID whose associated prescription is to be retrieved
     * @return ResponseEntity with the prescription details or an error message
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Fetch prescriptions for the given appointment ID
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            
            if (prescriptions == null || prescriptions.isEmpty()) {
                response.put("message", "No prescription found for this appointment");
                response.put("prescriptions", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            response.put("message", "Prescription retrieved successfully");
            response.put("prescriptions", prescriptions);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error retrieving prescription: " + e.getMessage());
            response.put("prescriptions", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
