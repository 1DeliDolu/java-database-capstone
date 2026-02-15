/**
 * Role-Based Login Service
 * 
 * Handles authentication for different user roles (Admin, Doctor).
 * Manages login modal interactions and API communication.
 * 
 * Dependencies:
 * - openModal() from ../components/modals.js
 * - API_BASE_URL from ../config/config.js
 * - selectRole() from ../render.js
 */

// Import the openModal function to handle showing login popups/modals
// Note: Assumes openModal is available globally from modals.js
// import { openModal } from '../components/modals.js';

// Import the base API URL from the config file
import { API_BASE_URL } from '../config/config.js';

// Define constants for the admin and doctor login API endpoints
const ADMIN_API = API_BASE_URL + '/api/admin/login';
const DOCTOR_API = API_BASE_URL + '/api/doctor/login';

/**
 * Setup button event listeners
 * Use window.onload to ensure DOM elements are available after page load
 */
window.onload = function () {
  // Select and setup admin login button
  const adminBtn = document.getElementById('adminBtn');
  if (adminBtn) {
    adminBtn.addEventListener('click', () => {
      // Open admin login modal when admin button is clicked
      if (typeof openModal === 'function') {
        openModal('adminLogin');
      } else {
        console.warn('openModal function not found. Admin login modal may not work.');
      }
    });
  }

  // Select and setup doctor login button
  const doctorBtn = document.getElementById('doctorBtn');
  if (doctorBtn) {
    doctorBtn.addEventListener('click', () => {
      // Open doctor login modal when doctor button is clicked
      if (typeof openModal === 'function') {
        openModal('doctorLogin');
      } else {
        console.warn('openModal function not found. Doctor login modal may not work.');
      }
    });
  }
};

/**
 * Admin Login Handler
 * Triggered when admin submits their login credentials
 */
window.adminLoginHandler = async function () {
  try {
    // Step 1: Get the entered username and password from the input fields
    const usernameInput = document.getElementById('adminUsername') || document.querySelector('input[name="adminUsername"]');
    const passwordInput = document.getElementById('adminPassword') || document.querySelector('input[name="adminPassword"]');

    if (!usernameInput || !passwordInput) {
      alert('Login form fields not found. Please check the modal.');
      return;
    }

    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();

    // Validate inputs
    if (!username || !password) {
      alert('Please enter both username and password.');
      return;
    }

    // Step 2: Create an admin object with these credentials
    const admin = {
      username: username,
      password: password
    };

    // Step 3: Use fetch() to send a POST request to the ADMIN_API endpoint
    const response = await fetch(ADMIN_API, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(admin)
    });

    // Step 4: Check if the response is successful
    if (response.ok) {
      // Parse the JSON response to get the token
      const data = await response.json();
      const token = data.token || data.access_token;

      if (!token) {
        alert('No authentication token received. Please try again.');
        return;
      }

      // Store the token in localStorage
      localStorage.setItem('token', token);
      localStorage.setItem('userRole', 'admin');

      // Close modal if it exists
      const modal = document.getElementById('modal');
      if (modal) {
        modal.style.display = 'none';
      }

      // Clear form inputs
      usernameInput.value = '';
      passwordInput.value = '';

      // Call selectRole('admin') to proceed with admin-specific behavior
      if (typeof selectRole === 'function') {
        selectRole('admin');
      } else {
        // Fallback: redirect manually
        window.location.href = '/templates/admin/adminDashboard.html';
      }
    } else {
      // Step 5: If login fails or credentials are invalid
      const errorData = await response.json().catch(() => ({}));
      const errorMsg = errorData.message || 'Invalid credentials! Please try again.';
      alert(errorMsg);
    }
  } catch (error) {
    // Step 6: Wrap everything in a try-catch to handle network or server errors
    console.error('Admin login error:', error);
    alert('An error occurred during login. Please check your connection and try again.');
  }
};

/**
 * Doctor Login Handler
 * Triggered when doctor submits their login credentials
 */
window.doctorLoginHandler = async function () {
  try {
    // Step 1: Get the entered email and password from the input fields
    const emailInput = document.getElementById('doctorEmail') || document.querySelector('input[name="doctorEmail"]');
    const passwordInput = document.getElementById('doctorPassword') || document.querySelector('input[name="doctorPassword"]');

    if (!emailInput || !passwordInput) {
      alert('Login form fields not found. Please check the modal.');
      return;
    }

    const email = emailInput.value.trim();
    const password = passwordInput.value.trim();

    // Validate inputs
    if (!email || !password) {
      alert('Please enter both email and password.');
      return;
    }

    // Step 2: Create a doctor object with these credentials
    const doctor = {
      email: email,
      password: password
    };

    // Step 3: Use fetch() to send a POST request to the DOCTOR_API endpoint
    const response = await fetch(DOCTOR_API, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(doctor)
    });

    // Step 4: Check if login is successful
    if (response.ok) {
      // Parse the JSON response to get the token
      const data = await response.json();
      const token = data.token || data.access_token;

      if (!token) {
        alert('No authentication token received. Please try again.');
        return;
      }

      // Store the token in localStorage
      localStorage.setItem('token', token);
      localStorage.setItem('userRole', 'doctor');

      // Close modal if it exists
      const modal = document.getElementById('modal');
      if (modal) {
        modal.style.display = 'none';
      }

      // Clear form inputs
      emailInput.value = '';
      passwordInput.value = '';

      // Call selectRole('doctor') to proceed with doctor-specific behavior
      if (typeof selectRole === 'function') {
        selectRole('doctor');
      } else {
        // Fallback: redirect manually
        window.location.href = '/templates/doctor/doctorDashboard.html';
      }
    } else {
      // If login fails or credentials are invalid
      const errorData = await response.json().catch(() => ({}));
      const errorMsg = errorData.message || 'Invalid credentials! Please try again.';
      alert(errorMsg);
    }
  } catch (error) {
    // Handle network or server errors
    console.error('Doctor login error:', error);
    alert('An error occurred during login. Please check your connection and try again.');
  }
};

  Step 5: If login fails:
    - Show an alert for invalid credentials

  Step 6: Wrap in a try-catch block to handle errors gracefully
    - Log the error to the console
    - Show a generic error message
*/
