import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Manages all Doctor objects in an ArrayList and the Doctor menu. */
public class DoctorManager {
    public static final String[] SPECIALIZATIONS = {
        "Cardiologist", "Neurologist", "Dermatologist",
        "General Physician", "Pediatrician", "Orthopedic Surgeon",
        "Gynecologist", "Dentist"
    };

    private final ArrayList<Doctor> doctors = new ArrayList<>();
    private final InputHelper input;
    private final Runnable onChange;
    private AppointmentManager appointments;

    public DoctorManager(InputHelper input, Runnable onChange) {
        this.input = input;
        this.onChange = onChange;
    }

    public void setAppointmentManager(AppointmentManager appointments) { this.appointments = appointments; }

    // ---------------------------------------------------------------- logic

    public List<Doctor> getAllDoctors() { return Collections.unmodifiableList(doctors); }

    public int getDoctorCount() { return doctors.size(); }

    private void validate(String name, String specialization, String contact, BigDecimal fee)
            throws HospitalException {
        if (!Validator.isValidName(name)) throw new HospitalException("Invalid doctor name.");
        if (!Validator.isValidName(specialization)) throw new HospitalException("Invalid specialization.");
        if (!Validator.isValidPhone(contact)) throw new HospitalException("Invalid contact number.");
        if (fee == null || fee.signum() <= 0 || fee.compareTo(Validator.MAX_MONEY) > 0) {
            throw new HospitalException("Consultation fee must be greater than 0.");
        }
    }

    private String nextId() {
        int max = 0;
        for (Doctor d : doctors) max = Math.max(max, Validator.idNumber(d.getId()));
        return String.format("D%03d", max + 1);
    }

    public Doctor addDoctor(String name, String specialization, String contact, BigDecimal fee)
            throws HospitalException {
        validate(name, specialization, contact, fee);
        Doctor d = new Doctor(nextId(), name.trim(), Validator.cleanPhone(contact), specialization.trim(),
                fee.setScale(2, java.math.RoundingMode.HALF_UP), true);
        doctors.add(d);
        onChange.run();
        return d;
    }

    public void addLoadedDoctor(Doctor d) throws HospitalException {
        if (!Validator.isValidId(d.getId(), "D")) throw new HospitalException("Bad doctor ID: " + d.getId());
        validate(d.getName(), d.getSpecialization(), d.getContactNumber(), d.getConsultationFee());
        if (findById(d.getId()) != null) throw new HospitalException("Duplicate doctor ID: " + d.getId());
        doctors.add(d);
    }

    public Doctor findById(String id) {
        for (Doctor d : doctors) {
            if (d.getId().equalsIgnoreCase(id)) return d;
        }
        return null;
    }

    public ArrayList<Doctor> searchBySpecialization(String text) {
        ArrayList<Doctor> result = new ArrayList<>();
        for (Doctor d : doctors) {
            if (d.getSpecialization().toLowerCase().contains(text.toLowerCase())) result.add(d);
        }
        return result;
    }

    public void updateDoctor(Doctor d, String name, String specialization, String contact,
                             BigDecimal fee, boolean available) throws HospitalException {
        validate(name, specialization, contact, fee);
        d.setName(name.trim());
        d.setSpecialization(specialization.trim());
        d.setContactNumber(Validator.cleanPhone(contact));
        d.setConsultationFee(fee.setScale(2, java.math.RoundingMode.HALF_UP));
        d.setAvailable(available);
        onChange.run();
    }

    public void deleteDoctor(String id, boolean cancelActive) throws HospitalException {
        Doctor d = findById(id);
        if (d == null) throw new HospitalException("No doctor found with ID " + id + ".");
        if (appointments.hasCompletedForDoctor(d.getId())) {
            throw new HospitalException("Doctor " + d.getId() + " has completed appointments/billing history "
                    + "and cannot be deleted (records must be kept). Mark the doctor Unavailable instead.");
        }
        int active = appointments.countActiveForDoctor(d.getId());
        if (active > 0 && !cancelActive) {
            throw new HospitalException("Doctor has " + active + " active appointment(s). Cancel them first.");
        }
        appointments.cancelActiveForDoctor(d.getId());
        appointments.removeCancelledForDoctor(d.getId());
        doctors.remove(d);
        onChange.run();
    }

    // ----------------------------------------------------------------- menu

    public void runMenu() {
        while (true) {
            UI.menu("DOCTOR MANAGEMENT", "Add New Doctor", "View All Doctors", "Search Doctor by ID",
                    "Search Doctor by Specialization", "Update Doctor Information", "Delete Doctor Record",
                    "View Doctor Availability", "Return to Main Menu");
            int choice = input.readChoice(1, 8);
            try {
                switch (choice) {
                    case 1 -> addUI();
                    case 2 -> viewAllUI();
                    case 3 -> searchByIdUI();
                    case 4 -> searchBySpecUI();
                    case 5 -> updateUI();
                    case 6 -> deleteUI();
                    case 7 -> availabilityUI();
                    default -> { return; }
                }
            } catch (HospitalException e) {
                UI.error(e.getMessage());
            }
        }
    }

    private String chooseSpecialization(String current) {
        for (int i = 0; i < SPECIALIZATIONS.length; i++) {
            System.out.printf("  [%d] %s%n", i + 1, SPECIALIZATIONS[i]);
        }
        System.out.printf("  [%d] Other (type your own)%n", SPECIALIZATIONS.length + 1);
        String prompt = "Specialization number" + (current == null ? "" : " [" + current + "] (Enter to keep)") + ": ";
        while (true) {
            String s = input.readLine(prompt);
            if (s.isEmpty() && current != null) return current;
            if (s.matches("\\d+")) {
                int n = Integer.parseInt(s);
                if (n >= 1 && n <= SPECIALIZATIONS.length) return SPECIALIZATIONS[n - 1];
                if (n == SPECIALIZATIONS.length + 1) return input.readName("Specialization name", null);
            }
            UI.error("Enter a number between 1 and " + (SPECIALIZATIONS.length + 1) + ".");
        }
    }

    private void addUI() throws HospitalException {
        UI.heading("ADD NEW DOCTOR");
        String name = input.readName("Full name", null);
        String spec = chooseSpecialization(null);
        String contact = input.readPhone(null);
        BigDecimal fee = input.readMoney("Consultation fee (Rs.)", false, null);
        Doctor d = addDoctor(name, spec, contact, fee);
        UI.success("Doctor added with ID " + d.getId() + ".");
        d.displayDetails();
    }

    private void printTable(List<Doctor> list) {
        System.out.println(Doctor.tableHeader());
        UI.thinLine();
        for (Doctor d : list) System.out.println(d.toTableRow());
        UI.thinLine();
        System.out.println("Total: " + list.size() + " doctor(s)");
    }

    private void viewAllUI() {
        UI.heading("ALL DOCTORS");
        if (doctors.isEmpty()) UI.info("No doctors in the system yet.");
        else printTable(doctors);
    }

    private void searchByIdUI() {
        UI.heading("SEARCH DOCTOR BY ID");
        String id = input.readId("Enter doctor ID (e.g. D001)");
        Doctor d = findById(id);
        if (d == null) UI.error("No doctor found with ID " + id + ".");
        else d.displayDetails();
    }

    private void searchBySpecUI() {
        UI.heading("SEARCH DOCTOR BY SPECIALIZATION");
        String text = input.readLine("Enter specialization (or part of it): ");
        if (text.isEmpty()) {
            UI.error("Search text cannot be empty.");
            return;
        }
        ArrayList<Doctor> found = searchBySpecialization(text);
        if (found.isEmpty()) UI.info("No doctors found for \"" + text + "\".");
        else printTable(found);
    }

    private void updateUI() throws HospitalException {
        UI.heading("UPDATE DOCTOR INFORMATION");
        Doctor d = findById(input.readId("Enter doctor ID"));
        if (d == null) throw new HospitalException("Doctor not found.");
        d.displayDetails();
        UI.info("Press Enter to keep the current value of any field.");
        String name = input.readName("Full name", d.getName());
        String spec = chooseSpecialization(d.getSpecialization());
        String contact = input.readPhone(d.getContactNumber());
        BigDecimal fee = input.readMoney("Consultation fee (Rs.)", false, d.getConsultationFee().toPlainString());
        boolean available = d.isAvailable();
        if (input.confirm("Is the doctor available for appointments (currently "
                + d.getAvailabilityLabel() + ")?")) available = true;
        else available = false;
        updateDoctor(d, name, spec, contact, fee, available);
        UI.success("Doctor " + d.getId() + " updated.");
        d.displayDetails();
    }

    private void deleteUI() throws HospitalException {
        UI.heading("DELETE DOCTOR RECORD");
        Doctor d = findById(input.readId("Enter doctor ID"));
        if (d == null) throw new HospitalException("Doctor not found.");
        if (appointments.hasCompletedForDoctor(d.getId())) {
            throw new HospitalException("Doctor " + d.getId() + " has completed appointments/billing history "
                    + "and cannot be deleted (records must be kept).");
        }
        d.displayDetails();
        int active = appointments.countActiveForDoctor(d.getId());
        boolean proceed;
        if (active > 0) {
            UI.warn("This doctor has " + active + " active appointment(s).");
            proceed = input.confirm("Cancel those appointments and delete the doctor?");
        } else {
            proceed = input.confirm("Are you sure you want to delete this doctor?");
        }
        if (!proceed) {
            UI.info("Deletion aborted. No changes made.");
            return;
        }
        deleteDoctor(d.getId(), true);
        UI.success("Doctor " + d.getId() + " deleted.");
    }

    /** Availability is the manual status combined with today's real bookings. */
    private void availabilityUI() {
        UI.heading("DOCTOR AVAILABILITY");
        if (doctors.isEmpty()) {
            UI.info("No doctors in the system yet.");
            return;
        }
        LocalDate today = LocalDate.now();
        System.out.println("Date checked: " + today + "  (max " + Doctor.MAX_DAILY_APPOINTMENTS + " patients/day)");
        System.out.println(String.format("%-6s %-22s %-20s %-22s %-9s", "ID", "Name", "Specialization",
                "Status today", "Upcoming"));
        UI.thinLine();
        for (Doctor d : doctors) {
            System.out.println(String.format("%-6s %s %s %s %-9d", d.getId(), UI.fit(d.getName(), 22),
                    UI.fit(d.getSpecialization(), 20), UI.fit(appointments.getAvailability(d, today), 22),
                    appointments.countActiveForDoctor(d.getId())));
        }
        UI.thinLine();
        String id = input.readLine("Enter a doctor ID for their schedule (or press Enter to go back): ");
        if (id.isEmpty()) return;
        Doctor d = findById(id);
        if (d == null) {
            UI.error("No doctor found with ID " + id + ".");
            return;
        }
        appointments.printScheduleFor(d);
    }
}
