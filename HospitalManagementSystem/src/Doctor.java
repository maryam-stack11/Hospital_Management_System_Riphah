import java.math.BigDecimal;

/** INHERITANCE: Doctor extends Person and adds specialization, fee and availability. */
public class Doctor extends Person {
    /** Business rule: a doctor can see at most this many scheduled patients per day. */
    public static final int MAX_DAILY_APPOINTMENTS = 8;

    private String specialization;
    private BigDecimal consultationFee;
    private boolean available;   // manual status (e.g. false when the doctor is on leave)

    public Doctor(String id, String name, String contactNumber,
                  String specialization, BigDecimal consultationFee, boolean available) {
        super(id, name, contactNumber);
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.available = available;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    /** Role-specific method. */
    public String getAvailabilityLabel() { return available ? "Available" : "Unavailable"; }

    @Override
    public String getRole() { return "Doctor"; }

    /** METHOD OVERRIDING: Doctor's own version of displayDetails(). */
    @Override
    public void displayDetails() {
        UI.thinLine();
        printCommonDetails();
        UI.field("Specialization", specialization);
        UI.field("Consultation Fee", UI.money(consultationFee));
        UI.field("Availability", getAvailabilityLabel());
        UI.thinLine();
    }

    public static String tableHeader() {
        return String.format("%-6s %-22s %-20s %-15s %-14s %-11s",
                "ID", "Name", "Specialization", "Contact", "Fee", "Status");
    }

    public String toTableRow() {
        return String.format("%-6s %s %s %s %s %-11s", getId(), UI.fit(getName(), 22),
                UI.fit(specialization, 20), UI.fit(getContactNumber(), 15),
                UI.fit(UI.money(consultationFee), 14), getAvailabilityLabel());
    }
}
