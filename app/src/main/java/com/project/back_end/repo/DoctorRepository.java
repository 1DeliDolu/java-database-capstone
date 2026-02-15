package com.project.back_end.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    /**
     * Find a doctor by their email address.
     * 
     * @param email The email address of the doctor
     * @return The doctor with the specified email, or null if not found
     */
    Doctor findByEmail(String email);
    
    /**
     * Find doctors by partial name match using LIKE pattern.
     * 
     * @param name Partial doctor name to match
     * @return List of doctors matching the name pattern
     */
    @Query("SELECT d FROM Doctor d WHERE d.name LIKE CONCAT('%', :name, '%')")
    List<Doctor> findByNameLike(String name);
    
    /**
     * Filter doctors by partial name (case-insensitive) and exact specialty (case-insensitive).
     * 
     * @param name Partial doctor name to match
     * @param specialty The specialty to match exactly
     * @return List of doctors matching both criteria
     */
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND LOWER(d.specialty) = LOWER(:specialty)")
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(String name, String specialty);
    
    /**
     * Find doctors by specialty, ignoring case.
     * 
     * @param specialty The specialty to search for
     * @return List of doctors with the specified specialty
     */
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.specialty) = LOWER(:specialty)")
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}
