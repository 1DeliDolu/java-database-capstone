package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    @Autowired
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
            // Fetch all appointments for the doctor on the specified date
            LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            
            var appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId, startOfDay, endOfDay);
            
            // Generate available time slots (assuming 1-hour slots from 8 AM to 5 PM)
            List<String> allSlots = new ArrayList<>();
            for (int hour = 8; hour < 17; hour++) {
                allSlots.add(String.format("%02d:00", hour));
            }
            
            // Remove booked slots
            for (var appointment : appointments) {
                int hour = appointment.getAppointmentTime().getHour();
                String bookedSlot = String.format("%02d:00", hour);
                allSlots.remove(bookedSlot);
            }
            
            availableSlots = allSlots;
        } catch (Exception e) {
            // Return empty list on error
        }
        
        return availableSlots;
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
            return 0; // Internal error
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
                        LocalTime time = availableTime.getStartTime();
                        
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
