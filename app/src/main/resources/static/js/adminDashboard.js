/**
 * Admin Dashboard Script
 * 
 * Manages doctor operations on the admin dashboard:
 * - Displays all doctors in card format
 * - Implements search and filtering by name, time, specialty
 * - Handles adding new doctors via modal
 * - Handles deleting doctors
 * 
 * Dependencies:
 * - openModal() from ./components/modals.js
 * - getDoctors(), filterDoctors(), saveDoctor() from ./services/doctorServices.js
 * - createDoctorCard() from ./components/doctorCard.js
 */

// Import required modules
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';

// Note: openModal is assumed to be available globally from modals.js

/**
 * Initialize admin dashboard when page loads
 */
document.addEventListener('DOMContentLoaded', function () {
  // Load all doctors on page load
  loadDoctorCards();

  // Attach event listener to "Add Doctor" button
  const addDocBtn = document.getElementById('addDocBtn');
  if (addDocBtn) {
    addDocBtn.addEventListener('click', () => {
      if (typeof openModal === 'function') {
        openModal('addDoctor');
      } else {
        console.warn('openModal function not found');
      }
    });
  }

  // Attach event listeners to search and filter inputs
  const searchBar = document.getElementById('searchBar');
  if (searchBar) {
    searchBar.addEventListener('input', filterDoctorsOnChange);
  }

  const filterTime = document.getElementById('filterTime');
  if (filterTime) {
    filterTime.addEventListener('change', filterDoctorsOnChange);
  }

  const filterSpecialty = document.getElementById('filterSpecialty');
  if (filterSpecialty) {
    filterSpecialty.addEventListener('change', filterDoctorsOnChange);
  }
});

/**
 * Load all doctors and display them as cards
 * Called on page load and after successful doctor addition
 */
async function loadDoctorCards() {
  try {
    // Step 1: Fetch all doctors from backend
    const doctors = await getDoctors();

    // Step 2: Render the doctor cards
    renderDoctorCards(doctors);
  } catch (error) {
    console.error('Error loading doctor cards:', error);
    const contentDiv = document.getElementById('content');
    if (contentDiv) {
      contentDiv.innerHTML = '<p style="color: red;">Failed to load doctors. Please try again.</p>';
    }
  }
}

/**
 * Filter doctors based on search and filter inputs
 * Called whenever user changes search bar or filter dropdowns
 */
async function filterDoctorsOnChange() {
  try {
    // Step 1: Get current filter values
    const searchBar = document.getElementById('searchBar');
    const filterTime = document.getElementById('filterTime');
    const filterSpecialty = document.getElementById('filterSpecialty');

    const name = searchBar ? searchBar.value.trim() : '';
    const time = filterTime ? filterTime.value : '';
    const specialty = filterSpecialty ? filterSpecialty.value : '';

    // Step 2: Normalize empty values
    const nameFilter = name || null;
    const timeFilter = time || null;
    const specialtyFilter = specialty || null;

    // Step 3: Call filter service
    const filteredDoctors = await filterDoctors(nameFilter, timeFilter, specialtyFilter);

    // Step 4: Render filtered results or show message
    if (filteredDoctors && filteredDoctors.length > 0) {
      renderDoctorCards(filteredDoctors);
    } else {
      // Show "no results" message
      const contentDiv = document.getElementById('content');
      if (contentDiv) {
        contentDiv.innerHTML = '<p style="text-align: center; color: #666; padding: 20px;">No doctors found with the given filters.</p>';
      }
    }
  } catch (error) {
    console.error('Error filtering doctors:', error);
    alert('An error occurred while filtering doctors. Please try again.');
  }
}

/**
 * Render a list of doctor cards to the page
 * Utility function used by loadDoctorCards and filterDoctorsOnChange
 * 
 * @param {Array} doctors - Array of doctor objects to render
 */
function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById('content');

  if (!contentDiv) {
    console.error('Content div not found');
    return;
  }

  // Step 1: Clear existing content
  contentDiv.innerHTML = '';

  // Step 2: Check if doctors array is empty
  if (!doctors || doctors.length === 0) {
    contentDiv.innerHTML = '<p style="text-align: center; color: #666; padding: 20px;">No doctors available.</p>';
    return;
  }

  // Step 3: Create and append doctor cards
  doctors.forEach(doctor => {
    try {
      const doctorCard = createDoctorCard(doctor);
      contentDiv.appendChild(doctorCard);
    } catch (error) {
      console.error('Error creating doctor card:', error, doctor);
    }
  });
}

/**
 * Handle adding a new doctor
 * Called when admin submits the "Add Doctor" form in the modal
 */
window.adminAddDoctor = async function () {
  try {
    // Step 1: Get form inputs
    const nameInput = document.getElementById('doctorName') || document.querySelector('input[name="doctorName"]');
    const specialtyInput = document.getElementById('doctorSpecialty') || document.querySelector('input[name="doctorSpecialty"]');
    const emailInput = document.getElementById('doctorEmail') || document.querySelector('input[name="doctorEmail"]');
    const passwordInput = document.getElementById('doctorPassword') || document.querySelector('input[name="doctorPassword"]');
    const phoneInput = document.getElementById('doctorPhone') || document.querySelector('input[name="doctorPhone"]');
    const availabilityInputs = document.querySelectorAll('input[name="availability"]:checked');

    // Step 2: Validate required fields
    if (!nameInput || !nameInput.value.trim()) {
      alert('Please enter doctor name');
      return;
    }

    if (!specialtyInput || !specialtyInput.value.trim()) {
      alert('Please enter specialty');
      return;
    }

    if (!emailInput || !emailInput.value.trim()) {
      alert('Please enter email address');
      return;
    }

    if (!passwordInput || !passwordInput.value.trim()) {
      alert('Please enter password');
      return;
    }

    // Step 3: Build doctor object
    const availability = Array.from(availabilityInputs).map(input => input.value);

    const doctor = {
      name: nameInput.value.trim(),
      specialty: specialtyInput.value.trim(),
      email: emailInput.value.trim(),
      password: passwordInput.value.trim(),
      phone: phoneInput ? phoneInput.value.trim() : '',
      availability: availability.length > 0 ? availability : []
    };

    // Step 4: Get authentication token
    const token = localStorage.getItem('token');
    if (!token) {
      alert('Authentication token not found. Please log in again.');
      return;
    }

    // Step 5: Call service to save doctor
    const result = await saveDoctor(doctor, token);

    // Step 6: Handle response
    if (result.success) {
      alert('Doctor added successfully!');

      // Clear form inputs
      if (nameInput) nameInput.value = '';
      if (specialtyInput) specialtyInput.value = '';
      if (emailInput) emailInput.value = '';
      if (passwordInput) passwordInput.value = '';
      if (phoneInput) phoneInput.value = '';

      // Close modal
      const modal = document.getElementById('modal');
      if (modal) {
        modal.style.display = 'none';
      }

      // Reload doctor list
      loadDoctorCards();
    } else {
      alert(`Error: ${result.message}`);
    }
  } catch (error) {
    console.error('Error adding doctor:', error);
    alert('An unexpected error occurred while adding the doctor. Please try again.');
  }
};

/**
 * Export functions for use in other modules if needed
 */
export { loadDoctorCards, filterDoctorsOnChange, renderDoctorCards };
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/
