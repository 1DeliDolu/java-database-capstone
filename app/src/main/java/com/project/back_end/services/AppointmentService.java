package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OrchestratorService service;
    private final TokenService tokenService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                            PatientRepository patientRepository,
                            DoctorRepository doctorRepository,
                            OrchestratorService service,
                            TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.service = service;
        this.tokenService = tokenService;
    }

    /**
     * Books a new appointment.
     * 
     * @param appointment The appointment object to be booked
     * @return 1 if successful, 0 if there's an error
     */
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Updates an existing appointment.
     * 
     * @param appointment The appointment object with updated details
     * @return ResponseEntity with a success or error message
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if appointment exists
            Optional<Appointment> existingAppointment = appointmentRepository.findById(appointment.getId());
            
            if (existingAppointment.isEmpty()) {
                response.put("message", "Appointment not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            // Validate the appointment
            int validationResult = service.validateAppointment(appointment);
            if (validationResult == 0) {
                response.put("message", "Appointment time is not available");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            if (validationResult == -1) {
                response.put("message", "Doctor not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            // Update the appointment
            appointmentRepository.save(appointment);
            response.put("message", "Appointment updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error updating appointment: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancels an existing appointment.
     * 
     * @param id The ID of the appointment to cancel
     * @param token The authorization token
     * @return ResponseEntity with a success or error message
     */
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Find the appointment
            Optional<Appointment> appointment = appointmentRepository.findById(id);
            
            if (appointment.isEmpty()) {
                response.put("message", "Appointment not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            // Extract patient ID from token
            String patientEmail = tokenService.extractEmail(token);
            if (patientEmail == null || patientEmail.isEmpty()) {
                response.put("message", "Invalid token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            Patient patient = patientRepository.findByEmail(patientEmail);

            if (patient == null) {
                response.put("message", "Patient not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            // Verify that the patient trying to cancel owns the appointment
            if (!appointment.get().getPatient().getId().equals(patient.getId())) {
                response.put("message", "Unauthorized: You can only cancel your own appointments");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
            
            // Delete the appointment
            appointmentRepository.delete(appointment.get());
            response.put("message", "Appointment cancelled successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error cancelling appointment: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves appointments for a doctor on a specific date, optionally filtered by patient name.
     * 
     * @param pname Patient name to filter by (optional)
     * @param date The date for which to retrieve appointments
     * @param token The authorization token
     * @return Map containing the list of appointments
     */
    @Transactional
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();
        List<Appointment> appointments = new ArrayList<>();
        
        try {
            // Extract doctor email from token
            String doctorEmail = tokenService.extractEmail(token);
            
            // Find the doctor
            var doctor = doctorRepository.findByEmail(doctorEmail);
            if (doctor == null) {
                response.put("message", "Doctor not found");
                response.put("appointments", appointments);
                return response;
            }
            
            // Set time range for the entire day
            LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            
            // Fetch appointments for the doctor on the specified date
            boolean applyPatientNameFilter = pname != null
                    && !pname.isBlank()
                    && !"null".equalsIgnoreCase(pname)
                    && !"all".equalsIgnoreCase(pname)
                    && !"undefined".equalsIgnoreCase(pname);

            if (applyPatientNameFilter) {
                // Filter by patient name if provided
                appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctor.getId(), pname, startOfDay, endOfDay);
            } else {
                // Get all appointments for the doctor on the date
                appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    doctor.getId(), startOfDay, endOfDay);
            }
            
            response.put("message", "Appointments retrieved successfully");
            response.put("appointments", appointments);
            return response;
            
        } catch (Exception e) {
            response.put("message", "Error retrieving appointments: " + e.getMessage());
            response.put("appointments", appointments);
            return response;
        }
    }

    /**
     * Retrieves all appointments for a doctor, optionally filtered by patient name.
     *
     * @param pname Patient name to filter by (optional)
     * @param token The authorization token
     * @return Map containing the list of appointments
     */
    @Transactional
    public Map<String, Object> getAllAppointmentsForDoctor(String pname, String token) {
        Map<String, Object> response = new HashMap<>();
        List<Appointment> appointments = new ArrayList<>();

        try {
            String doctorEmail = tokenService.extractEmail(token);
            var doctor = doctorRepository.findByEmail(doctorEmail);

            if (doctor == null) {
                response.put("message", "Doctor not found");
                response.put("appointments", appointments);
                return response;
            }

            boolean applyPatientNameFilter = pname != null
                    && !pname.isBlank()
                    && !"null".equalsIgnoreCase(pname)
                    && !"all".equalsIgnoreCase(pname)
                    && !"undefined".equalsIgnoreCase(pname);

            if (applyPatientNameFilter) {
                appointments = appointmentRepository.findByDoctorIdAndPatientNameContainingIgnoreCaseOrderByAppointmentTimeAsc(
                        doctor.getId(), pname);
            } else {
                appointments = appointmentRepository.findByDoctorIdOrderByAppointmentTimeAsc(doctor.getId());
            }

            response.put("message", "All appointments retrieved successfully");
            response.put("appointments", appointments);
            return response;
        } catch (Exception e) {
            response.put("message", "Error retrieving appointments: " + e.getMessage());
            response.put("appointments", appointments);
            return response;
        }
    }
}
