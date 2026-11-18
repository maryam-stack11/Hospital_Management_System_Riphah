# Hospital Management System

**Riphah International University - BSCS - Java Object-Oriented Programming**
Supervisor: Ma'am Nayab | Submission: 27 October 2026

| Student | SAP ID |
|---|---|
| Ishraq Abbas | 78501 |
| Maryam Sheraz Khan | 79068 |

## Overview
A console-based Hospital Management System written in plain Java (standard library only). It manages patients, doctors, appointments and billing, and demonstrates the core OOP concepts: classes/objects, constructors, encapsulation, inheritance, abstraction, polymorphism, method overriding, `ArrayList`, loops, conditionals and exception handling.

## Features
- Welcome screen and main menu; every module has its own submenu with a return option
- **Patients:** register (auto ID `P001...`), view, search by ID / name, update, delete (blocked if completed history exists; active appointments are cancelled first, after confirmation)
- **Doctors:** add (auto ID `D001...`), view, search by ID / specialization, update, delete, availability view (manual status + daily capacity of 8)
- **Appointments:** book (auto ID `A001...`), view, search, by patient / doctor, update status, cancel. No double booking (doctor or patient), no past dates, cancelled slots are reusable
- **Billing:** generate (fee taken from the doctor, `BigDecimal` maths), view, search, patient history, payment status (Paid / Unpaid / Partially Paid), no duplicate bills
- **Statistics:** all figures calculated from live records
- **Persistence:** CSV files in `data/` (loaded at start, saved after every change); missing/empty files are fine and corrupted lines are skipped with a warning

## Requirements
JDK 17 or newer (developed and tested with OpenJDK 21). No other libraries.

## Compile and run
**Linux / macOS / Git Bash:** `./run.sh`
**Windows:** double-click `run.bat`
**Manually (from the project folder):**
```
javac -d out src/*.java
java -cp out Main
```
Optional argument: another data folder, e.g. `java -cp out Main mydata`.

**VS Code:** install the "Extension Pack for Java", use *File > Open Folder...* on `HospitalManagementSystem`, open `src/Main.java` and click *Run* (or press F5, config included). Input is typed in the integrated terminal.

**Run the automated tests:** `./tests/run_tests.sh` (or `tests\run_tests.bat`).

## Project structure
```
HospitalManagementSystem/
  src/    Main, HospitalManagementSystem, Person, Patient, Doctor, Appointment, Billing,
          PatientManager, DoctorManager, AppointmentManager, BillingManager, DataManager,
          HospitalException, InputHelper, UI, Validator
  data/   patients.csv, doctors.csv, appointments.csv, bills.csv  (6 sample doctors included)
  docs/   Project_Report.md, UML_Class_Diagram.md, Flowchart.md, OOP_Concepts_Explanation.md
  tests/  HospitalTests.java, TESTING_REPORT.md, scripts/ (scripted console sessions + outputs)
  run.sh, run.bat, README.md, .gitignore
```

## Sample workflow
1. Start the program -> **[1] Patient Management -> [1] Register New Patient**
2. **[3] Appointment Management -> [1] Book New Appointment** (patient `P001`, doctor `D001`, date `2030-01-15`, time `10:00`)
3. **[6] Update Appointment Status -> Completed** once the visit is done
4. **[4] Billing Management -> [1] Generate Bill** (enter extra charges) -> **[5] Update Payment Status**
5. **[5] View Hospital Statistics**, then **[6] Exit** (data is saved automatically)

To start with a clean database, empty the four CSV files except for their header line (keep the header).
