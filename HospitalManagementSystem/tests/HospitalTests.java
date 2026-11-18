import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Simple self-made test harness (no JUnit needed). Tests the business rules by calling
 * the manager classes directly. Run:  java -cp out HospitalTests
 */
public class HospitalTests {
    static int passed = 0, failed = 0;
    static final LocalDate FUTURE = LocalDate.now().plusDays(30);
    static final LocalTime T10 = LocalTime.of(10, 0);

    static InputHelper emptyInput() { return new InputHelper(new ByteArrayInputStream(new byte[0])); }

    static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("  PASS  " + name); }
        else { failed++; System.out.println("  FAIL  " + name); }
    }

    /** Expects the action to throw HospitalException. */
    interface Action { void run() throws Exception; }
    static void expectError(String name, Action a) {
        try { a.run(); check(name + " (expected rejection)", false); }
        catch (HospitalException e) { check(name, true); }
        catch (Exception e) { check(name + " (wrong exception " + e + ")", false); }
    }

    static class Sys {
        PatientManager pm; DoctorManager dm; AppointmentManager am; BillingManager bm; DataManager data;
        Sys(String dir) {
            data = new DataManager(dir);
            Runnable saver = () -> data.saveAll(pm, dm, am, bm);
            InputHelper in = emptyInput();
            pm = new PatientManager(in, saver);
            dm = new DoctorManager(in, saver);
            am = new AppointmentManager(pm, dm, in, saver);
            bm = new BillingManager(am, pm, in, saver);
            pm.setAppointmentManager(am);
            dm.setAppointmentManager(am);
            data.loadAll(pm, dm, am, bm);
        }
    }

    public static void main(String[] args) throws Exception {
        Path tmp = Files.createTempDirectory("hms_test");
        Sys s = new Sys(tmp.toString());

        System.out.println("== Patients ==");
        Patient p1 = s.pm.registerPatient("Ali Khan", 30, "m", "0300-1234567", "House 5, Street 2, Islamabad", "Fever");
        Patient p2 = s.pm.registerPatient("Sara Ahmed", 8, "Female", "03111234567", "Lahore", "Checkup");
        check("IDs auto-generated and unique", p1.getId().equals("P001") && p2.getId().equals("P002"));
        check("Gender normalised, phone cleaned", p1.getGender().equals("Male") && p1.getContactNumber().equals("03001234567"));
        check("Comma in address kept", p1.getAddress().contains(","));
        check("Age group method", p2.getAgeGroup().equals("Child") && p1.getAgeGroup().equals("Adult"));
        expectError("Reject age 150", () -> s.pm.registerPatient("Bad Age", 150, "Male", "03001234567", "Addr", "Cond"));
        expectError("Reject negative age", () -> s.pm.registerPatient("Bad Age", -1, "Male", "03001234567", "Addr", "Cond"));
        expectError("Reject bad phone", () -> s.pm.registerPatient("Bad Phone", 20, "Male", "123", "Addr", "Cond"));
        expectError("Reject numeric name", () -> s.pm.registerPatient("12345", 20, "Male", "03001234567", "Addr", "Cond"));
        check("Search by ID (case-insens.)", s.pm.findById("p001") == p1);
        check("Search by unknown ID gives null", s.pm.findById("P999") == null);
        check("Search by partial name", s.pm.searchByName("sara").size() == 1 && s.pm.searchByName("zzz").isEmpty());
        s.pm.updatePatient(p1, "Ali Raza Khan", 31, "Male", "03001234567", "New Address", "Cough");
        check("Update patient", p1.getName().equals("Ali Raza Khan") && p1.getAge() == 31);
        expectError("Update with invalid data rejected", () -> s.pm.updatePatient(p1, "Ali", 500, "Male", "03001234567", "Addr", "Cond"));

        System.out.println("== Doctors ==");
        Doctor d1 = s.dm.addDoctor("Dr. Ahmed Raza", "Cardiologist", "03001234501", new BigDecimal("3000"));
        Doctor d2 = s.dm.addDoctor("Dr. Sana Malik", "Neurologist", "03001234502", new BigDecimal("2500.50"));
        check("Doctor IDs generated", d1.getId().equals("D001") && d2.getId().equals("D002"));
        expectError("Reject zero fee", () -> s.dm.addDoctor("Dr. Zero", "Cardiologist", "03001234501", BigDecimal.ZERO));
        check("Search by specialization", s.dm.searchBySpecialization("cardio").size() == 1);
        check("Doctor available by default", d1.isAvailable());

        System.out.println("== Appointments ==");
        Appointment a1 = s.am.bookAppointment("P001", "D001", FUTURE, T10);
        check("Book appointment", a1.getId().equals("A001") && a1.getStatus() == Appointment.Status.SCHEDULED);
        expectError("Unknown patient rejected", () -> s.am.bookAppointment("P999", "D001", FUTURE, T10));
        expectError("Unknown doctor rejected", () -> s.am.bookAppointment("P001", "D999", FUTURE, T10));
        expectError("Doctor double booking rejected", () -> s.am.bookAppointment("P002", "D001", FUTURE, T10));
        expectError("Patient double booking rejected", () -> s.am.bookAppointment("P001", "D002", FUTURE, T10));
        expectError("Past date rejected", () -> s.am.bookAppointment("P002", "D001", LocalDate.now().minusDays(1), T10));
        Appointment other = s.am.bookAppointment("P002", "D001", FUTURE, LocalTime.of(11, 0));
        check("Different time for same doctor allowed", other != null);
        s.am.cancelAppointment("A001");
        check("Cancelled status set", a1.getStatus() == Appointment.Status.CANCELLED);
        check("Cancelled appointment is not active", !a1.isActive() && s.am.countActiveForPatient("P001") == 0);
        Appointment again = s.am.bookAppointment("P002", "D001", FUTURE, T10);
        check("Slot reusable after cancellation", again.getId().equals("A003"));
        expectError("Cannot cancel twice", () -> s.am.cancelAppointment("A001"));
        expectError("Cannot complete a cancelled appointment", () -> s.am.completeAppointment("A001"));
        check("Find by patient / doctor", s.am.findByPatient("P002").size() == 2 && s.am.findByDoctor("D001").size() == 3);

        System.out.println("== Availability ==");
        d2.setAvailable(false);
        expectError("Unavailable doctor cannot be booked", () -> s.am.bookAppointment("P001", "D002", FUTURE, T10));
        check("Availability label: Unavailable", s.am.getAvailability(d2, FUTURE).equals("Unavailable"));
        d2.setAvailable(true);
        for (int i = 0; i < Doctor.MAX_DAILY_APPOINTMENTS - 2; i++) {   // d1 already has 2 scheduled on FUTURE
            s.pm.registerPatient("Filler Patient", 40, "Male", "03001234567", "Address", "Checkup");
        }
        int booked = s.am.countScheduledOn(d1, FUTURE);
        int patientNo = 3;
        while (booked < Doctor.MAX_DAILY_APPOINTMENTS) {
            s.am.bookAppointment(String.format("P%03d", patientNo++), "D001", FUTURE, LocalTime.of(12 + (booked % 8), booked));
            booked++;
        }
        check("Doctor shows Fully Booked at capacity", s.am.getAvailability(d1, FUTURE).equals("Fully Booked"));
        expectError("Daily capacity enforced", () -> s.am.bookAppointment("P001", "D001", FUTURE, LocalTime.of(20, 0)));
        s.am.cancelAppointment("A003");
        check("Availability returns after a cancellation", s.am.getAvailability(d1, FUTURE).startsWith("Available"));

        System.out.println("== Billing ==");
        expectError("Cannot bill a Scheduled appointment", () -> s.bm.generateBill("A002", BigDecimal.ZERO));
        s.am.completeAppointment("A002");
        Billing b1 = s.bm.generateBill("A002", new BigDecimal("0.10"));
        check("Fee taken from doctor record", b1.getConsultationFee().compareTo(new BigDecimal("3000")) == 0);
        check("Total = fee + extra", b1.getTotalAmount().compareTo(new BigDecimal("3000.10")) == 0);
        check("New bill is Unpaid", b1.getPaymentStatus() == Billing.PaymentStatus.UNPAID);
        expectError("Duplicate bill prevented", () -> s.bm.generateBill("A002", BigDecimal.ZERO));
        expectError("Unknown appointment rejected", () -> s.bm.generateBill("A777", BigDecimal.ZERO));
        expectError("Negative charges rejected", () -> s.bm.generateBill("A002", new BigDecimal("-5")));
        expectError("Partial payment 0 rejected", () -> s.bm.markPartiallyPaid("B001", BigDecimal.ZERO));
        expectError("Partial payment >= total rejected", () -> s.bm.markPartiallyPaid("B001", new BigDecimal("3000.10")));
        s.bm.markPartiallyPaid("B001", new BigDecimal("1000"));
        check("Partially Paid + balance", b1.getPaymentStatus() == Billing.PaymentStatus.PARTIALLY_PAID
                && b1.getBalance().compareTo(new BigDecimal("2000.10")) == 0);
        check("Outstanding counts partial", s.bm.getOutstandingBalance().compareTo(new BigDecimal("2000.10")) == 0);
        s.bm.markPaid("B001");
        check("Paid status", b1.getPaymentStatus() == Billing.PaymentStatus.PAID);
        check("Revenue from paid bills", s.bm.getRevenueFromPaidBills().compareTo(new BigDecimal("3000.10")) == 0
                && s.bm.getOutstandingBalance().signum() == 0);
        s.bm.markUnpaid("B001");
        check("Back to Unpaid", b1.getPaymentStatus() == Billing.PaymentStatus.UNPAID);
        check("Billing history by patient", s.bm.findByPatient("P002").size() == 1);

        System.out.println("== Delete rules ==");
        expectError("Patient with completed history cannot be deleted", () -> s.pm.deletePatient("P002", true));
        expectError("Doctor with completed history cannot be deleted", () -> s.dm.deleteDoctor("D001", true));
        expectError("Delete blocked for active appts without cancel", () -> s.pm.deletePatient("P003", false));
        int before = s.pm.getPatientCount();
        s.pm.deletePatient("P003", true);
        check("Patient deleted after cancelling active appointments", s.pm.getPatientCount() == before - 1
                && s.pm.findById("P003") == null && s.am.findByPatient("P003").isEmpty());
        expectError("Delete unknown patient", () -> s.pm.deletePatient("P555", true));
        s.dm.deleteDoctor("D002", false);
        check("Doctor with no appointments deleted", s.dm.findById("D002") == null);

        System.out.println("== Statistics counts ==");
        int sched = s.am.countByStatus(Appointment.Status.SCHEDULED);
        int comp = s.am.countByStatus(Appointment.Status.COMPLETED);
        int canc = s.am.countByStatus(Appointment.Status.CANCELLED);
        check("Status counts add up to total", sched + comp + canc == s.am.getCount() && comp == 1);

        System.out.println("== Persistence ==");
        Sys s2 = new Sys(tmp.toString());
        check("Patients reloaded", s2.pm.getPatientCount() == s.pm.getPatientCount());
        check("Doctors reloaded", s2.dm.getDoctorCount() == s.dm.getDoctorCount());
        check("Appointments reloaded", s2.am.getCount() == s.am.getCount());
        check("Bills reloaded", s2.bm.getCount() == 1);
        check("Updated address reloaded", s2.pm.findById("P001").getAddress().equals("New Address"));
        check("Reloaded appointment keeps status", s2.am.findById("A001").getStatus() == Appointment.Status.CANCELLED);
        check("Reloaded bill amounts exact", s2.bm.findById("B001").getTotalAmount().compareTo(new BigDecimal("3000.10")) == 0);
        Patient commaPatient = s.pm.registerPatient("Comma Test", 22, "Other", "03001234567", "Flat 1, Block B, Karachi", "Cough, cold");
        Sys s3 = new Sys(tmp.toString());
        check("Commas in text round-trip", s3.pm.findById(commaPatient.getId()).getAddress().equals("Flat 1, Block B, Karachi")
                && s3.pm.findById(commaPatient.getId()).getMedicalCondition().equals("Cough, cold"));
        Sys s4 = new Sys(tmp.toString());
        Doctor fresh = s4.dm.addDoctor("Dr. New Doc", "Pediatrician", "03001234509", new BigDecimal("1200"));
        check("ID sequence continues after reload (D002 free after D002 deleted)", fresh.getId().equals("D002"));

        System.out.println("== Corrupted / missing files ==");
        Path bad = Files.createTempDirectory("hms_bad");
        Files.writeString(bad.resolve("patients.csv"), "id,name,age,gender,contact,address,condition\n"
                + "P001,Good Person,25,Male,03001234567,Addr here,Flu\n"
                + "garbage line\n"
                + "P002,Bad Age,abc,Male,03001234567,Addr here,Flu\n"
                + "P001,Duplicate Id,30,Male,03001234567,Addr here,Flu\n"
                + "\n");
        Files.writeString(bad.resolve("appointments.csv"), "id,patientId,doctorId,date,time,status\nA001,P001,D404,2030-01-01,10:00,Scheduled\n");
        Sys s5 = new Sys(bad.toString());
        check("Only the valid record loaded", s5.pm.getPatientCount() == 1);
        check("Corrupted lines counted (4 bad)", s5.data.getSkippedRecords() == 4);
        check("Missing doctors/bills files tolerated", s5.dm.getDoctorCount() == 0 && s5.bm.getCount() == 0);
        Path empty = Files.createTempDirectory("hms_empty");
        Files.writeString(empty.resolve("doctors.csv"), "");
        Sys s6 = new Sys(empty.toString());
        check("Empty file tolerated", s6.dm.getDoctorCount() == 0);
        Sys s7 = new Sys(empty.resolve("does_not_exist").toString());
        check("Missing data folder tolerated", s7.pm.getPatientCount() == 0);

        System.out.println("== Validator ==");
        check("Date parsing", Validator.parseDate("2030-02-28") != null);
        expectError("Invalid date 2030-02-30", () -> Validator.parseDate("2030-02-30"));
        expectError("Invalid date format 15/03/2030", () -> Validator.parseDate("15/03/2030"));
        expectError("Invalid time 25:00", () -> Validator.parseTime("25:00"));
        expectError("Invalid time 9:5", () -> Validator.parseTime("9:5"));
        expectError("Invalid money -5", () -> Validator.parseMoney("-5"));
        expectError("Invalid money abc", () -> Validator.parseMoney("abc"));
        check("BigDecimal exactness 0.10+0.20", Validator.parseMoney("0.10").add(Validator.parseMoney("0.20")).toPlainString().equals("0.30"));

        System.out.println("== Polymorphism ==");
        Person[] people = { p1, d1 };
        check("Person references call overridden methods", people[0].getRole().equals("Patient") && people[1].getRole().equals("Doctor"));

        System.out.println("\nRESULT: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
