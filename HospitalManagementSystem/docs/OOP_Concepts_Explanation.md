# OOP Concepts Used - Viva Guide

Each concept below points to real code in `src/`.

## 1. Classes and Objects
A **class** is a blueprint; an **object** is one real item made from it.
`Patient`, `Doctor`, `Appointment`, `Billing` are classes. `new Patient("P001", "Ali Khan", ...)` in `PatientManager.registerPatient()` creates one patient object.
*Say in viva:* "Every record in the hospital is an object stored in an ArrayList."

## 2. Constructors
A constructor runs when an object is created and sets its starting values.
`Doctor(String id, String name, String contactNumber, String specialization, BigDecimal consultationFee, boolean available)` - it first calls `super(id, name, contactNumber)` to let `Person` set the shared fields.

## 3. Encapsulation
Fields are `private`; other classes use getters/setters.
`Person` has `private String name` with `getName()`/`setName()`. The ID has only a getter, so it can never change. In `Appointment`, status can only change through `markCompleted()` and `cancel()`, which refuse illegal changes (a cancelled appointment cannot be completed). `Billing` derives its payment status from `amountPaid`, so it cannot become inconsistent.

## 4. Inheritance
A child class reuses a parent class (`extends`).
`Patient extends Person` and `Doctor extends Person`. Both get `id`, `name`, `contactNumber` and their getters/setters for free, and add their own fields (age, address / specialization, fee).

## 5. Abstraction
`Person` is `abstract`: it describes "a person" in general and cannot be created with `new`. It declares abstract methods `getRole()` and `displayDetails()` that every child must implement. Also, each manager hides its details behind simple methods like `bookAppointment(...)`.

## 6. Polymorphism and Method Overriding
`displayDetails()` is declared in `Person` and **overridden** in `Patient` and `Doctor` (marked `@Override`). A `Person` reference calls the right version at run time:
```java
Person p = new Doctor(...);
p.displayDetails();   // runs Doctor's version
```
`getRole()` is overridden the same way ("Patient" / "Doctor"). This is tested in `HospitalTests` ("Polymorphism" section).

## 7. ArrayList (collections)
Each manager stores objects in an `ArrayList`: `ArrayList<Patient> patients`, `ArrayList<Doctor> doctors`, `ArrayList<Appointment> appointments`, `ArrayList<Billing> bills`. Methods like `add`, `remove`, `removeIf`, `size` and enhanced `for` loops are used for add/search/delete.

## 8. Loops
- `while (true)` menu loops in every `runMenu()` (repeat until "Return").
- Input loops in `InputHelper` repeat until the input is valid.
- `for-each` loops search the lists (`findById`, `searchByName`).

## 9. Conditional Statements
`if/else` and `switch` are used for menu choices (`switch (choice)`), validation, and business rules, e.g. in `AppointmentManager.bookAppointment()` (past date, double booking, capacity) and `Billing.getPaymentStatus()`.

## 10. Exception Handling
`HospitalException` (custom, `extends Exception`) is **thrown** by managers when a rule is broken and **caught** in each `runMenu()` with `try { ... } catch (HospitalException e) { UI.error(e.getMessage()); }`. The program therefore never crashes on bad input. Other places: `NumberFormatException` handled in `InputHelper.readChoice`, `IOException` in `DataManager`, and `DateTimeParseException` in `Validator`.

## 11. Object Relationships
- **Association:** an `Appointment` holds a `Patient` and a `Doctor` object.
- **Aggregation:** a manager holds many objects in a list.
- **Composition:** `HospitalManagementSystem` creates and owns its managers.
- **Reference by ID:** `Billing` stores `patientId` and `appointmentId`.

## 12. Other Java features worth mentioning
- `enum` (`Appointment.Status`, `Billing.PaymentStatus`) for fixed sets of values.
- `LocalDate` / `LocalTime` for dates and times.
- `BigDecimal` for money (exact decimal arithmetic).
- File handling with `java.nio.file.Files` (CSV persistence).
- Static utility classes (`UI`, `Validator`).

## Likely viva questions
**Why is `Person` abstract?** A generic "person" is never registered by itself - only patients and doctors. Abstract prevents misuse and forces children to implement `displayDetails()`.
**Where is polymorphism?** `displayDetails()`/`getRole()` overridden in `Patient` and `Doctor`.
**Why BigDecimal?** `double` gives rounding errors (0.1 + 0.2 = 0.30000000000000004); money must be exact.
**What happens on invalid input?** Validation loops ask again; rule violations throw `HospitalException`, which is caught and shown as `[ERROR]`.
**How is data kept after closing?** `DataManager` writes four CSV files after every change and reads them at start-up.
