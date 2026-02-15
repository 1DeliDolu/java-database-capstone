/**
 * Appointment Record Service Module
 * 
 * Handles all API interactions related to appointment management.
 * Centralizes appointment-related API calls for:
 * - Fetching appointments (doctor and patient views)
 * - Booking new appointments
 * - Updating appointment status/details
 * 
 * Dependencies:
 * - API_BASE_URL from ../config/config.js
 */

import { API_BASE_URL } from "../config/config.js";

const APPOINTMENT_API = `${API_BASE_URL}/appointments`;

/**
 * Get all appointments for a doctor
 * 
 * Used by doctors to view their appointments, filtered by date and/or patient name.
 * 
 * @param {String} date - Appointment date (format: YYYY-MM-DD or "today")
 * @param {String} patientName - Patient name to filter by (optional, pass empty string)
 * @param {String} token - Authentication token
 * 
 * @returns {Promise<Array>} - Array of appointment objects
 * @throws {Error} - If API call fails
 */
export async function getAllAppointments(date, patientName, token) {
  try {
    if (!token) {
      throw new Error('Authentication token is required');
    }

    const safePatientName = (patientName && patientName.trim()) ? patientName.trim() : 'null';
    const hasDate = Boolean(date && String(date).trim());
    const url = hasDate
      ? `${APPOINTMENT_API}/${encodeURIComponent(String(date).trim())}/${encodeURIComponent(safePatientName)}/${encodeURIComponent(token)}`
      : `${APPOINTMENT_API}/all/${encodeURIComponent(safePatientName)}/${encodeURIComponent(token)}`;

    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch appointments: ${response.statusText}`);
    }

    const data = await response.json();
    return data.appointments || data || [];
  } catch (error) {
    console.error('Error fetching appointments:', error);
    throw error;
  }
}

/**
 * Book a new appointment
 * 
 * Creates a new appointment for a patient with a doctor.
 * 
 * @param {Object} appointment - Appointment details
 *   - appointment.doctorId: ID of the selected doctor
 *   - appointment.patientId: ID of the patient
 *   - appointment.date: Appointment date (YYYY-MM-DD)
 *   - appointment.time: Appointment time (HH:MM)
 *   - appointment.reason: Reason for visit
 * @param {String} token - Authentication token
 * 
 * @returns {Promise<Object>} - { success: boolean, message: string, appointment?: Object }
 */
export async function bookAppointment(appointment, token) {
  try {
    // Step 1: Validate inputs
    if (!appointment || !token) {
      throw new Error('Appointment data and token are required');
    }

    const response = await fetch(`${APPOINTMENT_API}/${encodeURIComponent(token)}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(appointment)
    });

    // Step 3: Parse response
    const data = await response.json();

    // Step 4: Return structured response
    return {
      success: response.ok,
      message: data.message || 'Appointment booked successfully',
      appointment: data.appointment || null
    };
  } catch (error) {
    console.error('Error booking appointment:', error);
    return {
      success: false,
      message: 'Network error. Please try again later.'
    };
  }
}

/**
 * Update an existing appointment
 * 
 * Modifies appointment details (date, time, status, etc.).
 * 
 * @param {Object} appointment - Updated appointment details
 *   - appointment.id: Appointment ID (required)
 *   - appointment.date: New date (optional)
 *   - appointment.time: New time (optional)
 *   - appointment.status: New status (optional)
 * @param {String} token - Authentication token
 * 
 * @returns {Promise<Object>} - { success: boolean, message: string }
 */
export async function updateAppointment(appointment, token) {
  try {
    // Step 1: Validate inputs
    if (!appointment || !appointment.id || !token) {
      throw new Error('Appointment ID and token are required');
    }

    const response = await fetch(`${APPOINTMENT_API}/${encodeURIComponent(token)}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(appointment)
    });

    // Step 3: Parse response
    const data = await response.json();

    // Step 4: Return structured response
    return {
      success: response.ok,
      message: data.message || 'Appointment updated successfully'
    };
  } catch (error) {
    console.error('Error updating appointment:', error);
    return {
      success: false,
      message: error.message || 'Failed to update appointment'
    };
  }
}

/**
 * Cancel/Delete an appointment
 * 
 * Removes an appointment from the system.
 * 
 * @param {Number|String} appointmentId - ID of appointment to cancel
 * @param {String} token - Authentication token
 * 
 * @returns {Promise<Object>} - { success: boolean, message: string }
 */
export async function cancelAppointment(appointmentId, token) {
  try {
    // Step 1: Validate inputs
    if (!appointmentId || !token) {
      throw new Error('Appointment ID and token are required');
    }

    const response = await fetch(`${APPOINTMENT_API}/${encodeURIComponent(appointmentId)}/${encodeURIComponent(token)}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    });

    // Step 3: Parse response
    const data = await response.json();

    // Step 4: Return structured response
    return {
      success: response.ok,
      message: data.message || 'Appointment cancelled successfully'
    };
  } catch (error) {
    console.error('Error cancelling appointment:', error);
    return {
      success: false,
      message: error.message || 'Failed to cancel appointment'
    };
  }
}
