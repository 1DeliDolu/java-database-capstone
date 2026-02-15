/**
 * Patient Services Module
 *
 * Handles all API interactions related to patient data.
 * Centralizes patient-related API calls for:
 * - Patient signup/registration
 * - Patient login/authentication
 * - Fetching patient profile data
 * - Fetching patient appointments
 * - Filtering/searching appointments
 *
 * By organizing patient logic in this module:
 * - UI code (dashboards, pages) stays cleaner and focused on rendering
 * - Service layer handles all backend communication
 * - Code is reusable across different pages and components
 * - Easier to debug, test, and maintain
 * - Reduces the risk of bugs from repeated logic
 */

// Import the base API URL from the config file
import { API_BASE_URL } from "../config/config.js";

// Define the patient API endpoint
const PATIENT_API = API_BASE_URL + "/api/patients";

/**
 * Register a new patient (Signup)
 *
 * @param {Object} data - Patient registration data
 *   - data.name: Patient's full name
 *   - data.email: Patient's email address
 *   - data.password: Patient's password
 *   - data.phone: Patient's phone number (optional)
 *   - data.dateOfBirth: Patient's date of birth (optional)
 *
 * @returns {Promise<Object>} - { success: boolean, message: string }
 */
export async function patientSignup(data) {
  try {
    // Step 1: Validate input
    if (!data || !data.email || !data.password) {
      throw new Error("Email and password are required");
    }

    // Step 2: Send POST request with patient data
    const response = await fetch(`${PATIENT_API}/signup`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });

    // Step 3: Parse response
    const result = await response.json();

    // Step 4: Return structured response
    if (!response.ok) {
      throw new Error(result.message || "Signup failed");
    }

    return {
      success: response.ok,
      message: result.message || "Patient registered successfully",
    };
  } catch (error) {
    // Step 5: Handle errors
    console.error("Error during patient signup:", error);
    return {
      success: false,
      message: error.message || "An error occurred during signup",
    };
  }
}

/**
 * Authenticate a patient (Login)
 *
 * Returns the full fetch response so caller can:
 * - Check response.ok or response.status
 * - Extract token from response JSON
 * - Handle various error scenarios
 *
 * @param {Object} data - Login credentials
 *   - data.email: Patient's email
 *   - data.password: Patient's password
 *
 * @returns {Promise<Response>} - Fetch response object
 */
export async function patientLogin(data) {
  try {
    // Step 1: Validate input
    if (!data || !data.email || !data.password) {
      throw new Error("Email and password are required");
    }

    // Step 2: Log for debugging (remove in production)
    console.log("Patient login attempt for:", data.email);

    // Step 3: Send POST request with login credentials
    const response = await fetch(`${PATIENT_API}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });

    // Step 4: Return full response for caller to handle
    return response;
  } catch (error) {
    // Step 5: Log errors for debugging
    console.error("Error during patient login:", error);
    throw error;
  }
}

/**
 * Fetch logged-in patient's profile data
 *
 * Used for:
 * - Displaying patient profile information
 * - Booking appointments (to pre-fill patient data)
 * - Patient dashboard
 *
 * @param {String} token - Authentication token from localStorage
 *
 * @returns {Promise<Object|null>} - Patient object or null if error
 */
export async function getPatientData(token) {
  try {
    // Step 1: Validate token
    if (!token) {
      throw new Error("No authentication token provided");
    }

    // Step 2: Send GET request with authorization
    const response = await fetch(`${PATIENT_API}/profile`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });

    // Step 3: Check response
    if (!response.ok) {
      console.error("Failed to fetch patient data:", response.statusText);
      return null;
    }

    // Step 4: Parse and return patient data
    const data = await response.json();
    return data.patient || data.data || data;
  } catch (error) {
    // Step 5: Handle errors gracefully
    console.error("Error fetching patient details:", error);
    return null;
  }
}

/**
 * Fetch appointments for a patient
 *
 * Supports both patient and doctor views:
 * - Patient: View their own appointments
 * - Doctor: View patient's appointments (with authorization)
 *
 * @param {Number|String} id - Patient's unique ID
 * @param {String} token - Authentication token
 * @param {String} user - User type ("patient" or "doctor") for role-based backend logic
 *
 * @returns {Promise<Array|null>} - Array of appointments or null if error
 */
export async function getPatientAppointments(id, token, user) {
  try {
    // Step 1: Validate inputs
    if (!id || !token || !user) {
      throw new Error("Patient ID, token, and user type are required");
    }

    // Step 2: Send GET request for appointments
    const response = await fetch(`${PATIENT_API}/${id}/appointments`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
        "X-User-Type": user, // Send user type for backend role checking
      },
    });

    // Step 3: Check response
    if (!response.ok) {
      console.error("Failed to fetch appointments:", response.statusText);
      return null;
    }

    // Step 4: Parse and return appointments
    const data = await response.json();
    console.log("Patient appointments fetched:", data.appointments);
    return data.appointments || [];
  } catch (error) {
    // Step 5: Handle errors
    console.error("Error fetching patient appointments:", error);
    return null;
  }
}

/**
 * Filter appointments by condition
 *
 * Supports filtering such as:
 * - By status: "pending", "consulted", "completed", etc.
 * - By patient name
 * - By date range
 * - Combinations of above
 *
 * @param {String} condition - Filter condition (e.g., "pending", "consulted")
 * @param {String} name - Patient name to filter by (optional)
 * @param {String} token - Authentication token
 *
 * @returns {Promise<Array>} - Filtered appointments array or empty array
 */
export async function filterAppointments(condition, name, token) {
  try {
    // Step 1: Validate inputs
    if (!token) {
      throw new Error("Authentication token is required");
    }

    // Step 2: Build query parameters
    const queryParams = new URLSearchParams();
    if (condition && condition.trim()) {
      queryParams.append("condition", condition.trim());
    }
    if (name && name.trim()) {
      queryParams.append("name", name.trim());
    }

    // Step 3: Construct URL with filters
    const url = queryParams.toString()
      ? `${PATIENT_API}/appointments/filter?${queryParams.toString()}`
      : `${PATIENT_API}/appointments/filter`;

    // Step 4: Send GET request with filters
    const response = await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });

    // Step 5: Check response
    if (!response.ok) {
      console.error("Failed to filter appointments:", response.statusText);
      return [];
    }

    // Step 6: Parse and return filtered results
    const data = await response.json();
    return data.appointments || [];
  } catch (error) {
    // Step 7: Handle errors
    console.error("Error filtering appointments:", error);
    alert("Failed to filter appointments. Please try again.");
    return [];
  }
}

/**
 * Create a new appointment for a patient
 *
 * @param {Object} appointmentData - Appointment details
 *   - appointmentData.doctorId: ID of selected doctor
 *   - appointmentData.date: Appointment date
 *   - appointmentData.time: Appointment time
 *   - appointmentData.reason: Reason for visit
 * @param {String} token - Authentication token
 *
 * @returns {Promise<Object>} - { success: boolean, message: string, appointment?: Object }
 */
export async function createAppointment(appointmentData, token) {
  try {
    // Step 1: Validate inputs
    if (!appointmentData || !token) {
      throw new Error("Appointment data and token are required");
    }

    // Step 2: Send POST request to create appointment
    const response = await fetch(`${PATIENT_API}/appointments`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(appointmentData),
    });

    // Step 3: Parse response
    const data = await response.json();

    // Step 4: Return structured response
    return {
      success: response.ok,
      message: data.message || "Appointment created successfully",
      appointment: data.appointment || null,
    };
  } catch (error) {
    // Step 5: Handle errors
    console.error("Error creating appointment:", error);
    return {
      success: false,
      message: error.message || "Failed to create appointment",
    };
  }
}
