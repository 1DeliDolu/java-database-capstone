import { API_BASE_URL } from "../config/config.js";

export function createDoctorCard(doctor) {
  const card = document.createElement("div");
  card.classList.add("doctor-card");
  card.setAttribute("data-doctor-id", doctor.id);

  const role = localStorage.getItem("userRole");
  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  const name = document.createElement("h3");
  name.classList.add("doctor-name");
  name.textContent = doctor.name;

  const specialization = document.createElement("p");
  specialization.classList.add("doctor-specialty");
  specialization.textContent = `Specialization: ${doctor.specialty || "General Physician"}`;

  const email = document.createElement("p");
  email.classList.add("doctor-email");
  email.textContent = `Email: ${doctor.email || "N/A"}`;

  const phone = document.createElement("p");
  phone.classList.add("doctor-phone");
  phone.textContent = `Phone: ${doctor.phone || "N/A"}`;

  const availability = document.createElement("p");
  availability.classList.add("doctor-availability");
  const slots = Array.isArray(doctor.availableTimes) ? doctor.availableTimes : [];
  availability.innerHTML = `<span class="availability-label">Available:</span> <span class="availability-times">${slots.length ? slots.join(", ") : "Not specified"}</span>`;

  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(phone);
  infoDiv.appendChild(availability);

  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.classList.add("delete-btn");
    removeBtn.textContent = "Delete";

    removeBtn.addEventListener("click", async () => {
      const token = localStorage.getItem("token");
      if (!token) return;
      if (!confirm(`Are you sure you want to delete Dr. ${doctor.name}?`)) return;

      try {
        const response = await fetch(
          `${API_BASE_URL}/doctor/${encodeURIComponent(doctor.id)}/${encodeURIComponent(token)}`,
          { method: "DELETE", headers: { "Content-Type": "application/json" } }
        );
        const data = await response.json();
        if (response.ok) {
          card.remove();
        } else {
          alert(data.message || "Failed to delete doctor.");
        }
      } catch (error) {
        console.error("Error deleting doctor:", error);
        alert("An error occurred while deleting the doctor.");
      }
    });

    actionsDiv.appendChild(removeBtn);
  } else {
    const bookNow = document.createElement("button");
    bookNow.classList.add("book-btn");
    bookNow.textContent = "Book Now";

    bookNow.addEventListener("click", async () => {
      if (role === "patient") {
        window.location.href = "/login?role=patient";
        return;
      }

      const token = localStorage.getItem("token");
      if (!token) {
        window.location.href = "/login?role=patient";
        return;
      }

      const patient = await fetchPatientByToken(token);
      if (!patient?.id) {
        alert("Session expired. Please login again.");
        window.location.href = "/login?role=patient";
        return;
      }

      const query = new URLSearchParams({
        doctorId: doctor.id,
        doctorName: doctor.name || "",
        patientId: patient.id,
        patientName: patient.name || "",
      });
      window.location.href = `/pages/patientAppointments.html?${query.toString()}`;
    });

    actionsDiv.appendChild(bookNow);
  }

  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);
  return card;
}

async function fetchPatientByToken(token) {
  try {
    const response = await fetch(`${API_BASE_URL}/patient/${encodeURIComponent(token)}`, {
      method: "GET",
      headers: { "Content-Type": "application/json" },
    });
    const data = await response.json();
    if (!response.ok) return null;
    return data.patient || null;
  } catch (error) {
    console.error("Error fetching patient data:", error);
    return null;
  }
}
