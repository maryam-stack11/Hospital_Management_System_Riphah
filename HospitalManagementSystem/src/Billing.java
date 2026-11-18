import java.math.BigDecimal;

/**
 * A bill for one completed appointment. BigDecimal is used for money so that
 * calculations are exact (double would give rounding errors such as 0.1 + 0.2).
 */
public class Billing {

    public enum PaymentStatus {
        PAID("Paid"), UNPAID("Unpaid"), PARTIALLY_PAID("Partially Paid");

        private final String label;

        PaymentStatus(String label) { this.label = label; }

        public String getLabel() { return label; }
    }

    private final String id;
    private final String patientId;
    private final String appointmentId;
    private final BigDecimal consultationFee;
    private BigDecimal additionalCharges;
    private BigDecimal amountPaid;

    public Billing(String id, String patientId, String appointmentId,
                   BigDecimal consultationFee, BigDecimal additionalCharges, BigDecimal amountPaid) {
        this.id = id;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.consultationFee = consultationFee;
        this.additionalCharges = additionalCharges;
        this.amountPaid = amountPaid;
    }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getAppointmentId() { return appointmentId; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public BigDecimal getAdditionalCharges() { return additionalCharges; }
    public BigDecimal getAmountPaid() { return amountPaid; }

    public BigDecimal getTotalAmount() { return consultationFee.add(additionalCharges); }

    public BigDecimal getBalance() { return getTotalAmount().subtract(amountPaid); }

    void updateAdditionalCharges(BigDecimal charges) throws HospitalException {
        if (charges == null || charges.signum() < 0) {
            throw new HospitalException("Additional charges cannot be negative.");
        }
        BigDecimal normalized = charges.setScale(2, java.math.RoundingMode.HALF_UP);
        if (normalized.add(consultationFee).compareTo(amountPaid) < 0) {
            throw new HospitalException("New total cannot be less than the amount already paid.");
        }
        additionalCharges = normalized;
    }

    /** The status is derived from the amount paid, so it can never be inconsistent. */
    public PaymentStatus getPaymentStatus() {
        if (amountPaid.compareTo(getTotalAmount()) >= 0) return PaymentStatus.PAID;
        if (amountPaid.signum() == 0) return PaymentStatus.UNPAID;
        return PaymentStatus.PARTIALLY_PAID;
    }

    public void markPaid() { amountPaid = getTotalAmount(); }

    public void markUnpaid() { amountPaid = BigDecimal.ZERO.setScale(2); }

    /** Records the total amount received so far; must be more than 0 and less than the total. */
    public void recordPartialPayment(BigDecimal totalPaidSoFar) throws HospitalException {
        if (totalPaidSoFar.signum() <= 0 || totalPaidSoFar.compareTo(getTotalAmount()) >= 0) {
            throw new HospitalException("A partial payment must be greater than 0 and less than the total ("
                    + UI.money(getTotalAmount()) + ").");
        }
        amountPaid = totalPaidSoFar;
    }

    public void displayDetails() {
        UI.thinLine();
        UI.field("Bill ID", id);
        UI.field("Patient ID", patientId);
        UI.field("Appointment ID", appointmentId);
        UI.field("Consultation Fee", UI.money(consultationFee));
        UI.field("Additional Charges", UI.money(additionalCharges));
        UI.field("Total Amount", UI.money(getTotalAmount()));
        UI.field("Amount Paid", UI.money(amountPaid));
        UI.field("Balance Due", UI.money(getBalance()));
        UI.field("Payment Status", getPaymentStatus().getLabel());
        UI.thinLine();
    }

    public static String tableHeader() {
        return String.format("%-6s %-7s %-7s %-13s %-13s %-14s %-14s %-14s",
                "Bill", "Patient", "Appt", "Fee", "Extra", "Total", "Paid", "Status");
    }

    public String toTableRow() {
        return String.format("%-6s %-7s %-7s %s %s %s %s %-14s", id, patientId, appointmentId,
                UI.fit(UI.money(consultationFee), 13), UI.fit(UI.money(additionalCharges), 13),
                UI.fit(UI.money(getTotalAmount()), 14), UI.fit(UI.money(amountPaid), 14),
                getPaymentStatus().getLabel());
    }
}
