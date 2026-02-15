/**
 * Prescription Services Module
 *
 * Handles all API interactions related to prescription management.
 * Centralizes prescription-related API calls for:
 * - Adding/saving prescriptions after appointments
 * - Fetching prescription details
 * - Prescription history
 *
 * Dependencies:
 * - API_BASE_URL from ../config/config.js
 */

import { API_BASE_URL } from "../config/config.js";

const PRESCRIPTION_API = API_BASE_URL + "/api/prescriptions";

/**
 * Save a new prescription
 *
 * Creates a new prescription record after an appointment.
 * Typically used by doctors to prescribe medications/treatments.
 *
 * @param {Object} prescription - Prescription details
 *   - prescription.appointmentId: ID of the appointment
 *   - prescription.patientId: ID of the patient
 *   - prescription.medications: Array of medication objects
 *   - prescription.instructions: Prescription instructions
 *   - prescription.notes: Any additional notes
 * @param {String} token - Authentication token (doctor's token)
 *
 * @returns {Promise<Object>} - { success: boolean, message: string, prescription?: Object }
 */
export async function savePrescription(prescription, token) {
  try {
    // Step 1: Validate inputs
    if (!prescription || !token) {
      throw new Error("Prescription data and token are required");
    }

    // Step 2: Send POST request to save prescription
    const response = await fetch(PRESCRIPTION_API, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(prescription),
    });

    // Step 3: Parse response
    const result = await response.json();

    // Step 4: Return structured response
    return {
      success: response.ok,
      message: result.message || "Prescription saved successfully",
      prescription: result.prescription || null,
    };
  } catch (error) {
    console.error("Error saving prescription:", error);
    return {
      success: false,
      message: error.message || "Failed to save prescription",
    };
  }
}

/**
 * Fetch a specific prescription
 *
 * Retrieves prescription details for an appointment.
 * Can be viewed by patient or doctor (with appropriate permissions).
 *
 * @param {Number|String} appointmentId - ID of the appointment
 * @param {String} token - Authentication token
 *
 * @returns {Promise<Object|null>} - Prescription object or null if not found/error
 */
export async function getPrescription(appointmentId, token) {
  try {
    // Step 1: Validate inputs
    if (!appointmentId || !token) {
      throw new Error("Appointment ID and token are required");
    }

    // Step 2: Send GET request to fetch prescription
    const response = await fetch(`${PRESCRIPTION_API}/${appointmentId}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });

    // Step 3: Check response
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      console.error("Failed to fetch prescription:", errorData);
      throw new Error(errorData.message || "Unable to fetch prescription");
    }

    // Step 4: Parse and return prescription
    const result = await response.json();
    return result.prescription || result.data || result;
  } catch (error) {
    console.error("Error fetching prescription:", error);
    return null;
  }
}

/**
 * Get prescription history for a patient
 *
 * Fetches all prescriptions for a specific patient.
 *
 * @param {Number|String} patientId - ID of the patient
 * @param {String} token - Authentication token
 *
 * @returns {Promise<Array>} - Array of prescription objects
 */
export async function getPrescriptionHistory(patientId, token) {
  try {
    // Step 1: Validate inputs
    if (!patientId || !token) {
      throw new Error("Patient ID and token are required");
    }

    // Step 2: Send GET request to fetch prescription history
    const response = await fetch(`${PRESCRIPTION_API}/patient/${patientId}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });

    // Step 3: Check response
    if (!response.ok) {
      console.error(
        "Failed to fetch prescription history:",
        response.statusText,
      );
      return [];
    }

    // Step 4: Parse and return prescriptions
    const result = await response.json();
    return result.prescriptions || result.data || [];
  } catch (error) {
    console.error("Error fetching prescription history:", error);
    return [];
  }
}

/**
 * Update a prescription
 *
 * Modifies an existing prescription (status, instructions, etc.).
 *
 * @param {Number|String} prescriptionId - ID of the prescription
 * @param {Object} updates - Updated prescription data
 * @param {String} token - Authentication token
 *
 * @returns {Promise<Object>} - { success: boolean, message: string }
 */
export async function updatePrescription(prescriptionId, updates, token) {
  try {
    // Step 1: Validate inputs
    if (!prescriptionId || !updates || !token) {
      throw new Error("Prescription ID, updates, and token are required");
    }

    // Step 2: Send PUT request to update prescription
    const response = await fetch(`${PRESCRIPTION_API}/${prescriptionId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(updates),
    });

    // Step 3: Parse response
    const result = await response.json();

    // Step 4: Return structured response
    return {
      success: response.ok,
      message: result.message || "Prescription updated successfully",
    };
  } catch (error) {
    console.error("Error updating prescription:", error);
    return {
      success: false,
      message: error.message || "Failed to update prescription",
    };
  }
}
