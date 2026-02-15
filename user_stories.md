# User Story Template

**Title:**
_As a [user role], I want [feature/goal], so that [reason]._

**Acceptance Criteria:**

1. [Criteria 1]
2. [Criteria 2]
3. [Criteria 3]

**Priority:** [High/Medium/Low]
**Story Points:** [Estimated Effort in Points]
**Notes:**

- [Additional information or edge cases]

---

## Admin User Stories

### Story 1: Admin Login with Credentials

**Title:**
_As an admin, I want to log into the portal with my username and password, so that I can access and manage the platform securely._

**Acceptance Criteria:**

1. Admin can enter username and password on the login page
2. System validates credentials against the admin database
3. Upon successful authentication, a JWT token is generated and stored
4. Admin is redirected to the admin dashboard
5. Invalid credentials display an error message

**Priority:** High
**Story Points:** 5
**Notes:**

- Use JWT token-based authentication with 7-day expiration
- Password should be securely hashed before storage
- Failed login attempts should be logged for security audits

---

### Story 2: Admin Logout

**Title:**
_As an admin, I want to log out of the portal, so that I can protect system access when I step away._

**Acceptance Criteria:**

1. Admin can click a "Logout" button on the dashboard
2. JWT token is invalidated on the server side
3. Session data is cleared from the client
4. Admin is redirected to the login page
5. Subsequent requests with the old token are rejected

**Priority:** High
**Story Points:** 3
**Notes:**

- Ensure token is cleared from browser storage (localStorage/sessionStorage)
- Log all logout events for audit trails
- Prevent direct dashboard access without valid token after logout

---

### Story 3: Add Doctors to the Portal

**Title:**
_As an admin, I want to add new doctors to the portal with their details, so that doctors can be registered and available to patients for appointments._

**Acceptance Criteria:**

1. Admin can access a "Add Doctor" form from the dashboard
2. Form accepts doctor details: name, specialty, email, phone, and available time slots
3. System validates all required fields and email format
4. Email uniqueness is checked against existing doctors
5. Upon submission, doctor record is saved to MySQL database
6. Success message is displayed and doctor appears in doctor list

**Priority:** High
**Story Points:** 8
**Notes:**

- Specialty should be selected from a predefined list
- Available times should allow multiple time slot entries
- Email validation prevents duplicate doctors
- Default password should be generated and communicated securely

---

### Story 4: Delete Doctor Profile from Portal

**Title:**
_As an admin, I want to delete a doctor's profile from the portal, so that I can remove inactive or terminated doctors._

**Acceptance Criteria:**

1. Admin can select a doctor from the doctor list
2. Delete button is available on the doctor's profile page
3. Confirmation dialog appears before deletion
4. Upon confirmation, doctor record is deleted from MySQL database
5. All associated appointments are marked as cancelled
6. Success message confirms deletion

**Priority:** Medium
**Story Points:** 5
**Notes:**

- Soft delete (mark as inactive) is recommended instead of hard delete to preserve data
- Associated appointments should cascade properly
- Audit log should record who deleted the doctor and when
- Consider data integrity constraints before deletion

---

### Story 5: Generate Monthly Appointment Statistics

**Title:**
_As an admin, I want to run a stored procedure to get appointment statistics by month, so that I can track usage patterns and system performance._

**Acceptance Criteria:**

1. Admin can access a "Reports" section in the dashboard
2. "Monthly Appointments" report option is available
3. Clicking the option executes a MySQL stored procedure
4. Stored procedure counts appointments grouped by month
5. Results display total appointments per month in a readable format
6. Results can be exported or downloaded (optional)

**Priority:** Medium
**Story Points:** 8
**Notes:**

- Stored procedure should be named `GetAppointmentsPerMonth()`
- Query should filter by appointment status (scheduled, completed)
- Results should span the last 12 months
- Consider caching results for performance if large dataset
- Add execution timestamp to audit stored procedure calls

---

## Patient User Stories

### Story 6: View List of Doctors Without Login

**Title:**
_As a patient, I want to view a list of doctors without logging in, so that I can explore available options before registering._

**Acceptance Criteria:**

1. A public page displays all available doctors
2. Doctor list shows name, specialty, and brief description
3. No authentication is required to access this page
4. Doctor list is searchable by name or specialty
5. Pagination is implemented if doctor list is large
6. List updates automatically when new doctors are added by admin

**Priority:** High
**Story Points:** 5
**Notes:**

- Public endpoint should be available at `/api/doctors` or similar
- No sensitive information (phone, email) should be visible
- Consider caching for performance optimization
- Separate "public" and "authenticated" API responses

---

### Story 7: Patient Sign Up with Email and Password

**Title:**
_As a patient, I want to sign up using my email and password, so that I can create an account and book appointments._

**Acceptance Criteria:**

1. Patient can access a registration form
2. Form collects: name, email, password, phone, and address
3. System validates email format and checks for uniqueness
4. System validates password strength (minimum 6 characters)
5. Phone number is validated (10 digits)
6. Upon successful registration, account is created in MySQL database
7. Confirmation message is displayed and patient is redirected to login

**Priority:** High
**Story Points:** 8
**Notes:**

- Email verification (optional) can be implemented for future enhancement
- Password should be securely hashed using bcrypt or similar
- Phone number pattern: must be exactly 10 digits
- Address field should accept up to 255 characters
- Prevent duplicate emails during registration

---

### Story 8: Patient Login to Portal

**Title:**
_As a patient, I want to log into the portal with my email and password, so that I can manage my bookings securely._

**Acceptance Criteria:**

1. Patient can enter email and password on the login page
2. System validates credentials against the patient database
3. Upon successful authentication, JWT token is generated
4. Patient is redirected to the patient dashboard
5. Invalid credentials display an appropriate error message
6. Account lockout mechanism after multiple failed attempts (optional)

**Priority:** High
**Story Points:** 5
**Notes:**

- Use JWT token with 7-day expiration
- Failed login attempts should be logged for security
- Consider implementing rate limiting to prevent brute force attacks
- Display helpful error messages (e.g., "Invalid email or password")

---

### Story 9: Patient Logout from Portal

**Title:**
_As a patient, I want to log out of the portal, so that I can secure my account when I'm done._

**Acceptance Criteria:**

1. Patient can click a "Logout" button on the dashboard
2. JWT token is invalidated on the server side
3. Session and local storage data are cleared
4. Patient is redirected to the home page or login page
5. Subsequent requests with the old token are rejected

**Priority:** Medium
**Story Points:** 3
**Notes:**

- Ensure complete session cleanup on client side
- Log logout events for audit trail
- Token should not be reusable after logout
- Clear all cached data related to patient session

---

### Story 10: Book an Hour-Long Appointment with Doctor

**Title:**
_As a patient, I want to log in and book an hour-long appointment to consult with a doctor, so that I can receive medical care._

**Acceptance Criteria:**

1. Patient can access an "Book Appointment" form from the dashboard
2. Form allows selection of doctor from available doctors
3. System displays available time slots for selected doctor
4. Patient can select a one-hour time slot
5. System checks if the slot is available and not already booked
6. Upon confirmation, appointment is saved to MySQL database
7. Confirmation message displays appointment details (date, time, doctor)
8. Appointment appears in patient's upcoming appointments list

**Priority:** High
**Story Points:** 13
**Notes:**

- Available times are pulled from doctor's `availableTimes` field
- System should prevent overbooking
- Appointment duration is fixed at 1 hour
- Send confirmation email/notification to patient (optional)
- Appointment status should be set to "scheduled" upon creation

---

### Story 11: View Upcoming Appointments

**Title:**
_As a patient, I want to view my upcoming appointments, so that I can prepare accordingly and manage my schedule._

**Acceptance Criteria:**

1. Patient can access "My Appointments" page from the dashboard
2. Only future/scheduled appointments are displayed by default
3. Each appointment shows: date, time, doctor name, and specialty
4. Appointments are sorted by date (earliest first)
5. Patient can filter appointments by status (scheduled, completed, cancelled)
6. Patient can view additional appointment details
7. Options to reschedule or cancel appointments are available

**Priority:** High
**Story Points:** 8
**Notes:**

- Use appointment status field to filter (0=scheduled, 1=completed)
- Display only appointments after current date/time
- Consider paginating if patient has many appointments
- Show doctor contact information for reference
- Cancelled appointments can be shown in separate view/list

---

## Doctor User Stories

### Story 12: Doctor Login to Portal

**Title:**
_As a doctor, I want to log into the portal with my credentials, so that I can manage my appointments and patient interactions securely._

**Acceptance Criteria:**

1. Doctor can access the login page
2. Doctor enters email and password credentials
3. System validates credentials against the doctor database
4. Upon successful authentication, JWT token is generated
5. Doctor is redirected to the doctor dashboard
6. Invalid credentials display an error message
7. Token expires after 7 days of inactivity

**Priority:** High
**Story Points:** 5
**Notes:**

- Use JWT token-based authentication consistent with other user types
- Password should be securely hashed before storage
- Failed login attempts should be logged for security
- Display clear error messages for authentication failures

---

### Story 13: Doctor Logout from Portal

**Title:**
_As a doctor, I want to log out of the portal, so that I can protect my data and account security._

**Acceptance Criteria:**

1. Doctor can click "Logout" button from the dashboard
2. JWT token is invalidated on the server side
3. Session data is cleared from the client
4. Doctor is redirected to the login page
5. All cached appointment data is cleared
6. Subsequent API requests with the old token are rejected

**Priority:** Medium
**Story Points:** 3
**Notes:**

- Ensure complete cleanup of authenticated session
- Log all logout events for audit purposes
- Token should not be reusable after logout
- Clear browser storage and local caches

---

### Story 14: View Appointment Calendar

**Title:**
_As a doctor, I want to view my appointment calendar, so that I can stay organized and manage my schedule effectively._

**Acceptance Criteria:**

1. Doctor can access an "Appointments" view from the dashboard
2. Calendar displays all scheduled appointments for the doctor
3. Appointments show patient name, time, and status
4. Doctor can switch between day, week, and month views
5. Current date is highlighted in the calendar
6. Appointments are color-coded by status (scheduled, completed)
7. Doctor can click on an appointment to view full details

**Priority:** High
**Story Points:** 10
**Notes:**

- Calendar should retrieve appointments from MySQL database
- Real-time updates when new appointments are booked
- Time should be displayed in doctor's local timezone (consider later enhancement)
- Support filtering by appointment status
- Implement pagination if many appointments exist

---

### Story 15: Mark Unavailability Periods

**Title:**
_As a doctor, I want to mark my unavailability periods, so that patients see only available slots and cannot book during my off-time._

**Acceptance Criteria:**

1. Doctor can access an "Availability" management page
2. Doctor can select date(s) and time period(s) to mark as unavailable
3. System validates that unavailable slots don't conflict with existing appointments
4. Doctor can provide a reason for unavailability (optional)
5. Unavailable periods are saved to the database
6. Patient booking system excludes these periods from available slots
7. Doctor can edit or delete unavailability records

**Priority:** High
**Story Points:** 8
**Notes:**

- Unavailability can be for recurring (daily) or one-time events
- Should support blocking full days or specific time slots
- Display current availability settings clearly
- Consider timezone handling if doctors are in different regions
- Prevent patients from booking during marked unavailable times

---

### Story 16: Update Doctor Profile and Specialization

**Title:**
_As a doctor, I want to update my profile with specialization and contact information, so that patients have up-to-date information about me._

**Acceptance Criteria:**

1. Doctor can access a "Profile" or "Settings" page
2. Doctor can edit: name, specialty, phone number, and professional details
3. System validates email is not changed (email is unique identifier)
4. Changes are saved to the MySQL database immediately
5. Success message confirms the profile has been updated
6. Updated information appears immediately in patient-facing doctor list
7. Doctor can view a preview of their public profile

**Priority:** Medium
**Story Points:** 5
**Notes:**

- Specialty should be selected from a predefined list maintained by admin
- Phone validation (10 digits) should be enforced
- Consider adding additional fields: qualifications, experience, bio
- Changes should be reflected in patient's doctor search results
- Audit log should record profile modifications and timestamps

---

### Story 17: View Patient Details for Upcoming Appointments

**Title:**
_As a doctor, I want to view patient details for my upcoming appointments, so that I can be prepared and provide better medical care._

**Acceptance Criteria:**

1. Doctor can access an "Upcoming Appointments" list from the dashboard
2. For each appointment, doctor can click to view patient details
3. Patient details include: name, email, phone, address, and medical history (if available)
4. Patient contact information is displayed for appointment communication
5. Appointment notes or reason for visit (if provided) are visible
6. Doctor can add notes to the appointment (for internal reference)
7. Previous appointments with the same patient can be referenced

**Priority:** High
**Story Points:** 8
**Notes:**

- Patient details should be retrieved from MySQL patient table
- Medical history can be retrieved from MongoDB prescriptions collection
- Display only relevant/necessary patient information (privacy consideration)
- Consider HIPAA/data privacy compliance when storing sensitive info
- Allow doctor to add private notes without exposing to patient
- Search functionality to find patient quickly
