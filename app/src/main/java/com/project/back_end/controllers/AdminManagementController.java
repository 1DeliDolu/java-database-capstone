package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.AdminAppointmentUpdateDTO;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.services.AdminManagementService;

@RestController
@RequestMapping("${api.path}" + "admin/manage")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    public AdminManagementController(AdminManagementService adminManagementService) {
        this.adminManagementService = adminManagementService;
    }

    @GetMapping("/overview/{token}")
    public ResponseEntity<Map<String, Object>> getOverview(@PathVariable("token") String token) {
        Map<String, Object> response = new HashMap<>();
        if (!adminManagementService.isValidAdminToken(token)) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        response.putAll(adminManagementService.getOverview());
        response.put("message", "Overview fetched successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/doctor/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @PathVariable("token") String token,
            @RequestBody Doctor doctor) {
        if (!adminManagementService.isValidAdminToken(token)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return adminManagementService.updateDoctor(doctor);
    }

    @DeleteMapping("/doctor/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable("id") Long id,
            @PathVariable("token") String token) {
        if (!adminManagementService.isValidAdminToken(token)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return adminManagementService.deleteDoctor(id);
    }

    @PutMapping("/patient/{token}")
    public ResponseEntity<Map<String, String>> updatePatient(
            @PathVariable("token") String token,
            @RequestBody Patient patient) {
        if (!adminManagementService.isValidAdminToken(token)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return adminManagementService.updatePatient(patient);
    }

    @DeleteMapping("/patient/{id}/{token}")
    public ResponseEntity<Map<String, String>> deletePatient(
            @PathVariable("id") Long id,
            @PathVariable("token") String token) {
        if (!adminManagementService.isValidAdminToken(token)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return adminManagementService.deletePatient(id);
    }

    @PutMapping("/appointment/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable("token") String token,
            @RequestBody AdminAppointmentUpdateDTO appointment) {
        if (!adminManagementService.isValidAdminToken(token)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return adminManagementService.updateAppointment(appointment);
    }

    @DeleteMapping("/appointment/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteAppointment(
            @PathVariable("id") Long id,
            @PathVariable("token") String token) {
        if (!adminManagementService.isValidAdminToken(token)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return adminManagementService.deleteAppointment(id);
    }
}
