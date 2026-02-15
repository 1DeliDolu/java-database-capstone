package com.project.back_end.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/appointments")
@Validated
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    /**
     * Retrieves appointments for a specific date and patient name.
     * Only accessible to doctors with a valid token.
     *
     * @param date        The appointment date (format: yyyy-MM-dd)
     * @param patientName The name of the patient
     * @param token       The authentication token for the doctor
     * @return ResponseEntity with appointments or error message
     */
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Validate token for doctor role
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        // Parse the date string to LocalDate
        try {
            LocalDate appointmentDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Map<String, Object> result = appointmentService.getAppointment(patientName, appointmentDate, token);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Invalid date format. Use yyyy-MM-dd");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Books a new appointment for a patient.
     * Only accessible to patients with a valid token.
     *
     * @param appointment The appointment details
     * @param token       The authentication token for the patient
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @Valid @RequestBody Appointment appointment,
            @PathVariable String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Validate token for patient role
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        // Validate appointment data
        int appointmentValidation = service.validateAppointment(appointment);
        if (appointmentValidation == -1) {
            response.put("message", "Doctor not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else if (appointmentValidation == 0) {
            response.put("message", "Appointment time is not available");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
        // Book the appointment
        try {
            int result = appointmentService.bookAppointment(appointment);
            if (result == 1) {
                response.put("message", "Appointment booked successfully");
                response.put("appointment", appointment);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else {
                response.put("message", "Error booking appointment");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.put("message", "Error booking appointment: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing appointment.
     * Only accessible to patients with a valid token.
     *
     * @param appointment The updated appointment details
     * @param token       The authentication token for the patient
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/{token}")
    public ResponseEntity<?> updateAppointment(
            @Valid @RequestBody Appointment appointment,
            @PathVariable String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Validate token for patient role
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        // Update the appointment
        return appointmentService.updateAppointment(appointment);
    }

    /**
     * Cancels an existing appointment.
     * Only accessible to patients with a valid token.
     *
     * @param id    The appointment ID
     * @param token The authentication token for the patient
     * @return ResponseEntity with success or error message
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            @PathVariable String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Validate token for patient role
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        // Cancel the appointment
        return appointmentService.cancelAppointment(id, token);
    }

}
