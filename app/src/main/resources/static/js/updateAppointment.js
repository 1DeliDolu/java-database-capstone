import { API_BASE_URL } from "./config/config.js";
import { updateAppointment } from "./services/appointmentRecordService.js";
import { getDoctors } from "./services/doctorServices.js";

document.addEventListener("DOMContentLoaded", initializePage);

async function initializePage() {
  const token = localStorage.getItem("token");
  const params = new URLSearchParams(window.location.search);

  const appointmentId = params.get("appointmentId");
  const defaultDoctorId = params.get("doctorId");
  const defaultDate = params.get("appointmentDate");
  const defaultTime = params.get("appointmentTime");

  if (!token || !appointmentId) {
    alert("Missing session data, redirecting to appointments page.");
    window.location.href = "/pages/patientAppointments.html";
    return;
  }

  try {
    const patient = await fetchLoggedPatient(token);
    const doctors = await getDoctors();

    if (!patient?.id) {
      throw new Error("Patient profile not found");
    }

    if (!Array.isArray(doctors) || !doctors.length) {
      throw new Error("Doctor list not found");
    }

    const patientNameInput = document.getElementById("patientName");
    const doctorSelect = document.getElementById("doctorName");
    const dateInput = document.getElementById("appointmentDate");
    const timeSelect = document.getElementById("appointmentTime");
    const today = formatLocalDate(new Date());

    patientNameInput.value = patient.name || "You";
    populateDoctorOptions(doctorSelect, doctors);
    dateInput.min = today;
    timeSelect.disabled = true;

    if (defaultDoctorId) {
      doctorSelect.value = String(defaultDoctorId);
    }
    if (!doctorSelect.value && doctors[0]?.id) {
      doctorSelect.value = String(doctors[0].id);
    }

    if (defaultDate && defaultDate >= today) {
      dateInput.value = defaultDate;
    } else {
      dateInput.value = today;
    }

    await loadAvailability(timeSelect, doctorSelect.value, dateInput.value, token, defaultTime, doctors);

    doctorSelect.addEventListener("change", async () => {
      await loadAvailability(timeSelect, doctorSelect.value, dateInput.value, token, null, doctors);
    });

    dateInput.addEventListener("change", async () => {
      await loadAvailability(timeSelect, doctorSelect.value, dateInput.value, token, null, doctors);
    });

    document.getElementById("updateAppointmentForm").addEventListener("submit", async (e) => {
      e.preventDefault();

      const selectedDoctorId = Number(doctorSelect.value);
      const date = dateInput.value;
      const selectedSlot = timeSelect.value;

      if (!selectedDoctorId || !date || !selectedSlot) {
        alert("Please select doctor, date and time.");
        return;
      }

      const startTime = selectedSlot.includes("-")
        ? selectedSlot.split("-")[0]
        : selectedSlot;
      const selectedDateTime = new Date(`${date}T${startTime}:00`);
      if (Number.isNaN(selectedDateTime.getTime()) || selectedDateTime <= new Date()) {
        alert("Please select a future date and time.");
        return;
      }

      const updatedAppointment = {
        id: Number(appointmentId),
        doctor: { id: selectedDoctorId },
        patient: { id: Number(patient.id) },
        appointmentTime: `${date}T${startTime}:00`,
        status: 0,
      };

      const updateResponse = await updateAppointment(updatedAppointment, token);
      if (updateResponse.success) {
        alert("Appointment updated successfully!");
        window.location.href = "/pages/patientAppointments.html";
      } else {
        alert(`Failed to update appointment: ${updateResponse.message}`);
      }
    });
  } catch (error) {
    console.error("Error initializing update appointment page:", error);
    alert("Failed to load appointment update data.");
    window.location.href = "/pages/patientAppointments.html";
  }
}

async function fetchLoggedPatient(token) {
  const response = await fetch(`${API_BASE_URL}/patient/${encodeURIComponent(token)}`, {
    method: "GET",
    headers: { "Content-Type": "application/json" },
  });

  const data = await response.json();
  if (!response.ok || !data.patient) {
    throw new Error(data.message || "Failed to fetch patient");
  }

  return data.patient;
}

function populateDoctorOptions(selectEl, doctors) {
  selectEl.innerHTML = '<option value="">Select doctor</option>';
  doctors.forEach((doctor) => {
    const option = document.createElement("option");
    option.value = String(doctor.id);
    option.textContent = `${doctor.name} - ${doctor.specialty}`;
    selectEl.appendChild(option);
  });
}

async function loadAvailability(timeSelect, doctorId, selectedDate, token, preferredTime, doctors) {
  timeSelect.innerHTML = '<option value="">Select time</option>';
  timeSelect.disabled = true;

  if (!doctorId || !selectedDate) return;

  try {
    const response = await fetch(
      `${API_BASE_URL}/doctor/availability/patient/${encodeURIComponent(doctorId)}/${encodeURIComponent(selectedDate)}/${encodeURIComponent(token)}`,
      { method: "GET", headers: { "Content-Type": "application/json" } }
    );

    const data = await response.json();
    let slots = Array.isArray(data.availability) ? data.availability : [];
    if (!slots.length) {
      const doctor = (doctors || []).find((d) => String(d.id) === String(doctorId));
      slots = Array.isArray(doctor?.availableTimes) ? doctor.availableTimes : [];
    }

    slots.forEach((slot) => {
      const option = document.createElement("option");
      option.value = slot;
      option.textContent = slot;
      timeSelect.appendChild(option);
    });

    if (!slots.length) {
      timeSelect.innerHTML = '<option value="">No available time</option>';
      return;
    }

    const preferred = normalizeTime(preferredTime);
    const matched = slots.find((slot) => normalizeTime(slot) === preferred);
    timeSelect.value = matched || slots[0];
    timeSelect.disabled = false;
  } catch (error) {
    console.error("Failed to load doctor availability:", error);
    timeSelect.innerHTML = '<option value="">No available time</option>';
  }
}

function normalizeTime(rawTime) {
  if (!rawTime) return "";
  const clean = String(rawTime).trim();
  if (clean.includes("-")) return clean.split("-")[0].slice(0, 5);
  return clean.slice(0, 5);
}

function formatLocalDate(dateObj) {
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, "0");
  const day = String(dateObj.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}
