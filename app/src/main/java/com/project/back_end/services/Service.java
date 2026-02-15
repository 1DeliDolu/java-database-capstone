package com.project.back_end.services;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@org.springframework.stereotype.Service
public class Service {
    private static final Pattern TIME_TOKEN_PATTERN =
            Pattern.compile("(\\d{1,2}:\\d{2}(?::\\d{2})?\\s*(?i:AM|PM)?)");

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService, AdminRepository adminRepository,
                   DoctorRepository doctorRepository, AppointmentRepository appointmentRepository, PatientRepository patientRepository,
                   DoctorService doctorService, PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

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

    @Transactional
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();

        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            if (admin == null) {
                response.put("message", "Admin not found");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            if (!admin.getPassword().equals(receivedAdmin.getPassword())) {
                response.put("message", "Invalid password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            String token = tokenService.generateToken(admin.getUsername());
            response.put("token", token);
            response.put("message", "Admin authenticated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Error validating admin: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> doctors;

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

    @Transactional
    public int validateAppointment(Appointment appointment) {
        try {
            Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctor().getId());
            if (doctorOpt.isEmpty()) {
                return -1;
            }
            Doctor doctor = doctorOpt.get();

            String requestedTime = appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            List<String> availableSlots = doctorService.getDoctorAvailability(
                    appointment.getDoctor().getId(),
                    appointment.getAppointmentTime().toLocalDate()
            );

            for (String slot : availableSlots) {
                if (isRequestedTimeInSlot(requestedTime, slot)) {
                    return 1;
                }
            }

            // Fallback: validate against doctor's configured slots (for legacy/dirty slot text),
            // then enforce hard collision check to avoid double-booking.
            if (!isRequestedTimeConfiguredForDoctor(requestedTime, doctor)) {
                return 0;
            }

            Long doctorId = appointment.getDoctor().getId();
            Long appointmentId = appointment.getId();
            if (appointmentId != null) {
                boolean conflict = appointmentRepository.existsByDoctor_IdAndAppointmentTimeAndIdNot(
                        doctorId, appointment.getAppointmentTime(), appointmentId);
                return conflict ? 0 : 1;
            }

            boolean conflict = appointmentRepository.existsByDoctor_IdAndAppointmentTime(
                    doctorId, appointment.getAppointmentTime());
            return conflict ? 0 : 1;

        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isRequestedTimeConfiguredForDoctor(String requestedTime, Doctor doctor) {
        if (requestedTime == null || requestedTime.isBlank() || doctor == null || doctor.getAvailableTimes() == null) {
            return false;
        }

        String normalizedRequested = normalizeTime(requestedTime);
        if (normalizedRequested.isBlank()) {
            return false;
        }

        for (String rawSlot : doctor.getAvailableTimes()) {
            if (isRequestedTimeInSlot(normalizedRequested, rawSlot)) {
                return true;
            }
        }

        return false;
    }

    private boolean isRequestedTimeInSlot(String requestedTime, String slot) {
        if (requestedTime == null || requestedTime.isBlank() || slot == null || slot.isBlank()) {
            return false;
        }

        String normalizedRequested = normalizeTime(requestedTime);
        if (normalizedRequested.isBlank()) {
            return false;
        }

        String[] commaSplit = slot.split("[,;]");
        for (String piece : commaSplit) {
            String clean = piece == null ? "" : piece.trim();
            if (clean.isEmpty()) continue;

            String[] parts = clean.split("[-–]");
            String start = parts.length > 0 ? parts[0].trim() : clean;
            String normalizedStart = normalizeTime(extractFirstTimeToken(start));
            if (requestedTime.equals(normalizedStart)) {
                return true;
            }
            if (normalizedRequested.equals(normalizedStart)) {
                return true;
            }
        }

        return false;
    }

    private String normalizeTime(String value) {
        if (value == null) return "";
        String cleaned = sanitizeTimeToken(value);
        if (cleaned.isEmpty()) return "";

        try {
            return java.time.LocalTime.parse(cleaned, DateTimeFormatter.ofPattern("H:mm"))
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException ignored) {
            try {
                return java.time.LocalTime.parse(cleaned, DateTimeFormatter.ofPattern("HH:mm"))
                        .format(DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ignoredAgain) {
                try {
                    return java.time.LocalTime.parse(cleaned, DateTimeFormatter.ofPattern("H:mm:ss"))
                            .format(DateTimeFormatter.ofPattern("HH:mm"));
                } catch (DateTimeParseException ignoredThird) {
                    try {
                        return java.time.LocalTime.parse(cleaned, DateTimeFormatter.ofPattern("HH:mm:ss"))
                                .format(DateTimeFormatter.ofPattern("HH:mm"));
                    } catch (DateTimeParseException ignoredFourth) {
                        try {
                            return java.time.LocalTime.parse(cleaned.toUpperCase(Locale.ENGLISH),
                                            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
                                    .format(DateTimeFormatter.ofPattern("HH:mm"));
                        } catch (DateTimeParseException ignoredFifth) {
                            try {
                                return java.time.LocalTime.parse(cleaned.toUpperCase(Locale.ENGLISH),
                                                DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
                                        .format(DateTimeFormatter.ofPattern("HH:mm"));
                            } catch (DateTimeParseException ignoredSixth) {
                                return cleaned;
                            }
                        }
                    }
                }
            }
        }
    }

    private String sanitizeTimeToken(String value) {
        if (value == null) return "";
        return value
                .replace('\u2013', '-')  // en-dash
                .replace('\u2014', '-')  // em-dash
                .replace('\u00A0', ' ')  // non-breaking space
                .replace('\u2007', ' ')
                .replace('\u202F', ' ')
                .replace("\"", "")
                .replace("'", "")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String extractFirstTimeToken(String value) {
        if (value == null) return "";
        String cleaned = sanitizeTimeToken(value);
        Matcher matcher = TIME_TOKEN_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return cleaned;
    }

    @Transactional
    public boolean validatePatient(Patient patient) {
        try {
            Patient foundPatient = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
            return foundPatient == null;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();

        try {
            Patient patient = patientRepository.findByEmail(login.getIdentifier());
            if (patient == null) {
                response.put("message", "Patient not found");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            if (!patient.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            String token = tokenService.generateToken(patient.getEmail());
            response.put("token", token);
            response.put("message", "Patient authenticated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", "Error validating patient login: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Map<String, Object> response = new HashMap<>();

        try {
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

            if (condition != null && !condition.isEmpty() && name != null && !name.isEmpty()) {
                return patientService.filterByDoctorAndCondition(condition, name, patient.getId());
            } else if (condition != null && !condition.isEmpty()) {
                return patientService.filterByCondition(condition, patient.getId());
            } else if (name != null && !name.isEmpty()) {
                return patientService.filterByDoctor(name, patient.getId());
            } else {
                return patientService.getPatientAppointment(patient.getId(), token);
            }
        } catch (Exception e) {
            response.put("message", "Error filtering patient appointments: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
