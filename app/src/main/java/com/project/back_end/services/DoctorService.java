package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;

@Service
public class DoctorService {
    private static final Pattern TIME_TOKEN_PATTERN =
            Pattern.compile("(\\d{1,2}:\\d{2}(?::\\d{2})?\\s*(?i:AM|PM)?)");

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                        AppointmentRepository appointmentRepository,
                        TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Fetches the available time slots for a specific doctor on a given date.
     * 
     * @param doctorId The ID of the doctor
     * @param date The date for which availability is needed
     * @return List of available time slots in HH:mm format
     */
    @Transactional
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        List<String> availableSlots = new ArrayList<>();
        
        try {
            Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
            if (doctorOpt.isEmpty()) {
                return availableSlots;
            }

            Doctor doctor = doctorOpt.get();

            // Fetch all appointments for the doctor on the specified date
            LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            
            var appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId, startOfDay, endOfDay);
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            
            // Base slots should come from doctor's configured availability
            List<String> baseSlots = normalizeSlots(doctor.getAvailableTimes());
            if (baseSlots == null || baseSlots.isEmpty()) {
                for (int hour = 8; hour < 17; hour++) {
                    LocalTime candidate = LocalTime.of(hour, 0);
                    if (date.isEqual(today) && !candidate.isAfter(now)) {
                        continue;
                    }
                    availableSlots.add(String.format("%02d:00", hour));
                }
                return availableSlots;
            }

            Set<String> bookedStartTimes = new HashSet<>();
            for (var appointment : appointments) {
                int hour = appointment.getAppointmentTime().getHour();
                int minute = appointment.getAppointmentTime().getMinute();
                bookedStartTimes.add(String.format("%02d:%02d", hour, minute));
            }

            for (String slot : baseSlots) {
                String slotStart = extractStartTime(slot);
                String normalizedSlotStart = normalizeTimeText(slotStart);
                LocalTime slotStartTime = parseLocalTimeFlexible(normalizedSlotStart);
                if (date.isEqual(today) && slotStartTime != null && !slotStartTime.isAfter(now)) {
                    continue;
                }
                if (!bookedStartTimes.contains(normalizedSlotStart)) {
                    availableSlots.add(slot);
                }
            }
        } catch (Exception e) {
            // Return empty list on error
        }
        
        return availableSlots;
    }

    private String extractStartTime(String slot) {
        if (slot == null || slot.isBlank()) {
            return "";
        }

        String normalized = sanitizeSlotText(slot);
        if (normalized.contains("-") || normalized.contains("–")) {
            return normalizeTimeText(extractFirstTimeToken(normalized.split("[-–]")[0].trim()));
        }
        return normalizeTimeText(extractFirstTimeToken(normalized));
    }

    private List<String> normalizeSlots(List<String> rawSlots) {
        List<String> normalized = new ArrayList<>();
        if (rawSlots == null) {
            return normalized;
        }

        for (String raw : rawSlots) {
            if (raw == null || raw.isBlank()) {
                continue;
            }

            // Supports both ["09:00-10:00", "10:00-11:00"] and ["09:00-10:00, 10:00-11:00"]
            String[] pieces = raw.split("[,;]");
            for (String piece : pieces) {
                String cleaned = sanitizeSlotText(piece);
                if (!cleaned.isEmpty()) {
                    normalized.add(cleaned);
                }
            }
        }

        return normalized;
    }

    private String normalizeTimeText(String value) {
        LocalTime parsed = parseLocalTimeFlexible(value);
        if (parsed == null) {
            return value == null ? "" : value.trim();
        }
        return parsed.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private LocalTime parseLocalTimeFlexible(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String cleaned = sanitizeSlotText(value);
        String upper = cleaned.toUpperCase(Locale.ENGLISH);
        String[] patterns = {"H:mm", "HH:mm", "H:mm:ss", "HH:mm:ss", "h:mm a", "hh:mm a"};
        for (String pattern : patterns) {
            try {
                if (pattern.contains("a")) {
                    return LocalTime.parse(upper, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
                }
                return LocalTime.parse(cleaned, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) {
                // Try next format
            }
        }

        return null;
    }

    private String sanitizeSlotText(String value) {
        if (value == null) return "";
        return value
                .replace('\u2013', '-')
                .replace('\u2014', '-')
                .replace('\u00A0', ' ')
                .replace('\u2007', ' ')
                .replace('\u202F', ' ')
                .replace("\"", "")
                .replace("'", "")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String extractFirstTimeToken(String value) {
        if (value == null) return "";
        String cleaned = sanitizeSlotText(value);
        Matcher matcher = TIME_TOKEN_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return cleaned;
    }

    /**
     * Saves a new doctor to the database.
     * 
     * @param doctor The doctor object to be saved
     * @return 1 for success, -1 if doctor already exists, 0 for internal errors
     */
    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            // Check if doctor already exists by email
            Doctor existing = doctorRepository.findByEmail(doctor.getEmail());
            if (existing != null) {
                return -1; // Doctor already exists
            }
            
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            throw new RuntimeException("Doctor save failed: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the details of an existing doctor.
     * 
     * @param doctor The doctor object with updated details
     * @return 1 for success, -1 if doctor not found, 0 for internal errors
     */
    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            // Check if doctor exists by ID
            Optional<Doctor> existing = doctorRepository.findById(doctor.getId());
            if (existing.isEmpty()) {
                return -1; // Doctor not found
            }
            
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            return 0; // Internal error
        }
    }

    /**
     * Retrieves a list of all doctors.
     * 
     * @return List of all doctors
     */
    @Transactional
    public List<Doctor> getDoctors() {
        try {
            return doctorRepository.findAll();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Deletes a doctor by ID along with all associated appointments.
     * 
     * @param id The ID of the doctor to be deleted
     * @return 1 for success, -1 if doctor not found, 0 for internal errors
     */
    @Transactional
    public int deleteDoctor(long id) {
        try {
            // Check if doctor exists
            Optional<Doctor> doctor = doctorRepository.findById(id);
            if (doctor.isEmpty()) {
                return -1; // Doctor not found
            }
            
            // Delete all appointments for this doctor
            appointmentRepository.deleteAllByDoctorId(id);
            
            // Delete the doctor
            doctorRepository.deleteById(id);
            return 1; // Success
        } catch (Exception e) {
            return 0; // Internal error
        }
    }

    /**
     * Validates a doctor's login credentials.
     * 
     * @param login The login object containing email and password
     * @return ResponseEntity with a token if valid, or an error message if not
     */
    @Transactional
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Find doctor by email
            Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());
            
            if (doctor == null) {
                response.put("message", "Doctor not found");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            // Verify password
            if (!doctor.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            
            // Generate token
            String token = tokenService.generateToken(doctor.getEmail());
            response.put("token", token);
            response.put("message", "Login successful");
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("message", "Error validating doctor: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Finds doctors by their name.
     * 
     * @param name The name of the doctor to search for
     * @return Map with the list of doctors matching the name
     */
    @Transactional
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = new ArrayList<>();
        
        try {
            doctors = doctorRepository.findByNameLike(name);
            response.put("message", "Doctors found");
            response.put("doctors", doctors);
        } catch (Exception e) {
            response.put("message", "Error finding doctors: " + e.getMessage());
            response.put("doctors", doctors);
        }
        
        return response;
    }

    /**
     * Filters doctors by name, specialty, and availability during AM/PM.
     * 
     * @param name Doctor's name
     * @param specialty Doctor's specialty
     * @param amOrPm Time of day: AM/PM
     * @return Map with the filtered list of doctors
     */
    @Transactional
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = new ArrayList<>();
        
        try {
            doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
            doctors = filterDoctorByTime(doctors, amOrPm);
            response.put("message", "Doctors filtered successfully");
            response.put("doctors", doctors);
        } catch (Exception e) {
            response.put("message", "Error filtering doctors: " + e.getMessage());
            response.put("doctors", doctors);
        }
        
        return response;
    }

    /**
     * Filters doctors by name and availability during AM/PM.
     * 
     * @param name Doctor's name
     * @param amOrPm Time of day: AM/PM
     * @return Map with the filtered list of doctors
     */
    @Transactional
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = new ArrayList<>();
        
        try {
            doctors = doctorRepository.findByNameLike(name);
            doctors = filterDoctorByTime(doctors, amOrPm);
            response.put("message", "Doctors filtered successfully");
            response.put("doctors", doctors);
        } catch (Exception e) {
            response.put("message", "Error filtering doctors: " + e.getMessage());
            response.put("doctors", doctors);
        }
        
        return response;
    }

    /**
     * Filters doctors by name and specialty.
     * 
     * @param name Doctor's name
     * @param specialty Doctor's specialty
     * @return Map with the filtered list of doctors
     */
    @Transactional
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = new ArrayList<>();
        
        try {
            doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
            response.put("message", "Doctors filtered successfully");
            response.put("doctors", doctors);
        } catch (Exception e) {
            response.put("message", "Error filtering doctors: " + e.getMessage());
            response.put("doctors", doctors);
        }
        
        return response;
    }

    /**
     * Filters doctors by specialty and availability during AM/PM.
     * 
     * @param specialty Doctor's specialty
     * @param amOrPm Time of day: AM/PM
     * @return Map with the filtered list of doctors
     */
    @Transactional
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = new ArrayList<>();
        
        try {
            doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
            doctors = filterDoctorByTime(doctors, amOrPm);
            response.put("message", "Doctors filtered successfully");
            response.put("doctors", doctors);
        } catch (Exception e) {
            response.put("message", "Error filtering doctors: " + e.getMessage());
            response.put("doctors", doctors);
        }
        
        return response;
    }

    /**
     * Filters doctors by specialty.
     * 
     * @param specialty Doctor's specialty
     * @return Map with the filtered list of doctors
     */
    @Transactional
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = new ArrayList<>();
        
        try {
            doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
            response.put("message", "Doctors filtered successfully");
            response.put("doctors", doctors);
        } catch (Exception e) {
            response.put("message", "Error filtering doctors: " + e.getMessage());
            response.put("doctors", doctors);
        }
        
        return response;
    }

    /**
     * Filters all doctors by availability during AM/PM.
     * 
     * @param amOrPm Time of day: AM/PM
     * @return Map with the filtered list of doctors
     */
    @Transactional
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = new ArrayList<>();
        
        try {
            doctors = doctorRepository.findAll();
            doctors = filterDoctorByTime(doctors, amOrPm);
            response.put("message", "Doctors filtered successfully");
            response.put("doctors", doctors);
        } catch (Exception e) {
            response.put("message", "Error filtering doctors: " + e.getMessage());
            response.put("doctors", doctors);
        }
        
        return response;
    }

    /**
     * Private helper method to filter a list of doctors by their available times (AM/PM).
     * 
     * @param doctors The list of doctors to filter
     * @param amOrPm Time of day: AM/PM
     * @return Filtered list of doctors
     */
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        List<Doctor> filteredDoctors = new ArrayList<>();
        
        try {
            for (Doctor doctor : doctors) {
                // Check if doctor has availability in the specified time period
                if (doctor.getAvailableTimes() != null && !doctor.getAvailableTimes().isEmpty()) {
                    boolean hasTimeSlot = false;
                    
                    for (var availableTime : doctor.getAvailableTimes()) {
                        // Parse the time string (format: "HH:mm" or "HH:mm-HH:mm")
                        String[] timeParts = availableTime.split("-");
                        if (timeParts.length > 0) {
                            LocalTime time = LocalTime.parse(timeParts[0]);
                            
                            if ("AM".equalsIgnoreCase(amOrPm)) {
                                // AM = 8:00 to 12:00
                                if (time.isAfter(LocalTime.of(7, 59)) && time.isBefore(LocalTime.of(12, 1))) {
                                    hasTimeSlot = true;
                                    break;
                                }
                            } else if ("PM".equalsIgnoreCase(amOrPm)) {
                                // PM = 12:00 to 17:00
                                if (time.isAfter(LocalTime.of(11, 59)) && time.isBefore(LocalTime.of(17, 1))) {
                                    hasTimeSlot = true;
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (hasTimeSlot) {
                        filteredDoctors.add(doctor);
                    }
                }
            }
        } catch (Exception e) {
            // Return all doctors if filtering fails
            return doctors;
        }
        
        return filteredDoctors;
    }
}
