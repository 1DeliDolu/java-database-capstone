/**
 * Header Component
 *
 * This module provides dynamic header rendering based on user role and session state.
 * It handles authentication checks, role-based navigation, and logout functionality.
 */

// Render header based on current page and user session
function renderHeader() {
  const headerDiv = document.getElementById("header");

  if (!headerDiv) return;

  // Check if on homepage - don't show user-specific header
  if (window.location.pathname.endsWith("/")) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
    headerDiv.innerHTML = `
      <header class="header">
        <div class="logo-section">
          <img src="./assets/images/logo/logo.png" alt="Hospital CMS Logo" class="logo-img">
          <span class="logo-title">Hospital CMS</span>
        </div>
      </header>`;
    return;
  }

  // Retrieve user role and token from localStorage
  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  // Initialize header with logo
  let headerContent = `
    <header class="header">
      <div class="logo-section">
        <img src="../assets/images/logo/logo.png" alt="Hospital CMS Logo" class="logo-img">
        <span class="logo-title">Hospital CMS</span>
      </div>
      <nav>`;

  // Check for invalid session: role exists but no token
  if (
    (role === "loggedPatient" || role === "admin" || role === "doctor") &&
    !token
  ) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
    alert("Session expired or invalid login. Please log in again.");
    window.location.href = "/";
    return;
  }

  // Add role-specific header content
  if (role === "admin") {
    headerContent += `
      <button id="homeBtn" class="adminBtn">Dashboard</button>
      <button id="addDocBtn" class="adminBtn">Add Doctor</button>
      <a href="#" onclick="logout(); return false;">Logout</a>`;
  } else if (role === "doctor") {
    headerContent += `
      <button id="homeBtn" class="adminBtn">Dashboard</button>
      <a href="#" onclick="logout(); return false;">Logout</a>`;
  } else if (role === "patient") {
    headerContent += `
      <button id="homeBtn" class="adminBtn">Dashboard</button>
      <button id="patientLogin" class="adminBtn">Login</button>
      <button id="patientSignup" class="adminBtn">Sign Up</button>`;
  } else if (role === "loggedPatient") {
    headerContent += `
      <button id="homeBtn" class="adminBtn">Dashboard</button>
      <button id="appointmentsBtn" class="adminBtn">Appointments</button>
      <a href="#" onclick="logoutPatient(); return false;">Logout</a>`;
  }

  // Close navigation and header tags
  headerContent += `
      </nav>
    </header>`;

  // Insert header into DOM
  headerDiv.innerHTML = headerContent;

  // Attach event listeners to dynamically created buttons
  attachHeaderButtonListeners();
}

/**
 * Attach event listeners to header buttons
 * Handles login, signup, home navigation, and other header actions
 */
function attachHeaderButtonListeners() {
  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  // Login button for patients
  const loginBtn = document.getElementById("patientLogin");
  if (loginBtn) {
    loginBtn.addEventListener("click", (e) => {
      e.preventDefault();
      window.location.href = "/login?role=patient";
    });
  }

  // Signup button for patients
  const signupBtn = document.getElementById("patientSignup");
  if (signupBtn) {
    signupBtn.addEventListener("click", (e) => {
      e.preventDefault();
      window.location.href = "/register";
    });
  }

  // Add Doctor button for admin
  const addDocBtn = document.getElementById("addDocBtn");
  if (addDocBtn) {
    addDocBtn.addEventListener("click", (e) => {
      e.preventDefault();
      openModal("addDoctor");
    });
  }

  // Home button for logged-in users
  const homeBtn = document.getElementById("homeBtn");
  if (homeBtn) {
    homeBtn.addEventListener("click", (e) => {
      e.preventDefault();
      if (role === "doctor") {
        window.location.href = token ? `/doctorDashboard/${token}` : "/login?role=doctor";
      } else if (role === "patient") {
        window.location.href = "/pages/patientDashboard.html";
      } else if (role === "loggedPatient") {
        window.location.href = "/pages/loggedPatientDashboard.html";
      } else if (role === "admin") {
        window.location.href = token ? `/adminDashboard/${token}` : "/login?role=admin";
      }
    });
  }

  // Appointments button for logged-in patients
  const appointmentsBtn = document.getElementById("appointmentsBtn");
  if (appointmentsBtn) {
    appointmentsBtn.addEventListener("click", (e) => {
      e.preventDefault();
      window.location.href = "/pages/patientAppointments.html";
    });
  }
}

/**
 * Logout function - clears session and redirects to homepage
 * Used for admin and doctor roles
 */
function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("userRole");
  window.location.href = "/";
}

/**
 * Logout function for patients - clears token and resets role to patient
 * Allows patient to login/signup again without removing role
 */
function logoutPatient() {
  localStorage.removeItem("token");
  localStorage.setItem("userRole", "patient");
  window.location.href = "/";
}

// Call renderHeader when DOM is loaded
document.addEventListener("DOMContentLoaded", renderHeader);
