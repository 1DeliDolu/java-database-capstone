/**
 * Doctor Card Component
 *
 * Creates a reusable card component for displaying doctor information.
 * Shows different action buttons based on user role (admin, patient, loggedPatient).
 *
 * Dependencies:
 * - deleteDoctor() from: /js/services/doctorServices.js
 * - getPatientData() from: /js/services/patientServices.js
 * - showBookingOverlay() typically from: /js/loggedPatient.js or similar
 */

/**
 * Create and return a doctor card element
 * @param {Object} doctor - Doctor object containing name, specialty, email, availability, etc.
 * @returns {HTMLElement} - The complete doctor card DOM element
 */
export function createDoctorCard(doctor) {
  // Create main card container
  const card = document.createElement("div");
  card.classList.add("doctor-card");
  card.setAttribute("data-doctor-id", doctor.id);

  // Get current user role from localStorage
  const role = localStorage.getItem("userRole");

  // ===== CREATE DOCTOR INFO SECTION =====
  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  // Doctor name
  const name = document.createElement("h3");
  name.classList.add("doctor-name");
  name.textContent = doctor.name;

  // Doctor specialization
  const specialization = document.createElement("p");
  specialization.classList.add("doctor-specialty");
  specialization.textContent = `Specialty: ${doctor.specialty || "General Physician"}`;

  // Doctor email
  const email = document.createElement("p");
  email.classList.add("doctor-email");
  email.textContent = `Email: ${doctor.email || "N/A"}`;

  // Doctor phone
  const phone = document.createElement("p");
  phone.classList.add("doctor-phone");
  phone.textContent = `Phone: ${doctor.phone || "N/A"}`;

  // Doctor availability times
  const availabilityDiv = document.createElement("div");
  availabilityDiv.classList.add("doctor-availability");
  const availabilityLabel = document.createElement("p");
  availabilityLabel.classList.add("availability-label");
  availabilityLabel.textContent = "Available Times:";

  const availabilityTimes = document.createElement("p");
  availabilityTimes.classList.add("availability-times");
  if (doctor.availability && Array.isArray(doctor.availability)) {
    availabilityTimes.textContent = doctor.availability.join(", ");
  } else if (doctor.availability) {
    availabilityTimes.textContent = doctor.availability;
  } else {
    availabilityTimes.textContent = "Not specified";
  }

  availabilityDiv.appendChild(availabilityLabel);
  availabilityDiv.appendChild(availabilityTimes);

  // Append all info elements to doctor info container
  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(phone);
  infoDiv.appendChild(availabilityDiv);

  // ===== CREATE ACTION BUTTONS SECTION =====
  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  // ===== ADMIN ROLE ACTIONS =====
  if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.classList.add("btn", "btn-danger", "delete-btn");
    removeBtn.textContent = "Delete";

    removeBtn.addEventListener("click", async () => {
      // 1. Confirm deletion
      if (confirm(`Are you sure you want to delete Dr. ${doctor.name}?`)) {
        // 2. Get token from localStorage
        const token = localStorage.getItem("token");
        try {
          // 3. Call API to delete
          const response = await fetch(`/api/doctors/${doctor.id}`, {
            method: "DELETE",
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "application/json",
            },
          });

          if (response.ok) {
            // 4. On success: remove the card from the DOM
            card.remove();
            alert(`Dr. ${doctor.name} deleted successfully`);
          } else {
            alert("Failed to delete doctor. Please try again.");
          }
        } catch (error) {
          console.error("Error deleting doctor:", error);
          alert("An error occurred while deleting the doctor.");
        }
      }
    });

    actionsDiv.appendChild(removeBtn);
  }

  // ===== PATIENT (NOT LOGGED-IN) ROLE ACTIONS =====
  // Shows a button but alerts the user that login is required
  else if (role === "patient") {
    const bookNow = document.createElement("button");
    bookNow.classList.add("btn", "btn-primary", "book-btn");
    bookNow.textContent = "Book Now";

    bookNow.addEventListener("click", () => {
      alert("Patient needs to login first.");
    });

    actionsDiv.appendChild(bookNow);
  }

  // ===== LOGGED-IN PATIENT ROLE ACTIONS =====
  // Allows real booking by calling a function from another module
  else if (role === "loggedPatient") {
    const bookNow = document.createElement("button");
    bookNow.classList.add("btn", "btn-primary", "book-btn");
    bookNow.textContent = "Book Now";

    bookNow.addEventListener("click", async (e) => {
      const token = localStorage.getItem("token");

      if (!token) {
        alert("Session expired. Please log in again.");
        window.location.href = "/";
        return;
      }

      try {
        // Fetches patient data first
        const patientData = await getPatientData(token);

        // Show booking overlay UI with doctor and patient info
        showBookingOverlay(e, doctor, patientData);
      } catch (error) {
        console.error("Error fetching patient data:", error);
        alert("Failed to load booking form. Please try again.");
      }
    });

    actionsDiv.appendChild(bookNow);
  }

  // ===== DOCTOR ROLE - NO ACTIONS =====
  // Doctors don't see buttons on doctor cards

  // ===== FINAL ASSEMBLY =====
  // Add all the created pieces to the card
  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);

  // Return the final card
  return card;
}

/**
 * Helper function to fetch patient data
 * This function is intended to be imported from patientServices.js
 * @param {string} token - Authentication token
 * @returns {Promise<Object>} - Patient data
 */
async function getPatientData(token) {
  try {
    const response = await fetch("/api/patients/me", {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch patient data");
    }

    return await response.json();
  } catch (error) {
    console.error("Error fetching patient data:", error);
    throw error;
  }
}

/**
 * Helper function to show booking overlay
 * This function should be imported from loggedPatient.js or util.js
 * @param {Event} e - Click event
 * @param {Object} doctor - Selected doctor
 * @param {Object} patient - Current patient data
 */
function showBookingOverlay(e, doctor, patient) {
  // This function would typically open a modal with appointment booking form
  // It should include date/time selection, reason for visit, etc.

  // Example implementation - calls a function from util or other module
  if (typeof window.showBookingOverlay === "function") {
    window.showBookingOverlay(e, doctor, patient);
  } else {
    // Fallback: show a simple alert
    console.warn("showBookingOverlay function not found. Using fallback.");
    alert(`Booking appointment with Dr. ${doctor.name} for ${patient.name}`);
  }
}
