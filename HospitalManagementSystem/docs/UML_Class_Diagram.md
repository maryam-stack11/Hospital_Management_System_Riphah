# UML Class Diagram

```mermaid
classDiagram
    direction TB

    class Person {
        <<abstract>>
        -String id
        -String name
        -String contactNumber
        +getId() String
        +getName() String
        +setName(String)
        +getContactNumber() String
        +setContactNumber(String)
        +getRole()* String
        +displayDetails()* void
        #printCommonDetails() void
    }
    class Patient {
        -int age
        -String gender
        -String address
        -String medicalCondition
        +getAgeGroup() String
        +getRole() String
        +displayDetails() void
    }
    class Doctor {
        -String specialization
        -BigDecimal consultationFee
        -boolean available
        +MAX_DAILY_APPOINTMENTS int
        +getAvailabilityLabel() String
        +getRole() String
        +displayDetails() void
    }
    class Appointment {
        -String id
        -LocalDate date
        -LocalTime time
        -Status status
        +isActive() boolean
        +markCompleted() void
        +cancel() void
        +displayDetails() void
    }
    class Status {
        <<enumeration>>
        SCHEDULED
        COMPLETED
        CANCELLED
    }
    class Billing {
        -String id
        -String patientId
        -String appointmentId
        -BigDecimal consultationFee
        -BigDecimal additionalCharges
        -BigDecimal amountPaid
        +getTotalAmount() BigDecimal
        +getBalance() BigDecimal
        +getPaymentStatus() PaymentStatus
        +markPaid() void
        +markUnpaid() void
        +recordPartialPayment(BigDecimal) void
    }
    class PaymentStatus {
        <<enumeration>>
        PAID
        UNPAID
        PARTIALLY_PAID
    }
    class PatientManager {
        -ArrayList~Patient~ patients
        +registerPatient() Patient
        +findById(String) Patient
        +searchByName(String) ArrayList
        +updatePatient() void
        +deletePatient(String, boolean) void
        +runMenu() void
    }
    class DoctorManager {
        -ArrayList~Doctor~ doctors
        +addDoctor() Doctor
        +findById(String) Doctor
        +searchBySpecialization(String) ArrayList
        +updateDoctor() void
        +deleteDoctor(String, boolean) void
        +runMenu() void
    }
    class AppointmentManager {
        -ArrayList~Appointment~ appointments
        +bookAppointment() Appointment
        +cancelAppointment(String) void
        +completeAppointment(String) void
        +getAvailability(Doctor, LocalDate) String
        +runMenu() void
    }
    class BillingManager {
        -ArrayList~Billing~ bills
        +generateBill(String, BigDecimal) Billing
        +markPaid(String) void
        +markPartiallyPaid(String, BigDecimal) void
        +getRevenueFromPaidBills() BigDecimal
        +getOutstandingBalance() BigDecimal
        +runMenu() void
    }
    class DataManager {
        +loadAll() void
        +saveAll() boolean
    }
    class HospitalManagementSystem {
        +start() void
        -showStatistics() void
    }
    class Main {
        +main(String[]) void
    }
    class InputHelper
    class UI
    class Validator
    class HospitalException

    Person <|-- Patient : extends
    Person <|-- Doctor : extends
    Appointment "*" --> "1" Patient : patient
    Appointment "*" --> "1" Doctor : doctor
    Appointment --> Status
    Billing "0..1" --> "1" Appointment : refers to (by ID)
    Billing --> PaymentStatus

    PatientManager o-- "*" Patient
    DoctorManager o-- "*" Doctor
    AppointmentManager o-- "*" Appointment
    BillingManager o-- "*" Billing
    AppointmentManager --> PatientManager : uses
    AppointmentManager --> DoctorManager : uses
    PatientManager --> AppointmentManager : checks active
    DoctorManager --> AppointmentManager : checks active
    BillingManager --> AppointmentManager : uses

    HospitalManagementSystem *-- PatientManager
    HospitalManagementSystem *-- DoctorManager
    HospitalManagementSystem *-- AppointmentManager
    HospitalManagementSystem *-- BillingManager
    HospitalManagementSystem *-- DataManager
    HospitalManagementSystem --> InputHelper
    Main --> HospitalManagementSystem
    DataManager ..> PatientManager : load/save
    DataManager ..> DoctorManager : load/save
    DataManager ..> AppointmentManager : load/save
    DataManager ..> BillingManager : load/save
    PatientManager ..> HospitalException : throws
    BillingManager ..> HospitalException : throws
    InputHelper ..> Validator
    PatientManager ..> UI
```

**Legend:** `<|--` inheritance, `o--` aggregation (manager holds an ArrayList of objects), `*--` composition (the system creates and owns its managers), `-->` association/uses, `..>` dependency. Helper classes `UI`, `Validator`, `InputHelper` and `HospitalException` are used throughout; only a few links are drawn to keep the diagram readable.
