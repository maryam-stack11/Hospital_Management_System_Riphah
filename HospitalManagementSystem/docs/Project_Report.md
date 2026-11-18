# Hospital Management System
### A Console-Based Management Application Developed in Java

**Riphah International University**
**Course:** Java Object-Oriented Programming  |  **Program:** BS Computer Science (BSCS)
**Supervisor:** Ma'am Nayab  |  **Submission Date:** 27 October 2026

| Student Name | SAP ID | Email |
|---|---|---|
| Ishraq Abbas | 78501 | 78501@students.riphah.edu.pk |
| Maryam Sheraz Khan | 79068 | 79068@students.riphah.edu.pk |

---

## 1. Abstract
This project is a console-based Hospital Management System developed in Java. It manages patient details, doctor information, appointments and basic billing records. The application uses object-oriented programming to organize patients, doctors, appointments and bills into separate classes, and demonstrates classes and objects, constructors, encapsulation, inheritance, abstraction, polymorphism, collections using `ArrayList`, loops and conditional statements. It uses only standard Java functionality and console menus, without external libraries or a database; records are kept between runs in simple CSV text files.

## 2. Introduction
Object-oriented programming helps organize software into reusable and manageable components. This project applies Java OOP concepts to a Hospital Management System that maintains patient and doctor details, schedules appointments and records billing information. Separate classes and methods manage each function. Being a console application, the project focuses on class design, object interaction, data handling and core Java programming concepts.

## 3. Problem Statement
Beginner programming exercises often teach concepts individually through small examples. This project combines those concepts in one practical application. The system connects patient registration, doctor records, appointment scheduling and billing through an object-oriented design, providing a suitable setting for applying Java fundamentals in a manageable student project.

## 4. Objectives
- Develop a console-based Hospital Management System in Java.
- Demonstrate classes, objects, constructors and encapsulation through patient, doctor and appointment records.
- Apply inheritance, abstraction and polymorphism using a parent `Person` class and the child classes `Patient` and `Doctor`.
- Use `ArrayList` to manage patient, doctor, appointment and billing records.
- Provide patient registration, doctor management, appointment scheduling and basic billing.
- Implement input validation, record searching, appointment status updates and a return-to-menu option.
- Prepare a clear class structure and UML class diagram for documentation and presentation.

## 5. Project Description
The application starts with a welcome screen and a main menu. The user can register patients, manage doctor details, book appointments, search records, generate bills and view hospital statistics. All console input is validated and clear confirmation or error messages are displayed. A `Person` parent class holds the common details; `Patient` and `Doctor` extend it with role-specific fields and methods. Appointment records connect patients with doctors, dates and times. Business rules (no double booking, no past appointments, no duplicate bills) are enforced by the manager classes.

## 6. Functional Requirements
| Module | Requirements |
|---|---|
| Patient | Register (auto ID), view all, search by ID/name, update, delete with safeguards |
| Doctor | Add (auto ID), view, search by ID/specialization, update, delete, availability |
| Appointment | Book, view, search, by patient/doctor, update status, cancel |
| Billing | Generate bill, view, search, patient history, update payment status |
| Statistics | Patient, doctor, appointment (by status) and billing totals, revenue and outstanding balance |
| Data | Save/load CSV files; tolerate missing or corrupted data |

**Key business rules:** patient and doctor must exist before booking; a doctor or patient cannot have two *scheduled* appointments at the same date and time; cancelled appointments do not count as active; past dates are refused; a doctor accepts at most 8 appointments per day; bills are generated only for completed appointments, once per appointment, using the doctor's consultation fee.

## 7. System Modules
1. **System Control** (`HospitalManagementSystem.java`, `Main.java`) - welcome screen, main menu, coordination, statistics, saving.
2. **Patient Management** (`Patient.java`, `PatientManager.java`).
3. **Doctor Management** (`Doctor.java`, `DoctorManager.java`).
4. **Appointment Management** (`Appointment.java`, `AppointmentManager.java`).
5. **Person Parent Class** (`Person.java`) - abstract class with ID, name, contact number.
6. **Patient and Doctor child classes** - extend `Person`, override `displayDetails()`.
7. **Billing** (`Billing.java`, `BillingManager.java`) - charges, totals and payment status.
8. **Supporting classes** - `DataManager` (CSV persistence), `InputHelper`, `UI`, `Validator`, `HospitalException`.

## 8. OOP Concepts Used
| Concept | Where |
|---|---|
| Classes and objects | `Patient`, `Doctor`, `Appointment`, `Billing` objects stored in lists |
| Constructors | All model classes; children call `super(...)` |
| Encapsulation | Private fields with getters/setters; controlled status changes |
| Inheritance | `Patient` and `Doctor` extend `Person` |
| Abstraction | `abstract class Person` with abstract `getRole()` and `displayDetails()` |
| Polymorphism / overriding | `displayDetails()` and `getRole()` overridden in both children |
| ArrayList | One list per record type in each manager |
| Loops / conditionals | Menu loops, search loops, validation loops, `switch`/`if` |
| Exception handling | Custom `HospitalException`, caught in the menus |
| Object relationships | `Appointment` holds `Patient` and `Doctor`; managers aggregate lists |

See `OOP_Concepts_Explanation.md` for details and `UML_Class_Diagram.md` for the class diagram.

## 9. Tools and Technologies
- Language: Java (developed and tested with OpenJDK 21; JDK 17+ recommended)
- IDE: Visual Studio Code with the Extension Pack for Java
- Java standard library only: `Scanner`, `ArrayList`, `java.time`, `java.math.BigDecimal`, `java.nio.file`
- Mermaid for UML and flowchart documentation
- No external libraries or database

## 10. Working Criterion
1. The user opens the system and selects an option from the main menu.
2. The system shows options for patient, doctor, appointment and billing management.
3. The user selects an operation and enters the required details.
4. The system validates the details and saves new patient or doctor records.
5. The user books an appointment by selecting a patient, doctor, date and time.
6. The system checks the entered records and shows appointment or billing information on request.
7. Invalid input produces a clear message and the user can try again.
8. The system confirms completed actions and returns to the menu. See `Flowchart.md`.

## 11. Expected Results
A working, menu-driven console application in which patients and doctors can be registered, appointments scheduled without conflicts, bills produced from doctor fees, payment status tracked, and statistics calculated from real records, with data surviving restarts. Testing results are documented in `tests/TESTING_REPORT.md`.

## 12. Limitations
- Console interface only (no GUI); single user at a time.
- Data is stored in plain CSV text files, not a database; there is no user login or encryption.
- Appointments are free-time slots (no fixed slot grid or rescheduling feature); availability is a manual status plus a daily limit of 8 appointments.
- Patients/doctors with completed appointments cannot be deleted (records are retained).
- Currency is fixed to Rs.; there are no insurance, discounts or taxes.

## 13. Conclusion
The Hospital Management System meets the proposal's goals: it combines the main Java OOP concepts in one practical application with a clear class structure, validated input and tested business rules. It can be extended in future with a database, a graphical interface, user roles and reporting.
