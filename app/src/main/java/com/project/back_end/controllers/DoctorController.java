package com.project.back_end.controllers;

import com.project.back_end.models.Doctor;
import com.project.back_end.DTO.Login;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    @Autowired
    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    /**
     * Retrieves the availability of a specific doctor on a given date.
     *
     * @param user     The user role (doctor, patient, admin)
     * @param doctorId The ID of the doctor
     * @param date     The date for which to fetch availability (format: yyyy-MM-dd)
     * @param token    The authentication token
     * @return ResponseEntity with available time slots or error message
     */
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Validate token
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, user);
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        // Parse the date
        try {
            LocalDate appointmentDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> availability = doctorService.getDoctorAvailability(doctorId, appointmentDate);
            response.put("message", "Doctor availability retrieved successfully");
            response.put("availability", availability);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Invalid date format or error retrieving availability");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves a list of all doctors.
     *
     * @return ResponseEntity with list of all doctors
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Doctor> doctors = doctorService.getDoctors();
            response.put("doctors", doctors);
            response.put("message", "Doctors retrieved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Error retrieving doctors");
            response.put("doctors", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds a new doctor to the system.
     * Only accessible to admins with a valid token.
     *
     * @param doctor The doctor details to add
     * @param token  The authentication token for admin
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> addDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {
        
        Map<String, String> response = new HashMap<>();
        
        // Validate admin token
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "admin");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        try {
            int result = doctorService.saveDoctor(doctor);
            if (result == 1) {
                response.put("message", "Doctor added to db");
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else if (result == -1) {
                response.put("message", "Doctor already exists");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            } else {
                response.put("message", "Some internal error occurred");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.put("message", "Some internal error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handles doctor login.
     *
     * @param login The login credentials (email and password)
     * @return ResponseEntity with token if login is successful, error message otherwise
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    /**
     * Updates doctor information.
     * Only accessible to admins with a valid token.
     *
     * @param doctor The updated doctor details
     * @param token  The authentication token for admin
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {
        
        Map<String, String> response = new HashMap<>();
        
        // Validate admin token
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "admin");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        try {
            int result = doctorService.updateDoctor(doctor);
            if (result == 1) {
                response.put("message", "Doctor updated");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if (result == -1) {
                response.put("message", "Doctor not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            } else {
                response.put("message", "Some internal error occurred");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.put("message", "Some internal error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a doctor by ID.
     * Only accessible to admins with a valid token.
     *
     * @param id    The ID of the doctor to delete
     * @param token The authentication token for admin
     * @return ResponseEntity with success or error message
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable Long id,
            @PathVariable String token) {
        
        Map<String, String> response = new HashMap<>();
        
        // Validate admin token
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "admin");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        
        try {
            int result = doctorService.deleteDoctor(id);
            if (result == 1) {
                response.put("message", "Doctor deleted successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if (result == -1) {
                response.put("message", "Doctor not found with id");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            } else {
                response.put("message", "Some internal error occurred");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            response.put("message", "Some internal error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Filters doctors based on name, time, and specialty.
     *
     * @param name      The name of the doctor (can be partial)
     * @param time      The available time for filtering
     * @param speciality The specialty of the doctor
     * @return ResponseEntity with filtered doctors
     */
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {
        
        Map<String, Object> response = service.filterDoctor(name, time, speciality);
        
        if (response.get("doctors") != null) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
