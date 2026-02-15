import { API_BASE_URL } from "./config/config.js";
import { createPatientRecordRow } from "./components/patientRecordRow.js";

const tableBody = document.getElementById("patientTableBody");
const token = localStorage.getItem("token");
const urlParams = new URLSearchParams(window.location.search);
const patientId = urlParams.get("id");
const doctorId = urlParams.get("doctorId");

document.addEventListener("DOMContentLoaded", initializePage);

async function initializePage() {
  try {
    if (!token || !patientId) {
      throw new Error("Missing token or patient id");
    }

    const response = await fetch(
      `${API_BASE_URL}/patient/${encodeURIComponent(patientId)}/${encodeURIComponent(token)}`,
      { method: "GET", headers: { "Content-Type": "application/json" } }
    );

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.message || "Failed to load patient appointments");
    }

    let appointments = data.appointments || [];
    if (doctorId) {
      appointments = appointments.filter((app) => String(app.doctorId) === String(doctorId));
    }

    renderAppointments(appointments);
  } catch (error) {
    console.error("Error loading appointments:", error);
    tableBody.innerHTML = `<tr><td colspan="4" style="text-align:center;">Failed to load patient record.</td></tr>`;
  }
}

function renderAppointments(appointments) {
  tableBody.innerHTML = "";

  if (!appointments.length) {
    tableBody.innerHTML = `<tr><td colspan="4" style="text-align:center;">No Appointments Found</td></tr>`;
    return;
  }

  appointments.forEach((appointment) => {
    const row = createPatientRecordRow(appointment);
    tableBody.appendChild(row);
  });
}
