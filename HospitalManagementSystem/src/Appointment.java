import java.time.LocalDate;
import java.time.LocalTime;

/**
 * An Appointment links one Patient and one Doctor (object relationship / association).
 * Status changes go through methods that enforce the rules (encapsulation).
 */
public class Appointment {

    public enum Status {
        SCHEDULED("Scheduled"), COMPLETED("Completed"), CANCELLED("Cancelled");

        private final String label;

        Status(String label) { this.label = label; }

        public String getLabel() { return label; }

        public static Status fromLabel(String text) {
            for (Status s : values()) {
                if (s.label.equalsIgnoreCase(text)) return s;
            }
            throw new IllegalArgumentException("Unknown status: " + text);
        }
    }

    private final String id;
    private final Patient patient;
    private final Doctor doctor;
    private LocalDate date;
    private LocalTime time;
    private Status status;

    public Appointment(String id, Patient patient, Doctor doctor,
                       LocalDate date, LocalTime time, Status status) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getId() { return id; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public Status getStatus() { return status; }

    /** Only scheduled appointments are "active"; cancelled/completed ones never block a time slot. */
    public boolean isActive() { return status == Status.SCHEDULED; }

    public boolean isAt(LocalDate d, LocalTime t) { return date.equals(d) && time.equals(t); }

    void updateSchedule(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    public void markCompleted() throws HospitalException {
        if (status != Status.SCHEDULED) {
            throw new HospitalException("Only a Scheduled appointment can be completed (current status: "
                    + status.getLabel() + ").");
        }
        status = Status.COMPLETED;
    }

    public void cancel() throws HospitalException {
        if (status != Status.SCHEDULED) {
            throw new HospitalException("Only a Scheduled appointment can be cancelled (current status: "
                    + status.getLabel() + ").");
        }
        status = Status.CANCELLED;
    }

    public void displayDetails() {
        UI.thinLine();
        UI.field("Appointment ID", id);
        UI.field("Patient", patient.getId() + " - " + patient.getName());
        UI.field("Doctor", doctor.getId() + " - " + doctor.getName() + " (" + doctor.getSpecialization() + ")");
        UI.field("Date", date.toString());
        UI.field("Time", time.toString());
        UI.field("Status", status.getLabel());
        UI.thinLine();
    }

    public static String tableHeader() {
        return String.format("%-6s %-24s %-24s %-11s %-6s %-10s",
                "ID", "Patient", "Doctor", "Date", "Time", "Status");
    }

    public String toTableRow() {
        return String.format("%-6s %s %s %-11s %-6s %-10s", id,
                UI.fit(patient.getId() + " " + patient.getName(), 24),
                UI.fit(doctor.getId() + " " + doctor.getName(), 24),
                date, time, status.getLabel());
    }
}
