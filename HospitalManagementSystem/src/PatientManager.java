import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Manages all Patient objects in an ArrayList: add, search, update, delete and the Patient menu. */
public class PatientManager {
    private final ArrayList<Patient> patients = new ArrayList<>();
    private final InputHelper input;
    private final Runnable onChange;          // called after every change so data is saved
    private AppointmentManager appointments;  // set after construction (managers depend on each other)

    public PatientManager(InputHelper input, Runnable onChange) {
        this.input = input;
        this.onChange = onChange;
    }

    public void setAppointmentManager(AppointmentManager appointments) { this.appointments = appointments; }

    // ---------------------------------------------------------------- logic

    public List<Patient> getAllPatients() { return Collections.unmodifiableList(patients); }

    public int getPatientCount() { return patients.size(); }

    private void validate(String name, int age, String gender, String contact, String address,
                          String condition) throws HospitalException {
        if (!Validator.isValidName(name)) throw new HospitalException("Invalid patient name.");
        if (!Validator.isValidAge(age)) throw new HospitalException("Invalid age. Must be 0-" + Validator.MAX_AGE + ".");
        if (Validator.normalizeGender(gender) == null) throw new HospitalException("Invalid gender.");
        if (!Validator.isValidPhone(contact)) throw new HospitalException("Invalid contact number.");
        if (!Validator.isValidText(address)) throw new HospitalException("Invalid address (3-100 characters).");
        if (!Validator.isValidText(condition)) throw new HospitalException("Invalid medical condition (3-100 characters).");
    }

    private String nextId() {
        int max = 0;
        for (Patient p : patients) max = Math.max(max, Validator.idNumber(p.getId()));
        return String.format("P%03d", max + 1);
    }

    public Patient registerPatient(String name, int age, String gender, String contact,
                                   String address, String condition) throws HospitalException {
        validate(name, age, gender, contact, address, condition);
        Patient p = new Patient(nextId(), name.trim(), Validator.cleanPhone(contact), age,
                Validator.normalizeGender(gender), address.trim(), condition.trim());
        patients.add(p);
        onChange.run();
        return p;
    }

    /** Used when loading from file: validates the record and rejects duplicate IDs. */
    public void addLoadedPatient(Patient p) throws HospitalException {
        if (!Validator.isValidId(p.getId(), "P")) throw new HospitalException("Bad patient ID: " + p.getId());
        validate(p.getName(), p.getAge(), p.getGender(), p.getContactNumber(), p.getAddress(), p.getMedicalCondition());
        if (findById(p.getId()) != null) throw new HospitalException("Duplicate patient ID: " + p.getId());
        patients.add(p);
    }

    public Patient findById(String id) {
        for (Patient p : patients) {
            if (p.getId().equalsIgnoreCase(id)) return p;
        }
        return null;
    }

    public ArrayList<Patient> searchByName(String text) {
        ArrayList<Patient> result = new ArrayList<>();
        for (Patient p : patients) {
            if (p.getName().toLowerCase().contains(text.toLowerCase())) result.add(p);
        }
        return result;
    }

    public void updatePatient(Patient p, String name, int age, String gender, String contact,
                              String address, String condition) throws HospitalException {
        validate(name, age, gender, contact, address, condition);
        p.setName(name.trim());
        p.setAge(age);
        p.setGender(Validator.normalizeGender(gender));
        p.setContactNumber(Validator.cleanPhone(contact));
        p.setAddress(address.trim());
        p.setMedicalCondition(condition.trim());
        onChange.run();
    }

    /**
     * Deletes a patient. Active (scheduled) appointments must be cancelled first
     * (cancelActive = true). Patients with completed visits/bills are kept for records.
     */
    public void deletePatient(String id, boolean cancelActive) throws HospitalException {
        Patient p = findById(id);
        if (p == null) throw new HospitalException("No patient found with ID " + id + ".");
        if (appointments.hasCompletedForPatient(p.getId())) {
            throw new HospitalException("Patient " + p.getId() + " has completed appointments/billing history "
                    + "and cannot be deleted (records must be kept).");
        }
        int active = appointments.countActiveForPatient(p.getId());
        if (active > 0 && !cancelActive) {
            throw new HospitalException("Patient has " + active + " active appointment(s). Cancel them first.");
        }
        appointments.cancelActiveForPatient(p.getId());
        appointments.removeCancelledForPatient(p.getId());
        patients.remove(p);
        onChange.run();
    }

    // ----------------------------------------------------------------- menu

    public void runMenu() {
        while (true) {
            UI.menu("PATIENT MANAGEMENT", "Register New Patient", "View All Patients", "Search Patient by ID",
                    "Search Patient by Name", "Update Patient Information", "Delete Patient Record",
                    "Return to Main Menu");
            int choice = input.readChoice(1, 7);
            try {
                switch (choice) {
                    case 1 -> registerUI();
                    case 2 -> viewAllUI();
                    case 3 -> searchByIdUI();
                    case 4 -> searchByNameUI();
                    case 5 -> updateUI();
                    case 6 -> deleteUI();
                    default -> { return; }
                }
            } catch (HospitalException e) {
                UI.error(e.getMessage());
            }
        }
    }

    private void registerUI() throws HospitalException {
        UI.heading("REGISTER NEW PATIENT");
        String name = input.readName("Full name", null);
        int age = input.readAge(null);
        String gender = input.readGender(null);
        String contact = input.readPhone(null);
        String address = input.readText("Address", null);
        String condition = input.readText("Medical condition / reason for visit", null);
        Patient p = registerPatient(name, age, gender, contact, address, condition);
        UI.success("Patient registered with ID " + p.getId() + ".");
        p.displayDetails();
    }

    private void printTable(List<Patient> list) {
        System.out.println(Patient.tableHeader());
        UI.thinLine();
        for (Patient p : list) System.out.println(p.toTableRow());
        UI.thinLine();
        System.out.println("Total: " + list.size() + " patient(s)");
    }

    private void viewAllUI() {
        UI.heading("ALL PATIENTS");
        if (patients.isEmpty()) {
            UI.info("No patients registered yet.");
            return;
        }
        printTable(patients);
    }

    private void searchByIdUI() {
        UI.heading("SEARCH PATIENT BY ID");
        String id = input.readId("Enter patient ID (e.g. P001)");
        Patient p = findById(id);
        if (p == null) UI.error("No patient found with ID " + id + ".");
        else p.displayDetails();
    }

    private void searchByNameUI() {
        UI.heading("SEARCH PATIENT BY NAME");
        String text = input.readLine("Enter name (or part of it): ");
        if (text.isEmpty()) {
            UI.error("Search text cannot be empty.");
            return;
        }
        ArrayList<Patient> found = searchByName(text);
        if (found.isEmpty()) UI.info("No patients match \"" + text + "\".");
        else printTable(found);
    }

    private void updateUI() throws HospitalException {
        UI.heading("UPDATE PATIENT INFORMATION");
        Patient p = findById(input.readId("Enter patient ID"));
        if (p == null) throw new HospitalException("Patient not found.");
        p.displayDetails();
        UI.info("Press Enter to keep the current value of any field.");
        String name = input.readName("Full name", p.getName());
        int age = input.readAge(String.valueOf(p.getAge()));
        String gender = input.readGender(p.getGender());
        String contact = input.readPhone(p.getContactNumber());
        String address = input.readText("Address", p.getAddress());
        String condition = input.readText("Medical condition", p.getMedicalCondition());
        updatePatient(p, name, age, gender, contact, address, condition);
        UI.success("Patient " + p.getId() + " updated.");
        p.displayDetails();
    }

    private void deleteUI() throws HospitalException {
        UI.heading("DELETE PATIENT RECORD");
        Patient p = findById(input.readId("Enter patient ID"));
        if (p == null) throw new HospitalException("Patient not found.");
        if (appointments.hasCompletedForPatient(p.getId())) {
            throw new HospitalException("Patient " + p.getId() + " has completed appointments/billing history "
                    + "and cannot be deleted (records must be kept).");
        }
        p.displayDetails();
        int active = appointments.countActiveForPatient(p.getId());
        boolean cancelActive = false;
        if (active > 0) {
            UI.warn("This patient has " + active + " active appointment(s).");
            cancelActive = input.confirm("Cancel those appointments and delete the patient?");
        } else {
            cancelActive = input.confirm("Are you sure you want to delete this patient?");
        }
        if (!cancelActive) {
            UI.info("Deletion aborted. No changes made.");
            return;
        }
        deletePatient(p.getId(), true);
        UI.success("Patient " + p.getId() + " deleted.");
    }
}
