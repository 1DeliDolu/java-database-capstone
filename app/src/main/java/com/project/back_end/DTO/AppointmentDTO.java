package com.project.back_end.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentDTO {
    
    // Core appointment fields
    private final Long id;
    private final Long doctorId;
    private final String doctorName;
    private final Long patientId;
    private final String patientName;
    private final String patientEmail;
    private final String patientPhone;
    private final String patientAddress;
    private final LocalDateTime appointmentTime;
    private final int status;
    
    // Derived fields (calculated from appointmentTime)
    private final LocalDate appointmentDate;
    private final LocalTime appointmentTimeOnly;
    private final LocalDateTime endTime;

    /**
     * Constructor that initializes all fields and computes derived fields.
     * 
     * @param id Unique identifier for the appointment
     * @param doctorId ID of the doctor assigned to the appointment
     * @param doctorName Full name of the doctor
     * @param patientId ID of the patient
     * @param patientName Full name of the patient
     * @param patientEmail Email address of the patient
     * @param patientPhone Contact number of the patient
     * @param patientAddress Residential address of the patient
     * @param appointmentTime Full date and time of the appointment
     * @param status Appointment status (e.g., scheduled:0, completed:1)
     */
    public AppointmentDTO(Long id, Long doctorId, String doctorName, Long patientId, 
                          String patientName, String patientEmail, String patientPhone, 
                          String patientAddress, LocalDateTime appointmentTime, int status) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.patientAddress = patientAddress;
        this.appointmentTime = appointmentTime;
        this.status = status;
        
        // Calculate derived fields
        this.appointmentDate = appointmentTime.toLocalDate();
        this.appointmentTimeOnly = appointmentTime.toLocalTime();
        this.endTime = appointmentTime.plusHours(1);
    }

    // Getter methods
    public Long getId() {
        return id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public int getStatus() {
        return status;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTimeOnly() {
        return appointmentTimeOnly;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
