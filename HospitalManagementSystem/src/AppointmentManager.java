import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages Appointment objects. Enforces the booking rules (patient/doctor must exist,
 * no double booking, no past dates). Holds references to the Patient and Doctor managers.
 */
public class AppointmentManager {
    private final ArrayList<Appointment> appointments = new ArrayList<>();
    private final PatientManager patients;
    private final DoctorManager doctors;
    private final InputHelper input;
    private final Runnable onChange;

    public AppointmentManager(PatientManager patients, DoctorManager doctors,
                              InputHelper input, Runnable onChange) {
        this.patients = patients;
        this.doctors = doctors;
        this.input = input;
        this.onChange = onChange;
    }

    // ---------------------------------------------------------------- logic

    public List<Appointment> getAllAppointments() { return Collections.unmodifiableList(appointments); }

    public int getCount() { return appointments.size(); }

    public int countByStatus(Appointment.Status status) {
        int n = 0;
        for (Appointment a : appointments) {
            if (a.getStatus() == status) n++;
        }
        return n;
    }

    private String nextId() {
        int max = 0;
        for (Appointment a : appointments) max = Math.max(max, Validator.idNumber(a.getId()));
        return String.format("A%03d", max + 1);
    }

    public Appointment bookAppointment(String patientId, String doctorId, LocalDate date, LocalTime time)
            throws HospitalException {
        Patient patient = patients.findById(patientId);
        if (patient == null) throw new HospitalException("Patient " + patientId + " does not exist. Register the patient first.");
        Doctor doctor = doctors.findById(doctorId);
        if (doctor == null) throw new HospitalException("Doctor " + doctorId + " does not exist.");
        if (!doctor.isAvailable()) throw new HospitalException(doctor.getName() + " is currently unavailable.");
        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new HospitalException("Appointments cannot be booked in the past.");
        }
        for (Appointment a : appointments) {
            if (!a.isActive() || !a.isAt(date, time)) continue;
            if (a.getDoctor() == doctor) {
                throw new HospitalException("Doctor " + doctor.getId() + " already has a scheduled appointment on "
                        + date + " at " + time + ".");
            }
            if (a.getPatient() == patient) {
                throw new HospitalException("Patient " + patient.getId() + " already has a scheduled appointment on "
                        + date + " at " + time + ".");
            }
        }
        if (countScheduledOn(doctor, date) >= Doctor.MAX_DAILY_APPOINTMENTS) {
            throw new HospitalException("Doctor " + doctor.getId() + " is fully booked on " + date + ".");
        }
        Appointment appt = new Appointment(nextId(), patient, doctor, date, time, Appointment.Status.SCHEDULED);
        appointments.add(appt);
        onChange.run();
        return appt;
    }

    public void addLoadedAppointment(Appointment a) throws HospitalException {
        if (!Validator.isValidId(a.getId(), "A")) throw new HospitalException("Bad appointment ID: " + a.getId());
        if (findById(a.getId()) != null) throw new HospitalException("Duplicate appointment ID: " + a.getId());
        appointments.add(a);
    }

    public Appointment findById(String id) {
        for (Appointment a : appointments) {
            if (a.getId().equalsIgnoreCase(id)) return a;
        }
        return null;
    }

    public ArrayList<Appointment> findByPatient(String patientId) {
        ArrayList<Appointment> list = new ArrayList<>();
        for (Appointment a : appointments) {
            if (a.getPatient().getId().equalsIgnoreCase(patientId)) list.add(a);
        }
        return list;
    }

    public ArrayList<Appointment> findByDoctor(String doctorId) {
        ArrayList<Appointment> list = new ArrayList<>();
        for (Appointment a : appointments) {
            if (a.getDoctor().getId().equalsIgnoreCase(doctorId)) list.add(a);
        }
        return list;
    }

    private Appointment require(String id) throws HospitalException {
        Appointment a = findById(id);
        if (a == null) throw new HospitalException("No appointment found with ID " + id + ".");
        return a;
    }

    public void completeAppointment(String id) throws HospitalException {
        require(id).markCompleted();
        onChange.run();
    }

    public void cancelAppointment(String id) throws HospitalException {
        require(id).cancel();
        onChange.run();
    }

    public void updateSchedule(String id, LocalDate date, LocalTime time) throws HospitalException {
        Appointment appointment = require(id);
        if (!appointment.isActive()) {
            throw new HospitalException("Only Scheduled appointments can be edited.");
        }
        if (date == null || time == null || LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new HospitalException("Appointments cannot be booked in the past.");
        }
        Doctor doctor = appointment.getDoctor();
        if (!doctor.isAvailable()) throw new HospitalException(doctor.getName() + " is currently unavailable.");
        int scheduledForDoctor = 0;
        for (Appointment other : appointments) {
            if (other == appointment || !other.isActive()) continue;
            if (other.isAt(date, time)) {
                if (other.getDoctor() == doctor) {
                    throw new HospitalException("Doctor " + doctor.getId()
                            + " already has a scheduled appointment on " + date + " at " + time + ".");
                }
                if (other.getPatient() == appointment.getPatient()) {
                    throw new HospitalException("Patient " + appointment.getPatient().getId()
                            + " already has a scheduled appointment on " + date + " at " + time + ".");
                }
            }
            if (other.getDoctor() == doctor && other.getDate().equals(date)) scheduledForDoctor++;
        }
        if (scheduledForDoctor >= Doctor.MAX_DAILY_APPOINTMENTS) {
            throw new HospitalException("Doctor " + doctor.getId() + " is fully booked on " + date + ".");
        }
        appointment.updateSchedule(date, time);
        onChange.run();
    }

    public void deleteCancelledAppointment(String id) throws HospitalException {
        Appointment appointment = require(id);
        if (appointment.getStatus() != Appointment.Status.CANCELLED) {
            throw new HospitalException("Only cancelled appointments can be deleted. Cancel scheduled appointments first; completed records are retained.");
        }
        appointments.remove(appointment);
        onChange.run();
    }

    // ---- helpers used by Patient/Doctor managers and availability logic

    public int countActiveForPatient(String patientId) {
        int n = 0;
        for (Appointment a : findByPatient(patientId)) if (a.isActive()) n++;
        return n;
    }

    public int countActiveForDoctor(String doctorId) {
        int n = 0;
        for (Appointment a : findByDoctor(doctorId)) if (a.isActive()) n++;
        return n;
    }

    public boolean hasCompletedForPatient(String patientId) {
        for (Appointment a : findByPatient(patientId)) {
            if (a.getStatus() == Appointment.Status.COMPLETED) return true;
        }
        return false;
    }

    public boolean hasCompletedForDoctor(String doctorId) {
        for (Appointment a : findByDoctor(doctorId)) {
            if (a.getStatus() == Appointment.Status.COMPLETED) return true;
        }
        return false;
    }

    public void cancelActiveForPatient(String patientId) throws HospitalException {
        for (Appointment a : findByPatient(patientId)) if (a.isActive()) a.cancel();
    }

    public void cancelActiveForDoctor(String doctorId) throws HospitalException {
        for (Appointment a : findByDoctor(doctorId)) if (a.isActive()) a.cancel();
    }

    public void removeCancelledForPatient(String patientId) {
        appointments.removeIf(a -> a.getPatient().getId().equalsIgnoreCase(patientId)
                && a.getStatus() == Appointment.Status.CANCELLED);
    }

    public void removeCancelledForDoctor(String doctorId) {
        appointments.removeIf(a -> a.getDoctor().getId().equalsIgnoreCase(doctorId)
                && a.getStatus() == Appointment.Status.CANCELLED);
    }

    public int countScheduledOn(Doctor doctor, LocalDate date) {
        int n = 0;
        for (Appointment a : appointments) {
            if (a.isActive() && a.getDoctor() == doctor && a.getDate().equals(date)) n++;
        }
        return n;
    }

    /** Availability changes automatically as appointments are booked or cancelled. */
    public String getAvailability(Doctor doctor, LocalDate date) {
        if (!doctor.isAvailable()) return "Unavailable";
        int booked = countScheduledOn(doctor, date);
        if (booked >= Doctor.MAX_DAILY_APPOINTMENTS) return "Fully Booked";
        return "Available (" + (Doctor.MAX_DAILY_APPOINTMENTS - booked) + " slots left)";
    }

    public void printScheduleFor(Doctor doctor) {
        UI.heading("SCHEDULE - " + doctor.getName());
        ArrayList<Appointment> list = new ArrayList<>();
        for (Appointment a : findByDoctor(doctor.getId())) if (a.isActive()) list.add(a);
        if (list.isEmpty()) {
            UI.info("No scheduled appointments for this doctor.");
            return;
        }
        printTable(list);
    }

    // ----------------------------------------------------------------- menu

    public void runMenu() {
        while (true) {
            UI.menu("APPOINTMENT MANAGEMENT", "Book New Appointment", "View All Appointments",
                    "Search Appointment by ID", "View Appointments by Patient", "View Appointments by Doctor",
                    "Update Appointment Status", "Cancel Appointment", "Return to Main Menu");
            int choice = input.readChoice(1, 8);
            try {
                switch (choice) {
                    case 1 -> bookUI();
                    case 2 -> viewAllUI();
                    case 3 -> searchByIdUI();
                    case 4 -> byPatientUI();
                    case 5 -> byDoctorUI();
                    case 6 -> updateStatusUI();
                    case 7 -> cancelUI();
                    default -> { return; }
                }
            } catch (HospitalException e) {
                UI.error(e.getMessage());
            }
        }
    }

    private void printTable(List<Appointment> list) {
        System.out.println(Appointment.tableHeader());
        UI.thinLine();
        for (Appointment a : list) System.out.println(a.toTableRow());
        UI.thinLine();
        System.out.println("Total: " + list.size() + " appointment(s)");
    }

    private void bookUI() throws HospitalException {
        UI.heading("BOOK NEW APPOINTMENT");
        String pid = input.readId("Patient ID");
        Patient p = patients.findById(pid);
        if (p == null) throw new HospitalException("Patient " + pid + " does not exist. Register the patient first.");
        System.out.println("Patient: " + p.getName());
        System.out.println("\nDoctors:");
        System.out.println(Doctor.tableHeader());
        for (Doctor d : doctors.getAllDoctors()) System.out.println(d.toTableRow());
        String did = input.readId("Doctor ID");
        LocalDate date = input.readDate("Appointment date");
        LocalTime time = input.readTime("Appointment time");
        Appointment a = bookAppointment(pid, did, date, time);
        UI.success("Appointment booked.");
        a.displayDetails();
    }

    private void viewAllUI() {
        UI.heading("ALL APPOINTMENTS");
        if (appointments.isEmpty()) UI.info("No appointments booked yet.");
        else printTable(appointments);
    }

    private void searchByIdUI() {
        UI.heading("SEARCH APPOINTMENT BY ID");
        String id = input.readId("Enter appointment ID (e.g. A001)");
        Appointment a = findById(id);
        if (a == null) UI.error("No appointment found with ID " + id + ".");
        else a.displayDetails();
    }

    private void byPatientUI() {
        UI.heading("APPOINTMENTS BY PATIENT");
        String pid = input.readId("Patient ID");
        if (patients.findById(pid) == null) {
            UI.error("No patient found with ID " + pid + ".");
            return;
        }
        ArrayList<Appointment> list = findByPatient(pid);
        if (list.isEmpty()) UI.info("This patient has no appointments.");
        else printTable(list);
    }

    private void byDoctorUI() {
        UI.heading("APPOINTMENTS BY DOCTOR");
        String did = input.readId("Doctor ID");
        if (doctors.findById(did) == null) {
            UI.error("No doctor found with ID " + did + ".");
            return;
        }
        ArrayList<Appointment> list = findByDoctor(did);
        if (list.isEmpty()) UI.info("This doctor has no appointments.");
        else printTable(list);
    }

    private void updateStatusUI() throws HospitalException {
        UI.heading("UPDATE APPOINTMENT STATUS");
        Appointment a = require(input.readId("Appointment ID"));
        a.displayDetails();
        System.out.println("  [1] Completed\n  [2] Cancelled\n  [3] Back");
        int c = input.readChoice(1, 3);
        if (c == 1) {
            completeAppointment(a.getId());
            UI.success("Appointment " + a.getId() + " marked as Completed.");
        } else if (c == 2) {
            cancelAppointment(a.getId());
            UI.success("Appointment " + a.getId() + " cancelled.");
        }
    }

    private void cancelUI() throws HospitalException {
        UI.heading("CANCEL APPOINTMENT");
        Appointment a = require(input.readId("Appointment ID"));
        a.displayDetails();
        if (!input.confirm("Cancel this appointment?")) {
            UI.info("Cancellation aborted. No changes made.");
            return;
        }
        cancelAppointment(a.getId());
        UI.success("Appointment " + a.getId() + " cancelled. The time slot is free again.");
    }
}
