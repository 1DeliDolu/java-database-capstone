package com.project.back_end.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AdminAppointmentUpdateDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class AdminManagementService {

    private final TokenService tokenService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;

    public AdminManagementService(
            TokenService tokenService,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            DoctorService doctorService) {
        this.tokenService = tokenService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
    }

    public boolean isValidAdminToken(String token) {
        return tokenService.validateToken(token, "admin");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOverview() {
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> doctors = new ArrayList<>();
        for (Doctor d : doctorRepository.findAll()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", d.getId());
            row.put("name", d.getName());
            row.put("specialty", d.getSpecialty());
            row.put("email", d.getEmail());
            row.put("phone", d.getPhone());
            row.put("availableTimes", d.getAvailableTimes());
            doctors.add(row);
        }

        List<Map<String, Object>> patients = new ArrayList<>();
        for (Patient p : patientRepository.findAll()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", p.getId());
            row.put("name", p.getName());
            row.put("email", p.getEmail());
            row.put("phone", p.getPhone());
            row.put("address", p.getAddress());
            patients.add(row);
        }

        List<Map<String, Object>> appointments = new ArrayList<>();
        for (Appointment a : appointmentRepository.findAll()) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", a.getId());
            row.put("doctorId", a.getDoctor() != null ? a.getDoctor().getId() : null);
            row.put("doctorName", a.getDoctor() != null ? a.getDoctor().getName() : null);
            row.put("patientId", a.getPatient() != null ? a.getPatient().getId() : null);
            row.put("patientName", a.getPatient() != null ? a.getPatient().getName() : null);
            row.put("appointmentTime", a.getAppointmentTime());
            row.put("status", a.getStatus());
            row.put("statusName", a.getStatusName());
            appointments.add(row);
        }

        response.put("doctors", doctors);
        response.put("patients", patients);
        response.put("appointments", appointments);
        return response;
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateDoctor(Doctor incoming) {
        Map<String, String> response = new HashMap<>();
        if (incoming.getId() == null) {
            response.put("message", "Doctor id is required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        return doctorRepository.findById(incoming.getId())
                .map(existing -> {
                    existing.setName(incoming.getName());
                    existing.setSpecialty(incoming.getSpecialty());
                    existing.setEmail(incoming.getEmail());
                    existing.setPhone(incoming.getPhone());
                    if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
                        existing.setPassword(incoming.getPassword());
                    }
                    existing.setAvailableTimes(incoming.getAvailableTimes());
                    doctorRepository.save(existing);
                    response.put("message", "Doctor updated");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    response.put("message", "Doctor not found");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    @Transactional
    public ResponseEntity<Map<String, String>> deleteDoctor(Long id) {
        Map<String, String> response = new HashMap<>();
        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            response.put("message", "Doctor deleted");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        if (result == -1) {
            response.put("message", "Doctor not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        response.put("message", "Internal error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updatePatient(Patient incoming) {
        Map<String, String> response = new HashMap<>();
        if (incoming.getId() == null) {
            response.put("message", "Patient id is required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        return patientRepository.findById(incoming.getId())
                .map(existing -> {
                    existing.setName(incoming.getName());
                    existing.setEmail(incoming.getEmail());
                    existing.setPhone(incoming.getPhone());
                    existing.setAddress(incoming.getAddress());
                    if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
                        existing.setPassword(incoming.getPassword());
                    }
                    patientRepository.save(existing);
                    response.put("message", "Patient updated");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    response.put("message", "Patient not found");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    @Transactional
    public ResponseEntity<Map<String, String>> deletePatient(Long id) {
        Map<String, String> response = new HashMap<>();
        if (!patientRepository.existsById(id)) {
            response.put("message", "Patient not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        appointmentRepository.deleteAllByPatientId(id);
        patientRepository.deleteById(id);
        response.put("message", "Patient deleted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(AdminAppointmentUpdateDTO incoming) {
        Map<String, String> response = new HashMap<>();
        if (incoming.getId() == null) {
            response.put("message", "Appointment id is required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        return appointmentRepository.findById(incoming.getId())
                .map(existing -> {
                    if (incoming.getDoctorId() != null) {
                        Doctor doctor = doctorRepository.findById(incoming.getDoctorId()).orElse(null);
                        if (doctor == null) {
                            response.put("message", "Doctor not found");
                            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                        }
                        existing.setDoctor(doctor);
                    }

                    if (incoming.getPatientId() != null) {
                        Patient patient = patientRepository.findById(incoming.getPatientId()).orElse(null);
                        if (patient == null) {
                            response.put("message", "Patient not found");
                            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                        }
                        existing.setPatient(patient);
                    }

                    if (incoming.getAppointmentTime() != null) {
                        existing.setAppointmentTime(incoming.getAppointmentTime());
                    }

                    if (incoming.getStatus() != null) {
                        existing.setStatus(incoming.getStatus());
                    }

                    appointmentRepository.save(existing);
                    response.put("message", "Appointment updated");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    response.put("message", "Appointment not found");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    @Transactional
    public ResponseEntity<Map<String, String>> deleteAppointment(Long id) {
        Map<String, String> response = new HashMap<>();
        if (!appointmentRepository.existsById(id)) {
            response.put("message", "Appointment not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        appointmentRepository.deleteById(id);
        response.put("message", "Appointment deleted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
