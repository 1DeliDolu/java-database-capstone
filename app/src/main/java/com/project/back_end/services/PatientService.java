package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Saves a new patient to the database.
     * 
     * @param patient The patient object to be saved
     * @return 1 on success, 0 on failure
     */
    @Transactional
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Retrieves a list of appointments for a specific patient.
     * 
     * @param id The patient's ID
     * @param token The JWT token containing the email
     * @return ResponseEntity with a list of appointments or an error message
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();
        List<AppointmentDTO> appointmentDTOs = new ArrayList<>();
        
        try {
            // Extract email from token
            String email = tokenService.extractEmail(token);
            
            // Find patient by email to verify ownership
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null || !patient.getId().equals(id)) {
                response.put("message", "Unauthorized: Patient ID mismatch");
                response.put("appointments", appointmentDTOs);
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            // Fetch appointments for the patient
            List<Appointment> appointments = appointmentRepository.findByPatientId(id);
            
            // Convert to DTOs
            for (Appointment appointment : appointments) {
                AppointmentDTO dto = new AppointmentDTO(
                    appointment.getId(),
                    appointment.getDoctor().getId(),
                    appointment.getDoctor().getName(),
                    appointment.getPatient().getId(),
                    appointment.getPatient().getName(),
                    appointment.getPatient().getEmail(),
                    appointment.getPatient().getPhone(),
                    appointment.getPatient().getAddress(),
                    appointment.getAppointmentTime(),
                    appointment.getStatus()
                );
                appointmentDTOs.add(dto);
            }
            
            response.put("message", "Appointments retrieved successfully");
            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error retrieving appointments: " + e.getMessage());
            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Filters appointments by condition (past or future) for a specific patient.
     * 
     * @param condition The condition to filter by (past or future)
     * @param id The patient's ID
     * @return ResponseEntity with filtered appointments or an error message
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();
        List<AppointmentDTO> appointmentDTOs = new ArrayList<>();
        
        try {
            int status;
            if ("past".equalsIgnoreCase(condition)) {
                status = 1; // Past appointments
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0; // Future appointments
            } else {
                response.put("message", "Invalid condition. Use 'past' or 'future'");
                response.put("appointments", appointmentDTOs);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // Fetch appointments by status
            List<Appointment> appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);
            
            // Convert to DTOs
            for (Appointment appointment : appointments) {
                AppointmentDTO dto = new AppointmentDTO(
                    appointment.getId(),
                    appointment.getDoctor().getId(),
                    appointment.getDoctor().getName(),
                    appointment.getPatient().getId(),
                    appointment.getPatient().getName(),
                    appointment.getPatient().getEmail(),
                    appointment.getPatient().getPhone(),
                    appointment.getPatient().getAddress(),
                    appointment.getAppointmentTime(),
                    appointment.getStatus()
                );
                appointmentDTOs.add(dto);
            }
            
            response.put("message", "Appointments filtered successfully");
            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error filtering appointments: " + e.getMessage());
            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Filters the patient's appointments by doctor's name.
     * 
     * @param name The name of the doctor
     * @param patientId The ID of the patient
     * @return ResponseEntity with filtered appointments or an error message
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        List<AppointmentDTO> appointmentDTOs = new ArrayList<>();
        
        try {
            // Fetch appointments filtered by doctor name and patient ID
            List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);
            
            // Convert to DTOs
            for (Appointment appointment : appointments) {
                AppointmentDTO dto = new AppointmentDTO(
                    appointment.getId(),
                    appointment.getDoctor().getId(),
                    appointment.getDoctor().getName(),
                    appointment.getPatient().getId(),
                    appointment.getPatient().getName(),
                    appointment.getPatient().getEmail(),
                    appointment.getPatient().getPhone(),
                    appointment.getPatient().getAddress(),
                    appointment.getAppointmentTime(),
                    appointment.getStatus()
                );
                appointmentDTOs.add(dto);
            }
            
            response.put("message", "Appointments filtered successfully");
            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error filtering appointments: " + e.getMessage());
            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Filters appointments by doctor's name and condition (past or future).
     * 
     * @param condition The condition to filter by (past or future)
     * @param name The name of the doctor
     * @param patientId The ID of the patient
     * @return ResponseEntity with filtered appointments or an error message
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> response = new HashMap<>();
        List<AppointmentDTO> appointmentDTOs = new ArrayList<>();
        
        try {
            int status;
            if ("past".equalsIgnoreCase(condition)) {
                status = 1; // Past appointments
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0; // Future appointments
            } else {
                response.put("message", "Invalid condition. Use 'past' or 'future'");
                response.put("appointments", appointmentDTOs);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            // Fetch appointments filtered by doctor name, patient ID, and status
            List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, patientId, status);
            
            // Convert to DTOs
            for (Appointment appointment : appointments) {
                AppointmentDTO dto = new AppointmentDTO(
                    appointment.getId(),
                    appointment.getDoctor().getId(),
                    appointment.getDoctor().getName(),
                    appointment.getPatient().getId(),
                    appointment.getPatient().getName(),
                    appointment.getPatient().getEmail(),
                    appointment.getPatient().getPhone(),
                    appointment.getPatient().getAddress(),
                    appointment.getAppointmentTime(),
                    appointment.getStatus()
                );
                appointmentDTOs.add(dto);
            }
            
            response.put("message", "Appointments filtered successfully");
            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error filtering appointments: " + e.getMessage());
            response.put("appointments", appointmentDTOs);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Fetches the patient's details based on the provided JWT token.
     * 
     * @param token The JWT token containing the email
     * @return ResponseEntity with patient details or an error message
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract email from token
            String email = tokenService.extractEmail(token);
            
            // Find patient by email
            Patient patient = patientRepository.findByEmail(email);
            
            if (patient == null) {
                response.put("message", "Patient not found");
                response.put("patient", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            response.put("message", "Patient details retrieved successfully");
            response.put("patient", patient);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error retrieving patient details: " + e.getMessage());
            response.put("patient", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
