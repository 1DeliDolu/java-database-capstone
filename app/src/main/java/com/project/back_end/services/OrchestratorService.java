package com.project.back_end.services;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class OrchestratorService {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public OrchestratorService(TokenService tokenService, AdminRepository adminRepository,
                   DoctorRepository doctorRepository, PatientRepository patientRepository,
                   DoctorService doctorService, PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /**
     * Validates the authenticity of a provided token for a specific user.
     *
     * @param token The JWT token to validate
     * @param user  The user identifier (email) associated with the token
     * @return ResponseEntity with error message if token is invalid or expired
     */
    @Transactional
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        Map<String, String> response = new HashMap<>();
        
        try {
            boolean isValid = tokenService.validateToken(token, user);
            
            if (!isValid) {
                response.put("message", "Invalid or expired token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            response.put("message", "Token is valid");
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error validating token: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Validates admin login credentials and generates a token if valid.
     *
     * @param receivedAdmin The admin credentials (username and password)
     * @return ResponseEntity with generated token if valid, error message otherwise
     */
    @Transactional
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Find admin by username
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            
            if (admin == null) {
                response.put("message", "Admin not found");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            // Verify password
            if (!admin.getPassword().equals(receivedAdmin.getPassword())) {
                response.put("message", "Invalid password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            // Generate and return token
            String token = tokenService.generateToken(admin.getUsername());
            response.put("token", token);
            response.put("message", "Admin authenticated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error validating admin: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Filters doctors based on name, specialty, and available time.
     *
     * @param name      The name of the doctor (optional)
    /**
     * Filters doctors based on name, specialty, and available time.
     *
     * @param name      The name of the doctor (optional)
     * @param specialty The specialty of the doctor (optional)
     * @param time      The available time slot (optional)
     * @return Map containing filtered doctors list or all doctors if no filters provided
     */
    @Transactional
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> doctors;
            
            // Determine which filter combination to apply
            if (name != null && !name.isEmpty() && specialty != null && !specialty.isEmpty() && time != null && !time.isEmpty()) {
                doctors = doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, time);
            } else if (name != null && !name.isEmpty() && specialty != null && !specialty.isEmpty()) {
                doctors = doctorService.filterDoctorByNameAndSpecility(name, specialty);
            } else if (name != null && !name.isEmpty() && time != null && !time.isEmpty()) {
                doctors = doctorService.filterDoctorByNameAndTime(name, time);
            } else if (specialty != null && !specialty.isEmpty() && time != null && !time.isEmpty()) {
                doctors = doctorService.filterDoctorByTimeAndSpecility(specialty, time);
            } else if (name != null && !name.isEmpty()) {
                doctors = doctorService.findDoctorByName(name);
            } else if (specialty != null && !specialty.isEmpty()) {
                doctors = doctorService.filterDoctorBySpecility(specialty);
            } else if (time != null && !time.isEmpty()) {
                return doctorService.filterDoctorsByTime(time);
            } else {
                Map<String, Object> allDoctors = new HashMap<>();
                allDoctors.put("doctors", doctorService.getDoctors());
                return allDoctors;
            }
            
            return doctors;
        } catch (Exception e) {
            response.put("message", "Error filtering doctors: " + e.getMessage());
            response.put("doctors", null);
            return response;
        }
    }

    /**
     * Validates if an appointment time is available for a specific doctor.
     *
     * @param appointment The appointment to validate
     * @return 1 if valid, 0 if time unavailable, -1 if doctor doesn't exist
     */
    @Transactional
    public int validateAppointment(Appointment appointment) {
        try {
            // Check if doctor exists
            Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctor().getId());
            
            if (!doctorOpt.isPresent()) {
                return -1;
            }
            
            // Get available time slots for the doctor
            List<String> availableSlots = doctorService.getDoctorAvailability(
                    appointment.getDoctor().getId(),
                    appointment.getAppointmentTime().toLocalDate()
            );
            
            // Format the requested appointment time for comparison
            String requestedTime = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            
            // Check if requested time matches any available slot
            for (String slot : availableSlots) {
                if (isRequestedTimeInSlot(requestedTime, slot)) {
                    return 1; // Valid appointment time
                }
            }
            
            return 0; // Time not available
            
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isRequestedTimeInSlot(String requestedTime, String slot) {
        if (requestedTime == null || requestedTime.isBlank() || slot == null || slot.isBlank()) {
            return false;
        }

        String[] commaSplit = slot.split(",");
        for (String piece : commaSplit) {
            String clean = piece == null ? "" : piece.trim();
            if (clean.isEmpty()) continue;

            String start = clean.contains("-") ? clean.split("-")[0].trim() : clean;
            String normalizedStart = normalizeTime(start);
            if (requestedTime.equals(normalizedStart)) {
                return true;
            }
        }

        return false;
    }

    private String normalizeTime(String value) {
        if (value == null) return "";
        String cleaned = value.trim();
        if (cleaned.isEmpty()) return "";

        try {
            return java.time.LocalTime.parse(cleaned, DateTimeFormatter.ofPattern("H:mm"))
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException ignored) {
            try {
                return java.time.LocalTime.parse(cleaned, DateTimeFormatter.ofPattern("HH:mm"))
                        .format(DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ignoredAgain) {
                return cleaned;
            }
        }
    }

    /**
     * Validates if a patient is new (doesn't exist in the system).
     *
     * @param patient The patient to validate
     * @return true if patient does not exist, false if patient exists
     */
    @Transactional
    public boolean validatePatient(Patient patient) {
        try {
            Patient foundPatient = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
            return foundPatient == null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates patient login credentials and generates a token if valid.
     *
     * @param login The patient login credentials (email and password)
     * @return ResponseEntity with generated token if valid, error message otherwise
     */
    @Transactional
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Find patient by email
            Patient patient = patientRepository.findByEmail(login.getIdentifier());
            
            if (patient == null) {
                response.put("message", "Patient not found");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            // Verify password
            if (!patient.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            // Generate and return token
            String token = tokenService.generateToken(patient.getEmail());
            response.put("token", token);
            response.put("message", "Patient authenticated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error validating patient login: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Filters patient appointments based on condition and/or doctor name.
     *
     * @param condition The medical condition to filter by (optional)
     * @param name      The doctor name to filter by (optional)
     * @param token     The authentication token to identify the patient
     * @return ResponseEntity with filtered appointments or error message
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract patient email from token
            String patientEmail = tokenService.extractEmail(token);
            
            if (patientEmail == null || patientEmail.isEmpty()) {
                response.put("message", "Invalid token");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            // Find patient by email
            Patient patient = patientRepository.findByEmail(patientEmail);
            
            if (patient == null) {
                response.put("message", "Patient not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            // Apply filters based on provided parameters
            ResponseEntity<Map<String, Object>> filteredResult;
            
            if (condition != null && !condition.isEmpty() && name != null && !name.isEmpty()) {
                filteredResult = patientService.filterByDoctorAndCondition(condition, name, patient.getId());
            } else if (condition != null && !condition.isEmpty()) {
                filteredResult = patientService.filterByCondition(condition, patient.getId());
            } else if (name != null && !name.isEmpty()) {
                filteredResult = patientService.filterByDoctor(name, patient.getId());
            } else {
                filteredResult = patientService.getPatientAppointment(patient.getId(), token);
            }
            
            return filteredResult;
            
        } catch (Exception e) {
            response.put("message", "Error filtering patient appointments: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
