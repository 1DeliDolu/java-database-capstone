package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 7 days in milliseconds

    @Autowired
    public TokenService(AdminRepository adminRepository, DoctorRepository doctorRepository,
                       PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Retrieves the HMAC SHA signing key used for JWT token signing and verification.
     *
     * @return SecretKey used for signing and verifying JWT tokens
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a JWT token for a given user identifier.
     *
     * @param identifier The unique identifier for the user (username for admin, email for doctor/patient)
     * @return The generated JWT token as a string
     */
    public String generateToken(String identifier) {
        try {
            return Jwts.builder()
                    .setSubject(identifier)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error generating token: " + e.getMessage());
        }
    }

    /**
     * Extracts the user identifier (email or username) from a JWT token.
     *
     * @param token The JWT token from which to extract the identifier
     * @return The identifier (subject) extracted from the token
     */
    public String extractEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates a JWT token for a specific user type (admin, doctor, or patient).
     *
     * @param token The JWT token to validate
     * @param user  The user type (admin, doctor, or patient)
     * @return true if the token is valid for the specified user type, false otherwise
     */
    public boolean validateToken(String token, String user) {
        try {
            String identifier = extractEmail(token);
            
            if (identifier == null) {
                return false;
            }
            
            // Validate based on user type
            if ("admin".equalsIgnoreCase(user)) {
                return adminRepository.findByUsername(identifier) != null;
            } else if ("doctor".equalsIgnoreCase(user)) {
                return doctorRepository.findByEmail(identifier) != null;
            } else if ("patient".equalsIgnoreCase(user)) {
                return patientRepository.findByEmail(identifier) != null;
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
