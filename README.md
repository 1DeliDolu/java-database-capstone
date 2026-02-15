# Smart Clinic Management System

Smart Clinic Management System is a Spring Boot application for managing doctors, patients, appointments, and prescriptions with role-based access.

## Tech Stack

- Java 17+
- Spring Boot 3.4.4
- Spring MVC + Thymeleaf
- Spring Data JPA (MySQL)
- Spring Data MongoDB (Prescriptions)
- JWT authentication
- Vanilla JavaScript, HTML, CSS

## Repository Structure

- Backend Java source: `app/src/main/java/com/project/back_end`
- Backend resources: `app/src/main/resources`
- Thymeleaf templates: `app/src/main/resources/templates`
- Static frontend files: `app/src/main/resources/static`
- Main config file: `app/src/main/resources/application.properties`
- Docker compose: `docker-compose.yml`

## Running the Project

### Option 1: Docker Compose

```bash
docker compose up --build
```

Services:
- App: `http://localhost:8081`
- MySQL: `localhost:3306`
- MongoDB: `localhost:27017`

### Option 2: Local Run

Prerequisites:
- Java 17+
- Maven
- Running MySQL and MongoDB instances

```bash
cd app
mvn spring-boot:run
```

## Main User Flow

- Home: `http://localhost:8081/`
- Central login: `http://localhost:8081/login`
- Central register: `http://localhost:8081/register`

Role-specific login shortcuts:
- Admin: `http://localhost:8081/login?role=admin`
- Doctor: `http://localhost:8081/login?role=doctor`
- Patient: `http://localhost:8081/login?role=patient`

Post-login destinations:
- Admin: `/adminDashboard/{token}`
- Doctor: `/doctorDashboard/{token}`
- Patient: `/pages/loggedPatientDashboard.html`

## Core API Endpoints

### Admin

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

- `POST /patient`
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

### Admin Management

- `GET /api/admin/manage/overview/{token}`
- `PUT /api/admin/manage/doctor/{token}`
- `DELETE /api/admin/manage/doctor/{id}/{token}`
- `PUT /api/admin/manage/patient/{token}`
- `DELETE /api/admin/manage/patient/{id}/{token}`
- `PUT /api/admin/manage/appointment/{token}`
- `DELETE /api/admin/manage/appointment/{id}/{token}`

## Important Configuration

Key values in `app/src/main/resources/application.properties`:

- `server.port=8081`
- `api.path=/api/`
- `spring.web.resources.static-locations=classpath:/static/`

## Development Notes

- If port `8081` is busy, stop the process using it and restart the app.
- Many endpoints include JWT tokens in path parameters.
- UI dashboards rely on valid token + role checks in frontend scripts.
