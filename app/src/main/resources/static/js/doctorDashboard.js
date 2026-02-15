/**
 * Doctor Dashboard Script
 *
 * Manages the doctor's view of patient appointments:
 * - Displays patient appointment records in a table
 * - Filters appointments by date
 * - Allows searching patients by name
 * - Provides access to patient prescriptions
 *
 * Dependencies:
 * - getAllAppointments() from ./services/appointmentRecordService.js
 * - createPatientRow() from ./components/patientRows.js
 */

// Import required modules
import { getAllAppointments } from "./services/appointmentRecordService.js";

// Initialize variables
let selectedDate = "";
let patientName = null;
const token = localStorage.getItem("token");
const patientTableBody = document.getElementById("patientTableBody");

/**
 * Get today's date in YYYY-MM-DD format
 * @returns {String} Today's date
 */
function getTodayDate() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/**
 * Initialize dashboard when DOM is loaded
 */
document.addEventListener("DOMContentLoaded", function () {
  const datePicker = document.getElementById("datePicker");
  if (datePicker) {
    datePicker.value = "";
  }

  // Load all appointments on page load
  loadAppointments();

  // Attach search bar listener
  const searchBar = document.getElementById("searchBar");
  if (searchBar) {
    searchBar.addEventListener("input", handleSearchInput);
  }

  // Attach "Today" button listener
  const todayButton = document.getElementById("todayButton");
  if (todayButton) {
    todayButton.addEventListener("click", handleTodayButtonClick);
  }

  // Attach date picker listener
  if (datePicker) {
    datePicker.addEventListener("change", handleDatePickerChange);
  }
});

/**
 * Handle search input for filtering by patient name
 * Filters appointments based on the entered patient name
 */
function handleSearchInput(event) {
  const searchValue = event.target.value.trim();

  // Update patientName filter
  if (searchValue) {
    patientName = searchValue;
  } else {
    patientName = null;
  }

  // Reload appointments with updated filter
  loadAppointments();
}

/**
 * Handle "Today" button click
 * Resets to today's date and reloads appointments
 */
function handleTodayButtonClick() {
  selectedDate = getTodayDate();
  patientName = null;

  // Update UI
  const datePicker = document.getElementById("datePicker");
  if (datePicker) {
    datePicker.value = selectedDate;
  }

  const searchBar = document.getElementById("searchBar");
  if (searchBar) {
    searchBar.value = "";
  }

  // Reload appointments
  loadAppointments();
}

/**
 * Handle date picker change
 * Updates selected date and reloads appointments
 */
function handleDatePickerChange(event) {
  selectedDate = event.target.value;
  loadAppointments();
}

/**
 * Load and display appointments for the selected date
 * Fetches appointments from backend and renders them in the table
 */
async function loadAppointments() {
  try {
    // Step 1: Validate token
    if (!token) {
      window.location.href = "/login?role=doctor";
      return;
    }

    // Step 2: Clear existing table rows
    if (patientTableBody) {
      patientTableBody.innerHTML = "";
    }

    // Step 3: Fetch appointments from backend
    const appointments = await getAllAppointments(
      selectedDate,
      patientName || "null",
      token,
    );

    // Step 4: Check if appointments exist
    if (!appointments || appointments.length === 0) {
      showNoAppointmentsMessage();
      return;
    }

    // Step 5: Render each appointment as a table row
    appointments.forEach((appointment) => {
      try {
        const patientRow = createDoctorAppointmentRow(appointment);
        if (patientTableBody && patientRow) patientTableBody.appendChild(patientRow);
      } catch (error) {
        console.error("Error creating patient row:", error, appointment);
      }
    });
  } catch (error) {
    console.error("Error loading appointments:", error);
    showErrorMessage("Error loading appointments. Please try again later.");
  }
}

/**
 * Show "no appointments" message in table
 */
function showNoAppointmentsMessage() {
  if (!patientTableBody) return;

  const row = document.createElement("tr");
  const cell = document.createElement("td");
  cell.colSpan = 6; // number of columns
  cell.style.textAlign = "center";
  cell.style.padding = "20px";
  cell.style.color = "#666";
  cell.textContent = selectedDate
    ? "No appointments found for the selected date."
    : "No appointments found.";

  row.appendChild(cell);
  patientTableBody.appendChild(row);
}

/**
 * Show error message in table
 * @param {String} message - Error message to display
 */
function showErrorMessage(message) {
  if (!patientTableBody) return;

  const row = document.createElement("tr");
  const cell = document.createElement("td");
  cell.colSpan = 6; // number of columns
  cell.style.textAlign = "center";
  cell.style.padding = "20px";
  cell.style.color = "red";
  cell.textContent = message;

  row.appendChild(cell);
  patientTableBody.appendChild(row);
}

/**
 * Export functions for use in other modules if needed
 */
export { loadAppointments, getTodayDate };

function createDoctorAppointmentRow(appointment) {
  const tr = document.createElement("tr");
  const patientName = appointment.patientName || appointment.patient?.name || "Unknown";
  const phone = appointment.patient?.phone || "N/A";
  const email = appointment.patient?.email || "N/A";
  const rawDateTime = appointment.appointmentTime || "";
  const { dateText, timeText } = formatDateTime(rawDateTime, appointment.appointmentDate, appointment.appointmentTimeOnly);
  const statusText = getStatusText(appointment.status);

  tr.innerHTML = `
    <td>${escapeHtml(patientName)}</td>
    <td>${escapeHtml(phone)}</td>
    <td>${escapeHtml(email)}</td>
    <td>${escapeHtml(dateText)}</td>
    <td>${escapeHtml(timeText)}</td>
    <td>${escapeHtml(statusText)}</td>
  `;

  return tr;
}

function formatDateTime(isoDateTime, fallbackDate, fallbackTime) {
  if (isoDateTime) {
    const date = new Date(isoDateTime);
    if (!Number.isNaN(date.getTime())) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      return { dateText: `${year}-${month}-${day}`, timeText: `${hours}:${minutes}` };
    }
  }

  return {
    dateText: fallbackDate ? String(fallbackDate) : "N/A",
    timeText: fallbackTime ? String(fallbackTime).slice(0, 5) : "N/A",
  };
}

function getStatusText(status) {
  if (Number(status) === 1) return "Completed";
  if (Number(status) === 2) return "Cancelled";
  return "Scheduled";
}

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}
