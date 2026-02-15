/**
 * Modals Component
 * 
 * Handles modal dialog display and form submission:
 * - Dynamic modal content generation based on modal type
 * - Admin login, Doctor login, Patient login/signup forms
 * - Add doctor form with availability checkboxes
 * - Modal event handling (open, close, submit)
 * 
 * Modal Types:
 * - 'addDoctor': Form for adding new doctor (admin only)
 * - 'patientLogin': Login form for patient users
 * - 'patientSignup': Registration form for new patients
 * - 'adminLogin': Login form for admin users
 * - 'doctorLogin': Login form for doctor users
 * - 'bookAppointment': Form for booking appointment (patient)
 * 
 * Dependencies:
 * - HTML modal structure with id "modal" and "modal-body"
 * - Global functions: adminAddDoctor, signupPatient, loginPatient, etc.
 * - CSS styling for .input-field, .dashboard-btn, etc.
 */

/**
 * Open modal dialog with specified form type
 * Dynamically generates modal content based on type
 * 
 * @param {string} type - Modal type (addDoctor, patientLogin, patientSignup, adminLogin, doctorLogin, bookAppointment)
 */
export function openModal(type) {
  try {
    let modalContent = '';

    // Step 1: Generate modal content based on type
    switch (type) {
      case 'addDoctor':
        modalContent = generateAddDoctorForm();
        break;
      case 'patientLogin':
        modalContent = generatePatientLoginForm();
        break;
      case 'patientSignup':
        modalContent = generatePatientSignupForm();
        break;
      case 'adminLogin':
        modalContent = generateAdminLoginForm();
        break;
      case 'doctorLogin':
        modalContent = generateDoctorLoginForm();
        break;
      case 'bookAppointment':
        modalContent = generateBookAppointmentForm();
        break;
      default:
        console.error('Unknown modal type:', type);
        return;
    }

    // Step 2: Insert content into modal
    const modalBody = document.getElementById('modal-body');
    if (!modalBody) {
      console.error('Modal body element not found');
      return;
    }
    modalBody.innerHTML = modalContent;

    // Step 3: Display modal
    const modal = document.getElementById('modal');
    if (modal) {
      modal.style.display = 'block';
    }

    // Step 4: Attach close button listener
    attachCloseModal();

    // Step 5: Attach form submission listeners based on type
    attachFormListeners(type);

  } catch (error) {
    console.error('Error opening modal:', error);
    alert('✗ An error occurred. Please try again.');
  }
}

/**
 * Generate HTML for "Add Doctor" form
 * Used by admin to add new doctors to the system
 * 
 * @returns {string} HTML form content
 */
function generateAddDoctorForm() {
  return `
    <h2>Add Doctor</h2>
    <input type="text" id="doctorName" placeholder="Doctor Name" class="input-field" required>
    <select id="specialization" class="input-field select-dropdown" required>
      <option value="">Select Specialization</option>
      <option value="Cardiologist">Cardiologist</option>
      <option value="Dermatologist">Dermatologist</option>
      <option value="Neurologist">Neurologist</option>
      <option value="Pediatrician">Pediatrician</option>
      <option value="Orthopedic">Orthopedic</option>
      <option value="Gynecologist">Gynecologist</option>
      <option value="Psychiatrist">Psychiatrist</option>
      <option value="Dentist">Dentist</option>
      <option value="Ophthalmologist">Ophthalmologist</option>
      <option value="ENT">ENT Specialist</option>
      <option value="Urologist">Urologist</option>
      <option value="Oncologist">Oncologist</option>
      <option value="Gastroenterologist">Gastroenterologist</option>
      <option value="General">General Physician</option>
    </select>
    <input type="email" id="doctorEmail" placeholder="Email" class="input-field" required>
    <input type="password" id="doctorPassword" placeholder="Password" class="input-field" required>
    <input type="tel" id="doctorPhone" placeholder="Mobile No." class="input-field" required>
    <div class="availability-container">
      <label class="availabilityLabel">Select Availability:</label>
      <div class="checkbox-group">
        <label><input type="checkbox" name="availability" value="09:00-10:00"> 9:00 AM - 10:00 AM</label>
        <label><input type="checkbox" name="availability" value="10:00-11:00"> 10:00 AM - 11:00 AM</label>
        <label><input type="checkbox" name="availability" value="11:00-12:00"> 11:00 AM - 12:00 PM</label>
        <label><input type="checkbox" name="availability" value="12:00-13:00"> 12:00 PM - 1:00 PM</label>
        <label><input type="checkbox" name="availability" value="14:00-15:00"> 2:00 PM - 3:00 PM</label>
        <label><input type="checkbox" name="availability" value="15:00-16:00"> 3:00 PM - 4:00 PM</label>
        <label><input type="checkbox" name="availability" value="16:00-17:00"> 4:00 PM - 5:00 PM</label>
      </div>
    </div>
    <button class="dashboard-btn" id="saveDoctorBtn">Save Doctor</button>
  `;
}

/**
 * Generate HTML for "Patient Login" form
 * 
 * @returns {string} HTML form content
 */
function generatePatientLoginForm() {
  return `
    <h2>Patient Login</h2>
    <input type="email" id="loginEmail" placeholder="Email" class="input-field" required>
    <input type="password" id="loginPassword" placeholder="Password" class="input-field" required>
    <button class="dashboard-btn" id="loginBtn">Login</button>
  `;
}

/**
 * Generate HTML for "Patient Signup" form
 * 
 * @returns {string} HTML form content
 */
function generatePatientSignupForm() {
  return `
    <h2>Patient Signup</h2>
    <input type="text" id="patientName" placeholder="Full Name" class="input-field" required>
    <input type="email" id="patientEmail" placeholder="Email" class="input-field" required>
    <input type="password" id="patientPassword" placeholder="Password" class="input-field" required>
    <input type="tel" id="patientPhone" placeholder="Phone Number" class="input-field">
    <input type="text" id="patientAddress" placeholder="Address" class="input-field">
    <button class="dashboard-btn" id="signupBtn">Create Account</button>
  `;
}

/**
 * Generate HTML for "Admin Login" form
 * 
 * @returns {string} HTML form content
 */
function generateAdminLoginForm() {
  return `
    <h2>Admin Login</h2>
    <input type="text" id="adminUsername" name="username" placeholder="Username" class="input-field" required>
    <input type="password" id="adminPassword" name="password" placeholder="Password" class="input-field" required>
    <button class="dashboard-btn" id="adminLoginBtn">Login</button>
  `;
}

/**
 * Generate HTML for "Doctor Login" form
 * 
 * @returns {string} HTML form content
 */
function generateDoctorLoginForm() {
  return `
    <h2>Doctor Login</h2>
    <input type="email" id="doctorLoginEmail" placeholder="Email" class="input-field" required>
    <input type="password" id="doctorLoginPassword" placeholder="Password" class="input-field" required>
    <button class="dashboard-btn" id="doctorLoginBtn">Login</button>
  `;
}

/**
 * Generate HTML for "Book Appointment" form
 * 
 * @returns {string} HTML form content
 */
function generateBookAppointmentForm() {
  return `
    <h2>Book Appointment</h2>
    <input type="date" id="appointmentDate" placeholder="Date" class="input-field" required>
    <select id="appointmentTime" class="input-field select-dropdown" required>
      <option value="">Select Time Slot</option>
      <option value="09:00">9:00 AM</option>
      <option value="10:00">10:00 AM</option>
      <option value="11:00">11:00 AM</option>
      <option value="12:00">12:00 PM</option>
      <option value="14:00">2:00 PM</option>
      <option value="15:00">3:00 PM</option>
      <option value="16:00">4:00 PM</option>
      <option value="17:00">5:00 PM</option>
    </select>
    <textarea id="appointmentNotes" placeholder="Additional Notes (optional)" class="input-field" style="height: 80px;"></textarea>
    <button class="dashboard-btn" id="bookAppointmentBtn">Confirm Booking</button>
  `;
}

/**
 * Attach close button event listener to modal
 * Allows user to close modal by clicking close button
 */
function attachCloseModal() {
  const closeBtn = document.getElementById('closeModal');
  if (closeBtn) {
    closeBtn.onclick = () => {
      const modal = document.getElementById('modal');
      if (modal) {
        modal.style.display = 'none';
      }
    };
  }

  // Also close when clicking outside the modal box
  window.onclick = (event) => {
    const modal = document.getElementById('modal');
    const modalContent = document.querySelector('.modal-content');
    if (modal && event.target === modal) {
      modal.style.display = 'none';
    }
  };
}

/**
 * Attach form submission listeners based on modal type
 * Routes form submission to appropriate handler function
 * 
 * @param {string} type - Modal type
 */
function attachFormListeners(type) {
  try {
    switch (type) {
      case 'addDoctor':
        const saveDoctorBtn = document.getElementById('saveDoctorBtn');
        if (saveDoctorBtn && typeof window.adminAddDoctor === 'function') {
          saveDoctorBtn.addEventListener('click', window.adminAddDoctor);
        }
        break;

      case 'patientSignup':
        const signupBtn = document.getElementById('signupBtn');
        if (signupBtn && typeof window.signupPatient === 'function') {
          signupBtn.addEventListener('click', window.signupPatient);
        }
        break;

      case 'patientLogin':
        const loginBtn = document.getElementById('loginBtn');
        if (loginBtn && typeof window.loginPatient === 'function') {
          loginBtn.addEventListener('click', window.loginPatient);
        }
        break;

      case 'adminLogin':
        const adminLoginBtn = document.getElementById('adminLoginBtn');
        if (adminLoginBtn && typeof window.adminLoginHandler === 'function') {
          adminLoginBtn.addEventListener('click', window.adminLoginHandler);
        }
        break;

      case 'doctorLogin':
        const doctorLoginBtn = document.getElementById('doctorLoginBtn');
        if (doctorLoginBtn && typeof window.doctorLoginHandler === 'function') {
          doctorLoginBtn.addEventListener('click', window.doctorLoginHandler);
        }
        break;

      case 'bookAppointment':
        const bookBtn = document.getElementById('bookAppointmentBtn');
        if (bookBtn && typeof window.bookAppointmentHandler === 'function') {
          bookBtn.addEventListener('click', window.bookAppointmentHandler);
        }
        break;

      default:
        console.warn('No form listeners attached for type:', type);
    }
  } catch (error) {
    console.error('Error attaching form listeners:', error);
  }
}

/**
 * Close modal programmatically
 * Useful when form submission is successful
 */
export function closeModal() {
  const modal = document.getElementById('modal');
  if (modal) {
    modal.style.display = 'none';
  }
}

/**
 * Clear all form fields in the modal
 * Useful after successful form submission
 */
export function clearModalForm() {
  const modalBody = document.getElementById('modal-body');
  if (modalBody) {
    const inputs = modalBody.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
      if (input.type === 'checkbox' || input.type === 'radio') {
        input.checked = false;
      } else {
        input.value = '';
      }
    });
  }
}

export default {
  openModal,
  closeModal,
  clearModalForm
};
