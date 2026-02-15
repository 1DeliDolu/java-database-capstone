/**
 * Doctor Services Module
 *
 * Handles all API interactions related to doctor data.
 * Centralizes doctor-related API calls for:
 * - Fetching all doctors
 * - Deleting doctors (admin only)
 * - Creating/saving new doctors (admin only)
 * - Filtering doctors by name, time, or specialty
 *
 * By organizing this logic separately from UI code:
 * - Code is more modular and maintainable
 * - Reduces repetition of fetch logic
 * - Separates responsibilities between UI and backend logic
 * - Makes the app easier to scale and debug
 */

// Import the base API URL from the config file
import { API_BASE_URL } from "../config/config.js";

// Define the doctor API endpoint
const DOCTOR_API = API_BASE_URL + "/doctor";

/**
 * Fetch all doctors from the API
 *
 * @returns {Promise<Array>} - Array of doctor objects, or empty array if error
 */
export async function getDoctors() {
  try {
    // Step 1: Send GET request to doctor endpoint
    const response = await fetch(DOCTOR_API, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    // Step 2: Check if response is successful
    if (!response.ok) {
      throw new Error(`Failed to fetch doctors: ${response.statusText}`);
    }

    // Step 3: Convert response to JSON and extract doctors array
    const data = await response.json();
    return data.doctors || data || [];
  } catch (error) {
    // Step 4: Handle errors gracefully
    console.error("Error fetching doctors:", error);
    return []; // Return empty array to avoid breaking frontend
  }
}

/**
 * Delete a specific doctor by ID (Admin only)
 *
 * @param {Number|String} id - Doctor's unique ID
 * @param {String} token - Authentication token for authorization
 * @returns {Promise<Object>} - { success: boolean, message: string }
 */
export async function deleteDoctor(id, token) {
  try {
    // Step 1: Construct delete request
    const response = await fetch(`${DOCTOR_API}/${encodeURIComponent(id)}/${encodeURIComponent(token)}`, {
      method: "DELETE",
      headers: { "Content-Type": "application/json" },
    });

    // Step 2: Parse response
    const data = await response.json();

    // Step 3: Return structured response
    return {
      success: response.ok,
      message: data.message || "Doctor deleted successfully",
    };
  } catch (error) {
    // Step 4: Handle errors
    console.error("Error deleting doctor:", error);
    return {
      success: false,
      message: error.message || "Failed to delete doctor",
    };
  }
}

/**
 * Save (create) a new doctor (Admin only)
 *
 * @param {Object} doctor - Doctor object with properties like name, email, specialty, etc.
 * @param {String} token - Authentication token for authorization
 * @returns {Promise<Object>} - { success: boolean, message: string, doctor?: Object }
 */
export async function saveDoctor(doctor, token) {
  try {
    // Step 1: Validate input
    if (!doctor || typeof doctor !== "object") {
      throw new Error("Invalid doctor object");
    }

    // Step 2: Send POST request with doctor data
    const response = await fetch(`${DOCTOR_API}/${encodeURIComponent(token)}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(doctor),
    });

    // Step 3: Parse response
    const data = await response.json();

    // Step 4: Return structured response with created doctor data if available
    return {
      success: response.ok,
      message: data.message || "Doctor saved successfully",
      doctor: data.doctor || data.data || null,
    };
  } catch (error) {
    // Step 5: Handle errors with logging
    console.error("Error saving doctor:", error);
    return {
      success: false,
      message: error.message || "Failed to save doctor",
    };
  }
}

/**
 * Filter doctors by name, time availability, and/or specialty
 *
 * Supports multiple filtering scenarios:
 * - Filter by name only
 * - Filter by time only
 * - Filter by specialty only
 * - Combine multiple filters
 * - No filters returns all doctors
 *
 * @param {String} name - Doctor name (optional, pass empty string or null to skip)
 * @param {String} time - Time slot (AM/PM) (optional)
 * @param {String} specialty - Medical specialty (optional)
 * @returns {Promise<Array>} - Filtered array of doctors, or empty array if error
 */
export async function filterDoctors(name = "", time = "", specialty = "") {
  try {
    const safeName = name && name.trim() ? name.trim() : "null";
    const safeTime = time && time.trim() ? time.trim() : "null";
    const safeSpecialty = specialty && specialty.trim() ? specialty.trim() : "null";
    const url = `${DOCTOR_API}/filter/${encodeURIComponent(safeName)}/${encodeURIComponent(safeTime)}/${encodeURIComponent(safeSpecialty)}`;

    // Step 3: Send GET request with filters
    const response = await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    // Step 4: Check response status
    if (!response.ok) {
      console.error("Filter doctors failed:", response.statusText);
      return [];
    }

    // Step 5: Parse and return filtered results
    const data = await response.json();
    return data.doctors || data || [];
  } catch (error) {
    // Step 6: Handle errors gracefully
    console.error("Error filtering doctors:", error);
    return []; // Return empty array to avoid breaking frontend
  }
}

/**
 * Search doctors by name (convenience function)
 *
 * @param {String} name - Doctor name to search for
 * @returns {Promise<Array>} - Matching doctors or empty array
 */
export async function searchDoctors(name) {
  return filterDoctors(name, "", "");
}

/**
 * Get doctors available at a specific time
 *
 * @param {String} time - Time slot (AM or PM)
 * @returns {Promise<Array>} - Doctors available at that time
 */
export async function getDoctorsByTime(time) {
  return filterDoctors("", time, "");
}

/**
 * Get doctors by specialty
 *
 * @param {String} specialty - Medical specialty
 * @returns {Promise<Array>} - Doctors with that specialty
 */
export async function getDoctorsBySpecialty(specialty) {
  return filterDoctors("", "", specialty);
}
