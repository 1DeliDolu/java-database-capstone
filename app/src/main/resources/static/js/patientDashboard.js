/**
 * Patient Dashboard Script
 *
 * Manages the patient's view of available doctors:
 * - Displays all doctors in card format
 * - Implements search and filtering by name, time, specialty
 * - Handles patient signup and login
 * - Integrates with booking functionality
 *
 * Dependencies:
 * - getDoctors(), filterDoctors() from ./services/doctorServices.js
 * - createDoctorCard() from ./components/doctorCard.js
 * - patientSignup(), patientLogin() from ./services/patientServices.js
 * - openModal() from ./components/modals.js
 * - selectRole() from ./render.js (global)
 */

// Import required modules
import { getDoctors, filterDoctors } from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";
import { patientSignup, patientLogin } from "./services/patientServices.js";
import { openModal } from "./components/modals.js";

/**
 * Initialize dashboard when DOM is loaded
 * Consolidates all DOMContentLoaded listeners
 */
document.addEventListener("DOMContentLoaded", () => {
  // Load all doctors on page load
  loadDoctorCards();

  // Setup signup button
  const signupBtn = document.getElementById("patientSignup");
  if (signupBtn) {
    signupBtn.addEventListener("click", () => {
      openModal("patientSignup");
    });
  }

  // Setup login button
  const loginBtn = document.getElementById("patientLogin");
  if (loginBtn) {
    loginBtn.addEventListener("click", () => {
      openModal("patientLogin");
    });
  }

  // Setup search and filter listeners
  const searchBar = document.getElementById("searchBar");
  if (searchBar) {
    searchBar.addEventListener("input", filterDoctorsOnChange);
  }

  const filterTime = document.getElementById("filterTime");
  if (filterTime) {
    filterTime.addEventListener("change", filterDoctorsOnChange);
  }

  const filterSpecialty = document.getElementById("filterSpecialty");
  if (filterSpecialty) {
    filterSpecialty.addEventListener("change", filterDoctorsOnChange);
  }
});

/**
 * Load all doctors and display them as cards
 * Called on page load and after filters are cleared
 */
async function loadDoctorCards() {
  try {
    // Step 1: Fetch all doctors from backend
    const doctors = await getDoctors();

    // Step 2: Render doctor cards
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Failed to load doctors:", error);
    const contentDiv = document.getElementById("content");
    if (contentDiv) {
      contentDiv.innerHTML =
        '<p style=\"color: red; text-align: center;\">Failed to load doctors. Please try again.</p>';
    }
  }
}

/**
 * Render a list of doctor cards to the page
 * Utility function used by loadDoctorCards and filterDoctorsOnChange
 *
 * @param {Array} doctors - Array of doctor objects to render
 */
function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");

  if (!contentDiv) {
    console.error("Content div not found");
    return;
  }

  // Step 1: Clear existing content
  contentDiv.innerHTML = "";

  // Step 2: Check if doctors array is empty
  if (!doctors || doctors.length === 0) {
    contentDiv.innerHTML =
      '<p style=\"color: #666; text-align: center; padding: 20px;\">No doctors found.</p>';
    return;
  }

  // Step 3: Create and append doctor cards
  doctors.forEach((doctor) => {
    try {
      const card = createDoctorCard(doctor);
      contentDiv.appendChild(card);
    } catch (error) {
      console.error("Error creating doctor card:", error, doctor);
    }
  });
}

/**
 * Filter doctors based on search bar and filter dropdowns
 * Called whenever user types in search bar or changes filters
 */
async function filterDoctorsOnChange() {
  try {
    // Step 1: Get filter values
    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("filterTime");
    const filterSpecialty = document.getElementById("filterSpecialty");

    const name = searchBar ? searchBar.value.trim() : "";
    const time = filterTime ? filterTime.value : "";
    const specialty = filterSpecialty ? filterSpecialty.value : "";

    // Step 2: Normalize empty values to null
    const nameFilter = name.length > 0 ? name : null;
    const timeFilter = time.length > 0 ? time : null;
    const specialtyFilter = specialty.length > 0 ? specialty : null;

    // Step 3: Call filter service
    const response = await filterDoctors(
      nameFilter,
      timeFilter,
      specialtyFilter,
    );

    // Step 4: Extract doctors array from response
    const doctors = Array.isArray(response) ? response : response.doctors || [];

    // Step 5: Render results or show message
    if (doctors.length > 0) {
      renderDoctorCards(doctors);
    } else {
      const contentDiv = document.getElementById("content");
      if (contentDiv) {
        contentDiv.innerHTML =
          '<p style=\"color: #666; text-align: center; padding: 20px;\">No doctors found with the given filters.</p>';
      }
    }
  } catch (error) {
    console.error("Error filtering doctors:", error);
    alert("An error occurred while filtering doctors. Please try again.");
  }
}

/**
 * Handle patient signup form submission
 * Called when patient submits the signup form in the modal
 */
window.signupPatient = async function () {
  try {
    // Step 1: Get form inputs
    const nameInput =
      document.getElementById("patientName") || document.getElementById("name");
    const emailInput =
      document.getElementById("patientEmail") ||
      document.getElementById("email");
    const passwordInput =
      document.getElementById("patientPassword") ||
      document.getElementById("password");
    const phoneInput =
      document.getElementById("patientPhone") ||
      document.getElementById("phone");
    const addressInput =
      document.getElementById("patientAddress") ||
      document.getElementById("address");

    // Step 2: Validate required fields
    if (!nameInput || !nameInput.value.trim()) {
      alert("Please enter your name");
      return;
    }

    if (!emailInput || !emailInput.value.trim()) {
      alert("Please enter your email");
      return;
    }

    if (!passwordInput || !passwordInput.value.trim()) {
      alert("Please enter a password");
      return;
    }

    // Step 3: Build signup data
    const data = {
      name: nameInput.value.trim(),
      email: emailInput.value.trim(),
      password: passwordInput.value.trim(),
      phone: phoneInput ? phoneInput.value.trim() : "",
      address: addressInput ? addressInput.value.trim() : "",
    };

    // Step 4: Send signup request
    const { success, message } = await patientSignup(data);

    // Step 5: Handle response
    if (success) {
      alert("✓ " + message);

      // Close modal
      const modal = document.getElementById("modal");
      if (modal) {
        modal.style.display = "none";
      }

      // Clear form
      if (nameInput) nameInput.value = "";
      if (emailInput) emailInput.value = "";
      if (passwordInput) passwordInput.value = "";
      if (phoneInput) phoneInput.value = "";
      if (addressInput) addressInput.value = "";

      // Reload page
      window.location.reload();
    } else {
      alert("✗ " + message);
    }
  } catch (error) {
    console.error("Signup failed:", error);
    alert("✗ An error occurred during signup. Please try again.");
  }
};

/**
 * Handle patient login form submission
 * Called when patient submits the login form in the modal
 */
window.loginPatient = async function () {
  try {
    // Step 1: Get form inputs
    const emailInput =
      document.getElementById("loginEmail") ||
      document.querySelector('input[name=\"loginEmail\"]');
    const passwordInput =
      document.getElementById("loginPassword") ||
      document.querySelector('input[name=\"loginPassword\"]');

    // Step 2: Validate inputs
    if (!emailInput || !emailInput.value.trim()) {
      alert("Please enter your email");
      return;
    }

    if (!passwordInput || !passwordInput.value.trim()) {
      alert("Please enter your password");
      return;
    }

    // Step 3: Build login data
    const data = {
      email: emailInput.value.trim(),
      password: passwordInput.value.trim(),
    };

    // Step 4: Send login request
    const response = await patientLogin(data);

    // Step 5: Handle response
    if (response.ok) {
      const result = await response.json();

      // Store authentication data
      localStorage.setItem("token", result.token || result.access_token);
      localStorage.setItem("userRole", "loggedPatient");

      // Close modal
      const modal = document.getElementById("modal");
      if (modal) {
        modal.style.display = "none";
      }

      // Call global selectRole if available
      if (typeof selectRole === "function") {
        selectRole("loggedPatient");
      } else {
        // Fallback redirect
        window.location.href = "/pages/patientDashboard.html";
      }
    } else {
      alert("✗ Invalid credentials! Please try again.");
    }
  } catch (error) {
    console.error("Login error:", error);
    alert("✗ An error occurred during login. Please try again.");
  }
};

/**
 * Export functions for use in other modules if needed
 */
export { loadDoctorCards, filterDoctorsOnChange, renderDoctorCards };
