import { API_BASE_URL } from './config/config.js';
import { bookAppointment } from './services/appointmentRecordService.js';

const token = localStorage.getItem('token');
const tableBody = document.getElementById('patientTableBody');

const searchBar = document.getElementById('searchBar');
const appointmentFilter = document.getElementById('appointmentFilter');

const addAppointmentBtn = document.getElementById('addAppointmentBtn');
const addAppointmentCard = document.getElementById('addAppointmentCard');
const cancelAppointmentBtn = document.getElementById('cancelAppointmentBtn');
const saveAppointmentBtn = document.getElementById('saveAppointmentBtn');

const doctorSelect = document.getElementById('doctorSelect');
const appointmentDateSelect = document.getElementById('appointmentDate');
const appointmentTimeSelect = document.getElementById('appointmentTime');

let patient = null;
let allDoctors = [];
let allAppointments = [];
const pageParams = new URLSearchParams(window.location.search);
let availabilityByDate = new Map();
let isStaticFallbackMode = false;

document.addEventListener('DOMContentLoaded', initializePage);

async function initializePage() {
  try {
    if (!token) {
      alert('Session expired. Please login again.');
      window.location.href = '/login?role=patient';
      return;
    }

    patient = await fetchPatient();
    if (!patient?.id) {
      throw new Error('Patient profile not found');
    }

    await loadDoctors();
    await loadAppointments();
    bindEvents();
  } catch (error) {
    console.error(error);
    alert('Error loading appointments. Please try again later.');
  }
}

function bindEvents() {
  searchBar?.addEventListener('input', renderAppointments);
  appointmentFilter?.addEventListener('change', renderAppointments);

  addAppointmentBtn?.addEventListener('click', () => {
    addAppointmentCard.classList.toggle('show');
  });

  cancelAppointmentBtn?.addEventListener('click', () => {
    clearAddForm();
    addAppointmentCard.classList.remove('show');
  });

  doctorSelect?.addEventListener('change', handleDoctorChange);
  appointmentDateSelect?.addEventListener('change', fillTimeOptions);

  saveAppointmentBtn?.addEventListener('click', async () => {
    await saveAppointment();
  });
}

async function fetchPatient() {
  const response = await fetch(`${API_BASE_URL}/patient/${encodeURIComponent(token)}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  });

  const data = await response.json();
  if (!response.ok || !data.patient) {
    throw new Error(data.message || 'Failed to fetch patient');
  }

  return data.patient;
}

async function loadDoctors() {
  const response = await fetch(`${API_BASE_URL}/doctor`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || 'Failed to load doctors');
  }

  allDoctors = data.doctors || [];

  doctorSelect.innerHTML = '<option value="">Select doctor</option>';
  allDoctors.forEach((doctor) => {
    const option = document.createElement('option');
    option.value = String(doctor.id);
    option.textContent = `${doctor.name} - ${doctor.specialty}`;
    doctorSelect.appendChild(option);
  });

  const preselectedDoctorId = pageParams.get('doctorId');
  if (preselectedDoctorId && doctorSelect) {
    doctorSelect.value = String(preselectedDoctorId);
    if (addAppointmentCard) addAppointmentCard.classList.add('show');
    await handleDoctorChange();
  }
}

async function handleDoctorChange() {
  await loadAvailableDates();
  await fillTimeOptions();
}

async function loadAvailableDates() {
  const doctorId = Number(doctorSelect.value);
  const doctor = allDoctors.find((d) => Number(d.id) === doctorId);

  availabilityByDate = new Map();
  isStaticFallbackMode = false;
  appointmentDateSelect.innerHTML = '<option value="">Select date</option>';
  appointmentTimeSelect.innerHTML = '<option value="">Select time</option>';

  if (!doctorId) return;

  const dates = getNextDates(60);
  const results = await Promise.all(
    dates.map(async (dateStr) => {
      const slots = await fetchAvailability(doctorId, dateStr);
      return { dateStr, slots };
    })
  );

  const availableResults = results.filter((r) => Array.isArray(r.slots) && r.slots.length > 0);
  if (!availableResults.length) {
    const staticSlots = normalizeSlots(Array.isArray(doctor?.availableTimes) ? doctor.availableTimes : []);
    if (!staticSlots.length) {
      appointmentDateSelect.innerHTML = '<option value="">No available dates</option>';
      return;
    }

    isStaticFallbackMode = true;
    dates.forEach((dateStr) => {
      availabilityByDate.set(dateStr, staticSlots);
      const option = document.createElement('option');
      option.value = dateStr;
      option.textContent = formatDateLabel(dateStr);
      appointmentDateSelect.appendChild(option);
    });
  } else {
    availableResults.forEach(({ dateStr, slots }) => {
      availabilityByDate.set(dateStr, slots || []);
      const option = document.createElement('option');
      option.value = dateStr;
      option.textContent = formatDateLabel(dateStr);
      appointmentDateSelect.appendChild(option);
    });
  }

  const preferredDate = pageParams.get('appointmentDate');
  const hasPreferredDate = preferredDate && availabilityByDate.has(preferredDate);
  const firstDate = appointmentDateSelect.options[1]?.value || '';
  appointmentDateSelect.value = hasPreferredDate ? preferredDate : firstDate;
}

async function fillTimeOptions() {
  const doctorId = Number(doctorSelect.value);
  const selectedDate = appointmentDateSelect?.value;

  appointmentTimeSelect.innerHTML = '<option value="">Select time</option>';
  if (!doctorId || !selectedDate) return;

  try {
    let slots = availabilityByDate.get(selectedDate);
    if (!Array.isArray(slots)) {
      slots = await fetchAvailability(doctorId, selectedDate);
      availabilityByDate.set(selectedDate, slots);
    }

    if (!slots.length) return;

    slots.forEach((slot) => appendTimeOption(slot));
  } catch (error) {
    console.error('Failed to load availability:', error);
  }
}

async function loadAppointments() {
  const response = await fetch(`${API_BASE_URL}/patient/${encodeURIComponent(patient.id)}/${encodeURIComponent(token)}`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || 'Failed to load appointments');
  }

  allAppointments = data.appointments || [];
  renderAppointments();
}

function renderAppointments() {
  const search = (searchBar?.value || '').trim().toLowerCase();
  const filter = appointmentFilter?.value || 'allAppointments';

  let list = [...allAppointments];

  if (filter === 'future') {
    list = list.filter((a) => Number(a.status) === 0);
  } else if (filter === 'past') {
    list = list.filter((a) => Number(a.status) !== 0);
  }

  if (search) {
    list = list.filter((a) => (a.doctorName || '').toLowerCase().includes(search));
  }

  tableBody.innerHTML = '';

  if (!list.length) {
    tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No Appointments Found</td></tr>`;
    return;
  }

  list.forEach((appointment) => {
    const tr = document.createElement('tr');

    tr.innerHTML = `
      <td><img src="../assets/images/logo/logo.png" alt="Appointment" class="table-avatar"></td>
      <td>${escapeHtml(appointment.patientName || patient.name || 'You')}</td>
      <td>${escapeHtml(appointment.doctorName || '')}</td>
      <td>${escapeHtml(String(appointment.appointmentDate || ''))}</td>
      <td>${escapeHtml(String(appointment.appointmentTimeOnly || ''))}</td>
      <td class="actions-cell">${Number(appointment.status) === 0 ? `<img src="../assets/images/edit/edit.png" alt="Edit" class="prescription-btn action-icon" data-id="${appointment.id}">` : '-'}</td>
    `;

    if (Number(appointment.status) === 0) {
      const editBtn = tr.querySelector('.prescription-btn');
      editBtn?.addEventListener('click', () => redirectToUpdatePage(appointment));
    }

    tableBody.appendChild(tr);
  });
}

async function saveAppointment() {
  const doctorId = Number(doctorSelect.value);
  const date = appointmentDateSelect.value;
  const slot = appointmentTimeSelect.value;

  if (!doctorId || !date || !slot) {
    if (appointmentDateSelect?.options?.length <= 1 || appointmentDateSelect?.value === '') {
      alert('No available appointment date found for this doctor.');
      return;
    }
    if (appointmentTimeSelect?.options?.length <= 1 || appointmentTimeSelect?.value === '') {
      alert('No available time found for the selected date.');
      return;
    }
    alert('Please select doctor, date and time.');
    return;
  }

  // Re-check live availability before booking.
  const liveSlots = await fetchAvailability(doctorId, date);
  if (!isStaticFallbackMode && !isSlotInList(slot, liveSlots)) {
    alert('Selected time is no longer available. Please choose another date/time.');
    await loadAvailableDates();
    await fillTimeOptions();
    return;
  }

  const startTime = String(slot).split(/[-–]/)[0].trim();
  const selectedDateTime = new Date(`${date}T${startTime}:00`);
  if (Number.isNaN(selectedDateTime.getTime()) || selectedDateTime <= new Date()) {
    alert('Please select a future date and time.');
    return;
  }

  const appointment = {
    doctor: { id: doctorId },
    patient: { id: Number(patient.id) },
    appointmentTime: `${date}T${startTime}:00`,
    status: 0,
  };

  const { success, message } = await bookAppointment(appointment, token);
  if (!success) {
    alert(`Failed to add appointment: ${message}`);
    return;
  }

  alert('Appointment added successfully.');
  clearAddForm();
  addAppointmentCard.classList.remove('show');
  await loadAppointments();
}

function clearAddForm() {
  if (doctorSelect) doctorSelect.value = '';
  if (appointmentDateSelect) appointmentDateSelect.innerHTML = '<option value="">Select date</option>';
  if (appointmentTimeSelect) appointmentTimeSelect.innerHTML = '<option value="">Select time</option>';
  availabilityByDate = new Map();
  isStaticFallbackMode = false;
}

function appendTimeOption(slot) {
  const normalizedSlot = sanitizeSlot(slot);
  if (!normalizedSlot) return;
  const option = document.createElement('option');
  option.value = normalizedSlot;
  option.textContent = normalizedSlot;
  appointmentTimeSelect.appendChild(option);
}

function redirectToUpdatePage(appointment) {
  const queryString = new URLSearchParams({
    appointmentId: appointment.id,
    patientId: appointment.patientId,
    patientName: appointment.patientName || 'You',
    doctorName: appointment.doctorName,
    doctorId: appointment.doctorId,
    appointmentDate: appointment.appointmentDate,
    appointmentTime: appointment.appointmentTimeOnly,
  }).toString();

  window.location.href = `/pages/updateAppointment.html?${queryString}`;
}

function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

async function fetchAvailability(doctorId, dateStr) {
  try {
    const response = await fetch(
      `${API_BASE_URL}/doctor/availability/patient/${encodeURIComponent(doctorId)}/${encodeURIComponent(dateStr)}/${encodeURIComponent(token)}`,
      {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
      }
    );
    if (response.status === 401) {
      alert('Session expired. Please login again.');
      window.location.href = '/login?role=patient';
      return [];
    }
    const data = await response.json();
    if (!response.ok) return [];
    return Array.isArray(data.availability) ? data.availability : [];
  } catch (error) {
    console.error('Availability fetch failed:', error);
    return [];
  }
}

function getNextDates(dayCount) {
  const dates = [];
  const base = new Date();
  for (let i = 0; i < dayCount; i += 1) {
    const current = new Date(base);
    current.setDate(base.getDate() + i);
    dates.push(formatLocalDate(current));
  }
  return dates;
}

function formatLocalDate(dateObj) {
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, '0');
  const day = String(dateObj.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function formatDateLabel(dateStr) {
  const d = new Date(`${dateStr}T00:00:00`);
  if (Number.isNaN(d.getTime())) return dateStr;
  return d.toLocaleDateString('en-GB');
}

function isSlotInList(selectedSlot, slots) {
  if (!selectedSlot || !Array.isArray(slots) || !slots.length) return false;
  const selectedStart = sanitizeSlot(selectedSlot).split(/[-–]/)[0].trim().slice(0, 5);
  return slots.some((slot) => sanitizeSlot(slot).split(/[-–]/)[0].trim().slice(0, 5) === selectedStart);
}

function sanitizeSlot(value) {
  return String(value ?? '')
    .replace(/[–—]/g, '-')
    .replace(/["']/g, '')
    .replace(/\s+/g, ' ')
    .trim();
}

function normalizeSlots(rawSlots) {
  if (!Array.isArray(rawSlots)) return [];
  return rawSlots
    .flatMap((slot) => String(slot || '').split(/[,;]/))
    .map((slot) => sanitizeSlot(slot))
    .filter((slot) => slot.length > 0);
}
