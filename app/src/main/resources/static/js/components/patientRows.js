/**
 * Patient Rows Component
 *
 * Generates table row HTML for displaying patient appointment information
 * Used in doctor dashboard to show appointment details in table format
 *
 * Features:
 * - Displays patient ID, name, phone, email
 * - Provides action buttons for viewing patient records and adding prescriptions
 * - Handles navigation to patient record and prescription pages
 *
 * @param {Object} appointment - Appointment object containing appointment details
 * @param {Object} patientData - Patient information object with id, name, phone, email
 * @returns {HTMLTableRowElement} Table row element with patient appointment data
 */

/**
 * Create a table row element for displaying patient appointment information
 *
 * @param {Object} appointment - Appointment object with appointmentId and other details
 * @param {Object} patientData - Object containing:
 *   - id: Patient ID
 *   - name: Patient name
 *   - phone: Patient phone number
 *   - email: Patient email address
 * @returns {HTMLTableRowElement} Constructed table row with event handlers
 */
export function createPatientRow(appointment, patientData) {
  try {
    // Step 1: Validate inputs
    if (!appointment || !patientData) {
      console.error(
        "createPatientRow: Missing appointment or patientData",
        appointment,
        patientData,
      );
      return null;
    }

    // Step 2: Create table row element
    const tr = document.createElement("tr");
    tr.className = "patient-appointment-row";

    // Step 3: Build row content with patient and appointment information
    const appointmentId = appointment.id || appointment.appointmentId || "N/A";
    const patientId = patientData.id || "N/A";
    const patientName = patientData.name || "Unknown Patient";
    const patientPhone = patientData.phone || "N/A";
    const patientEmail = patientData.email || "N/A";

    tr.innerHTML = `
      <td class="patient-id">${patientId}</td>
      <td class="patient-name">${patientName}</td>
      <td class="patient-phone">${patientPhone}</td>
      <td class="patient-email">${patientEmail}</td>
      <td class="patient-actions">
        <button class="view-record-btn" title="View Patient Record" data-patient-id="${patientId}">
          <img src="/assets/images/addPrescriptionIcon/addPrescription.png" alt="View Record" class="action-icon">
        </button>
        <button class="add-prescription-btn" title="Add Prescription" data-appointment-id="${appointmentId}" data-patient-name="${patientName}">
          <i class="icon-prescription"></i> Prescription
        </button>
      </td>
    `;

    // Step 4: Attach event listeners for patient interactions

    // View patient record - Click on patient ID or view button
    const viewRecordBtn = tr.querySelector(".view-record-btn");
    if (viewRecordBtn) {
      viewRecordBtn.addEventListener("click", () => {
        window.location.href = `/pages/patientRecord.html?id=${patientId}`;
      });
    }

    const patientIdCell = tr.querySelector(".patient-id");
    if (patientIdCell) {
      patientIdCell.style.cursor = "pointer";
      patientIdCell.addEventListener("click", () => {
        window.location.href = `/pages/patientRecord.html?id=${patientId}`;
      });
    }

    // Add prescription - Click on add prescription button
    const prescriptionBtn = tr.querySelector(".add-prescription-btn");
    if (prescriptionBtn) {
      prescriptionBtn.addEventListener("click", () => {
        window.location.href = `/pages/addPrescription.html?appointmentId=${appointmentId}&patientName=${encodeURIComponent(patientName)}`;
      });
    }

    return tr;
  } catch (error) {
    console.error("Error creating patient row:", error);
    return null;
  }
}

/**
 * Alternative: Create appointment row with time and status information
 * Used when displaying appointment details including date and time
 *
 * @param {Object} appointment - Full appointment object
 * @param {Object} patientData - Patient information
 * @returns {HTMLTableRowElement} Table row with appointment details
 */
export function createAppointmentRow(appointment, patientData) {
  try {
    // Similar to createPatientRow but includes appointment time and status
    const tr = document.createElement("tr");
    tr.className = "appointment-row";

    const appointmentId = appointment.id || "N/A";
    const appointmentDate = appointment.date || "N/A";
    const appointmentTime = appointment.time || "N/A";
    const appointmentStatus = appointment.status || "Scheduled";

    tr.innerHTML = `
      <td>${appointmentDate}</td>
      <td>${appointmentTime}</td>
      <td>${patientData.name || "Unknown"}</td>
      <td>${appointmentStatus}</td>
      <td>
        <button class="edit-btn" data-id="${appointmentId}">Edit</button>
        <button class="cancel-btn" data-id="${appointmentId}">Cancel</button>
      </td>
    `;

    // Attach event listeners
    const editBtn = tr.querySelector(".edit-btn");
    if (editBtn) {
      editBtn.addEventListener("click", () => {
        window.location.href = `/pages/updateAppointment.html?id=${appointmentId}`;
      });
    }

    const cancelBtn = tr.querySelector(".cancel-btn");
    if (cancelBtn) {
      cancelBtn.addEventListener("click", () => {
        if (confirm("Are you sure you want to cancel this appointment?")) {
          // Call cancel appointment API here
          console.log("Canceling appointment:", appointmentId);
        }
      });
    }

    return tr;
  } catch (error) {
    console.error("Error creating appointment row:", error);
    return null;
  }
}

export default {
  createPatientRow,
  createAppointmentRow,
};
