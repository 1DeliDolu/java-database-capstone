import { API_BASE_URL } from './config/config.js';

const API = `${API_BASE_URL}/api/admin/manage`;
const DOCTOR_API = `${API_BASE_URL}/api/doctor`;

let cache = {
  doctors: [],
  patients: [],
  appointments: [],
};

function getToken() {
  return localStorage.getItem('token');
}

function requireAdminSession() {
  const token = getToken();
  const role = localStorage.getItem('userRole');
  if (!token || role !== 'admin') {
    window.location.href = '/login?role=admin';
    return null;
  }
  return token;
}

document.addEventListener('DOMContentLoaded', () => {
  const token = requireAdminSession();
  if (!token) return;

  const refreshBtn = document.getElementById('refreshBtn');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', () => loadOverview());
  }

  const searchBar = document.getElementById('searchBar');
  if (searchBar) {
    searchBar.addEventListener('input', applySearchFilter);
  }

  bindSectionTabs();
  bindTableActions();
  loadOverview();
});

async function loadOverview() {
  const token = requireAdminSession();
  if (!token) return;

  try {
    const response = await fetch(`${API}/overview/${token}`);
    const data = await response.json();

    if (!response.ok) {
      alert(data.message || 'Failed to load admin overview');
      return;
    }

    cache = {
      doctors: data.doctors || [],
      patients: data.patients || [],
      appointments: data.appointments || [],
    };

    renderAll(cache);
  } catch (error) {
    console.error(error);
    alert('Failed to load data.');
  }
}

function renderAll(data) {
  renderDoctors(data.doctors);
  renderPatients(data.patients);
  renderAppointments(data.appointments);
  applySearchFilter();
}

function bindSectionTabs() {
  const tabs = document.querySelectorAll('.tab-btn');
  if (!tabs.length) return;

  tabs.forEach((tab) => {
    tab.addEventListener('click', () => {
      const target = tab.getAttribute('data-target');
      activateSection(target || 'doctors');
      applySearchFilter();
    });
  });

  activateSection('doctors');
}

function activateSection(sectionName) {
  const sections = document.querySelectorAll('.table-section[data-section]');
  const tabs = document.querySelectorAll('.tab-btn');

  sections.forEach((section) => {
    const name = section.getAttribute('data-section');
    section.classList.toggle('hidden', name !== sectionName);
  });

  tabs.forEach((tab) => {
    tab.classList.toggle('active', tab.getAttribute('data-target') === sectionName);
  });
}

function renderDoctors(doctors) {
  const tbody = document.getElementById('doctorsBody');
  if (!tbody) return;

  tbody.innerHTML = doctors.map((d) => {
    const availability = formatAvailabilityForDisplay(d.availableTimes);
    return `<tr>
      <td>${d.id ?? ''}</td>
      <td>${escapeHtml(d.name)}</td>
      <td>${escapeHtml(d.specialty)}</td>
      <td>${escapeHtml(d.email)}</td>
      <td>${escapeHtml(d.phone)}</td>
      <td>${escapeHtml(availability)}</td>
      <td>
        <button class="action-btn view-btn" data-kind="doctor" data-action="view" data-id="${d.id}">View</button>
        <button class="action-btn edit-btn" data-kind="doctor" data-action="edit" data-id="${d.id}">Edit</button>
        <button class="action-btn delete-btn" data-kind="doctor" data-action="delete" data-id="${d.id}">Delete</button>
      </td>
    </tr>`;
  }).join('');
}

function renderPatients(patients) {
  const tbody = document.getElementById('patientsBody');
  if (!tbody) return;

  tbody.innerHTML = patients.map((p) => `<tr>
      <td>${p.id ?? ''}</td>
      <td>${escapeHtml(p.name)}</td>
      <td>${escapeHtml(p.email)}</td>
      <td>${escapeHtml(p.phone)}</td>
      <td>${escapeHtml(p.address)}</td>
      <td>
        <button class="action-btn view-btn" data-kind="patient" data-action="view" data-id="${p.id}">View</button>
        <button class="action-btn edit-btn" data-kind="patient" data-action="edit" data-id="${p.id}">Edit</button>
        <button class="action-btn delete-btn" data-kind="patient" data-action="delete" data-id="${p.id}">Delete</button>
      </td>
    </tr>`).join('');
}

function renderAppointments(appointments) {
  const tbody = document.getElementById('appointmentsBody');
  if (!tbody) return;

  tbody.innerHTML = appointments.map((a) => `<tr>
      <td>${a.id ?? ''}</td>
      <td>${escapeHtml(a.doctorName)} (#${a.doctorId ?? ''})</td>
      <td>${escapeHtml(a.patientName)} (#${a.patientId ?? ''})</td>
      <td>${formatDateTime(a.appointmentTime)}</td>
      <td>${escapeHtml(a.statusName)} (${a.status})</td>
      <td>
        <button class="action-btn view-btn" data-kind="appointment" data-action="view" data-id="${a.id}">View</button>
        <button class="action-btn edit-btn" data-kind="appointment" data-action="edit" data-id="${a.id}">Edit</button>
        <button class="action-btn delete-btn" data-kind="appointment" data-action="delete" data-id="${a.id}">Delete</button>
      </td>
    </tr>`).join('');
}

function bindTableActions() {
  const content = document.getElementById('content');
  if (!content) return;

  content.addEventListener('click', async (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) return;

    const btn = target.closest('button[data-kind][data-action][data-id]');
    if (!btn) return;

    const kind = btn.getAttribute('data-kind');
    const action = btn.getAttribute('data-action');
    const id = Number(btn.getAttribute('data-id'));

    if (Number.isNaN(id)) return;

    if (action === 'view') {
      handleView(kind, id);
    } else if (action === 'edit') {
      await handleEdit(kind, id);
    } else if (action === 'delete') {
      await handleDelete(kind, id);
    }
  });
}

function handleView(kind, id) {
  const item = findItem(kind, id);
  if (!item) return;

  const modal = document.getElementById('modal');
  const modalBody = document.getElementById('modal-body');
  const closeBtn = document.getElementById('closeModal');
  if (!modal || !modalBody) return;

  modalBody.innerHTML = buildViewForm(kind, item);
  modal.style.display = 'block';

  if (closeBtn) {
    closeBtn.onclick = () => {
      modal.style.display = 'none';
    };
  }

  modal.onclick = (event) => {
    if (event.target === modal) {
      modal.style.display = 'none';
    }
  };
}

async function handleEdit(kind, id) {
  const token = requireAdminSession();
  if (!token) return;

  const item = findItem(kind, id);
  if (!item) return;

  openEditModal(kind, item, token);
}

async function handleDelete(kind, id) {
  const token = requireAdminSession();
  if (!token) return;

  if (!confirm(`Delete ${kind} #${id}?`)) return;

  try {
    if (kind === 'doctor') {
      await callJson(`${API}/doctor/${id}/${token}`, 'DELETE');
    }
    if (kind === 'patient') {
      await callJson(`${API}/patient/${id}/${token}`, 'DELETE');
    }
    if (kind === 'appointment') {
      await callJson(`${API}/appointment/${id}/${token}`, 'DELETE');
    }

    await loadOverview();
  } catch (error) {
    console.error(error);
    alert(error.message || 'Delete failed');
  }
}

function findItem(kind, id) {
  if (kind === 'doctor') return cache.doctors.find((d) => d.id === id);
  if (kind === 'patient') return cache.patients.find((p) => p.id === id);
  if (kind === 'appointment') return cache.appointments.find((a) => a.id === id);
  return null;
}

function applySearchFilter() {
  const input = document.getElementById('searchBar');
  const q = (input?.value || '').trim().toLowerCase();

  const activeSection = document.querySelector('.table-section[data-section]:not(.hidden)');
  const rows = activeSection ? activeSection.querySelectorAll('tbody tr') : document.querySelectorAll('tbody tr');
  rows.forEach((row) => {
    const text = row.textContent?.toLowerCase() || '';
    row.style.display = q && !text.includes(q) ? 'none' : '';
  });
}

function formatDateTime(value) {
  if (!value) return '';
  try {
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return String(value);
    return d.toLocaleString();
  } catch {
    return String(value);
  }
}

function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

async function callJson(url, method, body) {
  const response = await fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  let data = {};
  try {
    data = await response.json();
  } catch {
    data = {};
  }

  if (!response.ok) {
    throw new Error(data.message || `Request failed (${response.status})`);
  }

  return data;
}

window.adminAddDoctor = async function () {
  const token = requireAdminSession();
  if (!token) return;

  try {
    const selectedName = document.getElementById('doctorName')?.value?.trim() || '';
    const customName = document.getElementById('doctorNameCustom')?.value?.trim() || '';
    const name = selectedName === '__custom__' ? customName : selectedName;
    const specialty = document.getElementById('specialization')?.value?.trim() || '';
    const email = document.getElementById('doctorEmail')?.value?.trim() || '';
    const password = document.getElementById('doctorPassword')?.value?.trim() || '';
    const phone = document.getElementById('doctorPhone')?.value?.trim() || '';

    const availability = Array.from(document.querySelectorAll('input[name="availability"]:checked'))
      .map((input) => input.value);

    if (!name || !specialty || !email || !password || !phone) {
      alert('Please fill all required doctor fields.');
      return;
    }

    const normalizedPhone = phone.replace(/\D/g, '');
    if (normalizedPhone.length !== 10) {
      alert('Phone must be exactly 10 digits.');
      return;
    }

    if (availability.length === 0) {
      alert('Please select at least one availability slot.');
      return;
    }

    const payload = {
      name,
      specialty,
      email,
      password,
      phone: normalizedPhone,
      availableTimes: normalizeAvailabilityList(availability),
    };

    const response = await fetch(`${DOCTOR_API}/${token}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    const data = await response.json();
    if (!response.ok) {
      alert(data.message || 'Failed to add doctor');
      return;
    }

    alert('Doctor added successfully');
    await loadOverview();
    window.renderAddDoctorList();
    clearAddDoctorForm();
  } catch (error) {
    console.error(error);
    alert('Failed to add doctor');
  }
};

window.renderAddDoctorList = function () {
  const tbody = document.getElementById('addDoctorListBody');
  if (!tbody) return;

  const doctors = Array.isArray(cache.doctors) ? cache.doctors : [];
  if (!doctors.length) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No doctors found</td></tr>';
    return;
  }

  tbody.innerHTML = doctors.map((d) => `
    <tr>
      <td>${d.id ?? ''}</td>
      <td>${escapeHtml(d.name)}</td>
      <td>${escapeHtml(d.specialty)}</td>
      <td>${escapeHtml(d.email)}</td>
      <td>${escapeHtml(d.phone)}</td>
    </tr>
  `).join('');
};

window.populateDoctorNameDropdown = function () {
  const select = document.getElementById('doctorName');
  if (!select) return;

  const names = Array.from(
    new Set((cache.doctors || []).map((d) => (d.name || '').trim()).filter(Boolean))
  ).sort((a, b) => a.localeCompare(b));

  select.innerHTML = '<option value="">Select Doctor Name</option>';
  names.forEach((name) => {
    const option = document.createElement('option');
    option.value = name;
    option.textContent = name;
    select.appendChild(option);
  });

  const customOption = document.createElement('option');
  customOption.value = '__custom__';
  customOption.textContent = 'Other (New Name)';
  select.appendChild(customOption);
};

function clearAddDoctorForm() {
  const ids = ['doctorName', 'doctorNameCustom', 'specialization', 'doctorEmail', 'doctorPassword', 'doctorPhone'];
  ids.forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  const customInput = document.getElementById('doctorNameCustom');
  if (customInput) customInput.style.display = 'none';
  document.querySelectorAll('input[name="availability"]').forEach((input) => {
    input.checked = false;
  });
}

function normalizeAvailabilityList(values) {
  if (!Array.isArray(values)) return [];
  return values
    .map((v) => String(v || '').trim())
    .filter(Boolean)
    .sort((a, b) => a.localeCompare(b));
}

function parseAvailabilityInput(input) {
  return normalizeAvailabilityList(String(input || '').split(',').map((v) => v.trim()));
}

function formatAvailabilityForDisplay(values) {
  return normalizeAvailabilityList(values).join(', ');
}

function buildViewForm(kind, item) {
  if (kind === 'doctor') {
    return `
      <h2>Doctor Details</h2>
      <div class="view-form-grid">
        ${viewField('ID', item.id)}
        ${viewField('Name', item.name)}
        ${viewField('Specialty', item.specialty)}
        ${viewField('Email', item.email)}
        ${viewField('Phone', item.phone)}
        ${viewField('Availability', formatAvailabilityForDisplay(item.availableTimes))}
      </div>
    `;
  }

  if (kind === 'patient') {
    return `
      <h2>Patient Details</h2>
      <div class="view-form-grid">
        ${viewField('ID', item.id)}
        ${viewField('Name', item.name)}
        ${viewField('Email', item.email)}
        ${viewField('Phone', item.phone)}
        ${viewField('Address', item.address)}
      </div>
    `;
  }

  return `
    <h2>Appointment Details</h2>
    <div class="view-form-grid">
      ${viewField('ID', item.id)}
      ${viewField('Doctor', `${item.doctorName || ''} (#${item.doctorId || ''})`)}
      ${viewField('Patient', `${item.patientName || ''} (#${item.patientId || ''})`)}
      ${viewField('Date Time', formatDateTime(item.appointmentTime))}
      ${viewField('Status', `${item.statusName || ''} (${item.status ?? ''})`)}
    </div>
  `;
}

function viewField(label, value) {
  return `
    <div class="view-field">
      <label>${escapeHtml(label)}</label>
      <input class="input-field" type="text" value="${escapeHtml(value ?? '')}" readonly>
    </div>
  `;
}

function openEditModal(kind, item, token) {
  const modal = document.getElementById('modal');
  const modalBody = document.getElementById('modal-body');
  const closeBtn = document.getElementById('closeModal');
  if (!modal || !modalBody) return;

  modalBody.innerHTML = buildEditForm(kind, item);
  modal.style.display = 'block';

  if (closeBtn) {
    closeBtn.onclick = () => {
      modal.style.display = 'none';
    };
  }

  modal.onclick = (event) => {
    if (event.target === modal) {
      modal.style.display = 'none';
    }
  };

  const saveBtn = document.getElementById('editSaveBtn');
  if (!saveBtn) return;

  saveBtn.addEventListener('click', async () => {
    try {
      if (kind === 'doctor') {
        const name = valueOf('editName');
        const specialty = valueOf('editSpecialty');
        const email = valueOf('editEmail');
        const phone = valueOf('editPhone');
        const availability = parseAvailabilityInput(valueOf('editAvailability'));
        const password = valueOf('editPassword');

        if (!name || !specialty || !email || !phone) {
          alert('Please fill required doctor fields.');
          return;
        }

        const payload = {
          id: item.id,
          name,
          specialty,
          email,
          phone,
          availableTimes: availability,
        };
        if (password) payload.password = password;
        await callJson(`${API}/doctor/${token}`, 'PUT', payload);
      }

      if (kind === 'patient') {
        const name = valueOf('editName');
        const email = valueOf('editEmail');
        const phone = valueOf('editPhone');
        const address = valueOf('editAddress');
        const password = valueOf('editPassword');

        if (!name || !email || !phone || !address) {
          alert('Please fill required patient fields.');
          return;
        }

        const payload = {
          id: item.id,
          name,
          email,
          phone,
          address,
        };
        if (password) payload.password = password;
        await callJson(`${API}/patient/${token}`, 'PUT', payload);
      }

      if (kind === 'appointment') {
        const doctorId = Number(valueOf('editDoctorId'));
        const patientId = Number(valueOf('editPatientId'));
        const appointmentTime = valueOf('editAppointmentTime');
        const status = Number(valueOf('editStatus'));

        if (!doctorId || !patientId || !appointmentTime || Number.isNaN(status)) {
          alert('Please fill required appointment fields.');
          return;
        }

        await callJson(`${API}/appointment/${token}`, 'PUT', {
          id: item.id,
          doctorId,
          patientId,
          appointmentTime: `${appointmentTime}:00`,
          status,
        });
      }

      modal.style.display = 'none';
      await loadOverview();
    } catch (error) {
      console.error(error);
      alert(error.message || 'Update failed');
    }
  });
}

function buildEditForm(kind, item) {
  if (kind === 'doctor') {
    return `
      <h2>Edit Doctor</h2>
      <div class="view-form-grid">
        ${editField('ID', 'editId', item.id, true)}
        ${editField('Name', 'editName', item.name)}
        ${editField('Specialty', 'editSpecialty', item.specialty)}
        ${editField('Email', 'editEmail', item.email)}
        ${editField('Phone', 'editPhone', item.phone)}
        ${editField('Availability', 'editAvailability', formatAvailabilityForDisplay(item.availableTimes))}
        ${editField('New Password', 'editPassword', '', false, 'password')}
      </div>
      <button id="editSaveBtn" class="dashboard-btn">Update</button>
    `;
  }

  if (kind === 'patient') {
    return `
      <h2>Edit Patient</h2>
      <div class="view-form-grid">
        ${editField('ID', 'editId', item.id, true)}
        ${editField('Name', 'editName', item.name)}
        ${editField('Email', 'editEmail', item.email)}
        ${editField('Phone', 'editPhone', item.phone)}
        ${editField('Address', 'editAddress', item.address)}
        ${editField('New Password', 'editPassword', '', false, 'password')}
      </div>
      <button id="editSaveBtn" class="dashboard-btn">Update</button>
    `;
  }

  const current = (item.appointmentTime || '').toString().replace(' ', 'T').slice(0, 16);
  return `
    <h2>Edit Appointment</h2>
    <div class="view-form-grid">
      ${editField('ID', 'editId', item.id, true)}
      ${editField('Doctor ID', 'editDoctorId', item.doctorId)}
      ${editField('Patient ID', 'editPatientId', item.patientId)}
      ${editField('Date Time', 'editAppointmentTime', current, false, 'datetime-local')}
      ${editField('Status (0/1/2)', 'editStatus', item.status)}
    </div>
    <button id="editSaveBtn" class="dashboard-btn">Update</button>
  `;
}

function editField(label, id, value, readonly = false, type = 'text') {
  return `
    <div class="view-field">
      <label for="${id}">${escapeHtml(label)}</label>
      <input id="${id}" class="input-field" type="${type}" value="${escapeHtml(value ?? '')}" ${readonly ? 'readonly' : ''}>
    </div>
  `;
}

function valueOf(id) {
  const el = document.getElementById(id);
  return (el?.value || '').trim();
}
