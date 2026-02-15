package com.project.back_end.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    /**
     * Find appointments for a doctor within a given time range.
     * Fetches doctor and availability information eagerly.
     * 
     * @param doctorId The ID of the doctor
     * @param start The start of the time range (inclusive)
     * @param end The end of the time range (inclusive)
     * @return List of appointments for the doctor in the specified time range
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor LEFT JOIN FETCH a.availability " +
           "WHERE a.doctor.id = :doctorId AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
    
    /**
     * Find appointments by doctor ID, partial patient name (case-insensitive), and time range.
     * Fetches patient and doctor details eagerly.
     * 
     * @param doctorId The ID of the doctor
     * @param patientName Partial patient name to match (case-insensitive)
     * @param start The start of the time range (inclusive)
     * @param end The end of the time range (inclusive)
     * @return List of appointments matching the criteria
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor " +
           "WHERE a.doctor.id = :doctorId AND LOWER(a.patient.name) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
           "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            Long doctorId, String patientName, LocalDateTime start, LocalDateTime end);
    
    /**
     * Delete all appointments for a specific doctor.
     * 
     * @param doctorId The ID of the doctor whose appointments should be deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.doctor.id = :doctorId")
    void deleteAllByDoctorId(Long doctorId);
    
    /**
     * Find all appointments for a specific patient.
     * 
     * @param patientId The ID of the patient
     * @return List of all appointments for the patient
     */
    List<Appointment> findByPatientId(Long patientId);
    
    /**
     * Find appointments for a patient by status, ordered by appointment time in ascending order.
     * 
     * @param patientId The ID of the patient
     * @param status The appointment status (0=scheduled, 1=completed, etc.)
     * @return List of appointments ordered by appointment time (earliest first)
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);
    
    /**
     * Filter appointments by partial doctor name and patient ID (case-insensitive).
     * 
     * @param doctorName Partial doctor name to match
     * @param patientId The ID of the patient
     * @return List of appointments matching the criteria
     */
    @Query("SELECT a FROM Appointment a WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
           "AND a.patient.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(String doctorName, Long patientId);
    
    /**
     * Filter appointments by doctor name, patient ID, and status (case-insensitive).
     * 
     * @param doctorName Partial doctor name to match
     * @param patientId The ID of the patient
     * @param status The appointment status
     * @return List of appointments matching the criteria
     */
    @Query("SELECT a FROM Appointment a WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
           "AND a.patient.id = :patientId AND a.status = :status")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(String doctorName, Long patientId, int status);
}
