# Smart Clinic Management System - Architecture Documentation

## Section 1: Architecture Summary

This Spring Boot application implements a hybrid architectural approach that combines both MVC and REST design patterns to serve different client types and use cases. **Thymeleaf-based MVC controllers** render server-side HTML templates for the Admin and Doctor dashboards, providing traditional web application experiences through the browser. In parallel, **REST API controllers** expose JSON-based endpoints for stateless, scalable interactions with modules such as Appointments, Patient Dashboard, and Patient Records. This dual approach allows the system to support both interactive web interfaces and modern API consumers (mobile apps, third-party integrations, future frontend frameworks).

The application follows a **three-tier architectural pattern** with clear separation of concerns: the Presentation Tier handles user interactions through controllers, the Application Tier implements business logic in a centralized Service Layer, and the Data Tier abstracts database access through Repository patterns. Controllers—whether MVC or REST—delegate all business logic to a unified Service Layer, ensuring consistency, testability, and maintainability. The Service Layer enforces business rules, performs validations, coordinates multi-entity workflows, and acts as the sole gateway to the data persistence layer. This separation makes the application more modular, easier to test in isolation, and simpler to extend with new features.

Data persistence is handled through a **dual-database strategy**: MySQL stores all structured, relational data (Patient, Doctor, Appointment, and Admin entities) using JPA with normalized schemas and constraints, while MongoDB stores flexible, document-based Prescription records that benefit from schema flexibility and rapid iteration. Each database type is accessed through its own repository implementation—MySQL repositories leverage Spring Data JPA with automatic CRUD operations and custom query methods, while the MongoDB repository uses Spring Data MongoDB for document-oriented operations. This polyglot persistence approach optimizes storage and performance for each data type: relational constraints and normalization for user and appointment management, document flexibility for evolving prescription formats.

## Section 2: Numbered Flow of Data and Control

This section traces the complete request-response cycle through each architectural layer:

1. **User Interface Layer**
   Users interact with the system through two primary interfaces: Thymeleaf-rendered HTML pages (AdminDashboard and DoctorDashboard) served by the server, or REST API consumers (Appointments, PatientDashboard, PatientRecord modules) that communicate via HTTP requests and expect JSON responses.

2. **Controller Layer**
   The request is routed to the appropriate controller based on the URL path and HTTP method. Thymeleaf controllers handle server-side rendering requests and return `.html` templates, while REST controllers process API requests and return JSON payloads. Both controller types validate incoming data and prepare it for downstream processing.

3. **Service Layer**
   All controllers delegate business logic to a unified Service Layer, which serves as the core of the application. This layer applies business rules and validations (e.g., checking if a patient email is unique, validating doctor availability before scheduling), coordinates complex workflows across multiple entities, and ensures clean separation between controller concerns and data access logic.

4. **Repository Layer**
   The service layer communicates with specialized repository implementations to perform data access operations. MySQL repositories use Spring Data JPA to manage relational data (Patient, Doctor, Appointment, Admin), while the MongoDB repository uses Spring Data MongoDB to manage document-based Prescription records. Repositories abstract database-specific operations and expose a declarative interface.

5. **Database Access**
   Each repository executes database-specific operations: MySQL repositories query relational tables with JPA, enforcing constraints and relationships defined in the schema, while the MongoDB repository queries the prescriptions collection using MongoDB query operators. This dual-database approach leverages the strengths of both relational (structured, ACID-compliant) and document-oriented (flexible, schema-less) storage.

6. **Model Binding**
   Data retrieved from the databases is mapped into Java model classes. MySQL data is converted into JPA entities (annotated with `@Entity`), representing normalized relational rows as objects. MongoDB data is loaded into document models (annotated with `@Document`), mapping BSON/JSON structures into type-safe Java POJOs. These models provide a consistent, object-oriented representation across the application.

7. **Response Layer**
   The bound models flow back through the controllers to the presentation layer. In MVC flows, models are passed to Thymeleaf templates, which use them to render dynamic HTML pages in the browser. In REST flows, models (or transformed DTOs) are serialized into JSON and returned as HTTP responses. This completes the request-response cycle, delivering either a full web page or structured API data depending on the consumer type.
