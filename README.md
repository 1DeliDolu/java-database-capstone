# Smart Clinic Management System

Spring Boot-based clinic management application.

## Technology

- Java 17+
- Spring Boot 3.4.4
- Spring MVC + Thymeleaf
- Spring Data JPA (MySQL)
- Spring Data MongoDB (Prescription)
- JWT-based authentication
- Vanilla JS + HTML/CSS

## Architecture

- Backend: `app/src/main/java/com/project/back_end`
- Thymeleaf templates: `app/src/main/resources/templates`
- Static pages/assets: `app/src/main/resources/static`
- Configuration: `app/src/main/resources/application.properties`

## Running the Application

### Option 1: Docker Compose

```bash
docker compose up --build
```

- Application: `http://localhost:8081`
- MySQL: `localhost:3306`
- MongoDB: `localhost:27017`

### Option 2: Local

Prerequisite: MySQL and MongoDB must be running.

```bash
cd app
mvn spring-boot:run
```

## Main Flow

- Home page: `http://localhost:8081/`
- Central login: `http://localhost:8081/login`
- Central register: `http://localhost:8081/register`
- Role-based login preselection:
  - `http://localhost:8081/login?role=admin`
  - `http://localhost:8081/login?role=doctor`
  - `http://localhost:8081/login?role=patient`

Post-login redirection:

- Admin: `/adminDashboard/{token}`
- Doctor: `/doctorDashboard/{token}`
- Patient: `/pages/loggedPatientDashboard.html`

## Key Pages

- Admin dashboard: `templates/admin/adminDashboard.html`
- Doctor dashboard: `templates/doctor/doctorDashboard.html`
- Central login/register: `templates/auth/centralLogin.html`, `templates/auth/centralRegister.html`

## API Overview

### Admin Auth

- `POST /api/admin/login`

### Doctor

- `GET /doctor`
- `POST /doctor/{token}`
- `POST /doctor/login`
- `PUT /doctor/{token}`
- `DELETE /doctor/{id}/{token}`
- `GET /doctor/filter/{name}/{time}/{speciality}`
- `GET /doctor/availability/{user}/{doctorId}/{date}/{token}`

### Patient

- `POST /patient` (signup)
- `POST /patient/login`
- `GET /patient/{token}`
- `GET /patient/{id}/{token}`
- `GET /patient/filter/{condition}/{name}/{token}`

### Appointment

- `GET /appointments/{date}/{patientName}/{token}`
- `POST /appointments/{token}`
- `PUT /appointments/{token}`
- `DELETE /appointments/{id}/{token}`

### Prescription

- `POST /api/prescription/{token}`
- `GET /api/prescription/{appointmentId}/{token}`

### Admin Management (Dashboard tables)

- `GET /api/admin/manage/overview/{token}`
- `PUT /api/admin/manage/doctor/{token}`
- `DELETE /api/admin/manage/doctor/{id}/{token}`
- `PUT /api/admin/manage/patient/{token}`
- `DELETE /api/admin/manage/patient/{id}/{token}`
- `PUT /api/admin/manage/appointment/{token}`
- `DELETE /api/admin/manage/appointment/{id}/{token}`

## Configuration Notes

In `application.properties`:

- `server.port=8081`
- `api.path=/api/`
- Thymeleaf path/caching settings enabled
- `spring.web.resources.static-locations=classpath:/static/`

## Development Notes

- If port conflict occurs (`8081 already in use`), free the port and restart.
- Some endpoints pass token as a path parameter.
- Dashboard access is protected with token validation.
